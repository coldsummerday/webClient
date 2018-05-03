package com.example.zhouhaibin.webclient.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.zhouhaibin.webclient.HttpClient;
import com.example.zhouhaibin.webclient.ImageService;
import com.example.zhouhaibin.webclient.R;
import com.example.zhouhaibin.webclient.Setting;
import com.example.zhouhaibin.webclient.VideoActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class VedioSelectActivity extends AppCompatActivity {

    private Context context;
    private ListView listView = null;
    private HttpClient httpClient;
    private List<String> vedioNameList = new ArrayList<String>();
    private VedioListAdapter vedioListAdapter =null;
    private VedioListAdapter tvVideoListAdapter = null;
    private HashMap<String,String> hashMap= new HashMap<>();
    private Handler handler;
    private Handler downloadHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vedio_select);
        listView = (ListView)findViewById(R.id.vedio_Listview);

        httpClient = new HttpClient();


        vedioListAdapter = new VedioListAdapter(VedioSelectActivity.this,R.layout.vedio_item,vedioNameList,hashMap,false);
        tvVideoListAdapter = new VedioListAdapter(VedioSelectActivity.this,R.layout.vedio_item,vedioNameList,hashMap,true);
        listView.setAdapter(vedioListAdapter);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == Setting.RESPONSEOK) {
                    JSONArray jsonArray = JSON.parseArray(msg.obj.toString());
                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = (JSONObject) obj;
                        String url = jsonObject.getString("name");
                        vedioNameList.add(url);
                    }
                    vedioListAdapter.notifyDataSetChanged();
                }else if(msg.what==Setting.RESPONSEERROR){
                    LoadTvVideo();
                }
            }
        };

        downloadHandle = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                if (msg.what==Setting.RESPONSEOK){
                    Toast.makeText(context,"下载成功;",Toast.LENGTH_SHORT).show();
                }else  {
                    Toast.makeText(context,"下载失败;",Toast.LENGTH_SHORT).show();
                }
            }
        };
        LoadOnlineVedio();
    }
    private void LoadLocalJson(){

        InputStream inputStream = getResources().openRawResource(R.raw.tv);
        InputStreamReader inputStreamReader = null;
        try{
            inputStreamReader=new InputStreamReader(inputStream, "utf-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String jsonString = sb.toString();
        JSONArray jsonArray = JSON.parseArray(jsonString);
        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            String url = jsonObject.getString("data");
            String name = jsonObject.getString("name");
            hashMap.put(name,url);
            vedioNameList.add(name);
        }
    }

    private void LoadTvVideo(){
        vedioNameList.clear();
        listView.setAdapter(tvVideoListAdapter);
        final Handler syncHandler = new Handler(){
            public void handleMessage(Message msg) {
                if (msg.what == Setting.RESPONSEOK) {
                    JSONArray jsonArray = JSON.parseArray(msg.obj.toString());
                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = (JSONObject) obj;
                        String url = jsonObject.getString("data");
                        String name = jsonObject.getString("name");
                        hashMap.put(name, url);
                        vedioNameList.add(name);
                    }

                }else if (msg.what==Setting.RESPONSEERROR) {
                    LoadLocalJson();
                }
                tvVideoListAdapter.notifyDataSetChanged();

            }
        };
        httpClient.Get(Setting.getWebAddress()+"/cloud/file/tv.json",syncHandler);

    }
    private void LoadOnlineVedio(){
        vedioNameList.clear();
        listView.setAdapter(vedioListAdapter);
        httpClient.Get(Setting.getWebAddress()+"/search?type=vedio",handler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(1,100,1,"播放在线视频");
        menu.add(1,200,1,"本机视频");
        menu.add(1,300,1,"上传视频");
        menu.add(1,400,1,"播放卫视");
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case 100:
                LoadOnlineVedio();
                break;
            case 200:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                 intent.setType("video/*"); //选择视频 （mp4 3gp 是android支持的视频格式）
                                 intent.addCategory(Intent.CATEGORY_OPENABLE);
                                 startActivityForResult(intent, 1);
                break;
            case 300:
                Intent Postintent = new Intent(Intent.ACTION_GET_CONTENT);
                Postintent.setType("*/*"); //选择任意类型
                Postintent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Postintent, 2);
                break;
            case 400:
                LoadTvVideo();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String SelectPath ="";
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
                SelectPath = uri.getPath();
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                SelectPath = httpClient.getPath(context, uri);

            } else {//4.4以下下系统调用方法
                SelectPath = httpClient.getRealPathFromURI(uri,context);
            }
            switch (requestCode){
                case 1:
                    Intent openVideoIntent = new Intent(context,VideoActivity.class);
                    openVideoIntent.putExtra("Source",SelectPath);
                    startActivity(openVideoIntent);
                    break;
                case 2:
                    final Handler postHandle = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        if(msg.what==Setting.RESPONSEOK){
                            Toast.makeText(context,"上传成功;",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context,"上传失败;",Toast.LENGTH_SHORT).show();
                        }
                    }

                };
                    httpClient.postVedio(uri,context,postHandle);
                    break;


            }



        }
    }

    class VedioListAdapter extends ArrayAdapter<String> {
        private HashMap<String, String> nameToUrl;
        private int resourceId;
        private boolean flag;

        public VedioListAdapter(Context context, int textViewResourceId, List<String> objects, HashMap<String, String> hashMap,boolean flag) {
            super(context, textViewResourceId, objects);
            this.nameToUrl = hashMap;
            this.resourceId = textViewResourceId;
            this.flag = flag;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String Url = getItem(position);
            View view;
            ViewHolder holder;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(resourceId, null);
                holder = new ViewHolder();
                holder.videoNameTextView = (TextView) view.findViewById(R.id.vedio_name_textView);
                holder.playButton = (Button) view.findViewById(R.id.Vedio_play_Button);
                holder.downloadButton = (Button)view.findViewById(R.id.Vedio_download_Button);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            String[] elements = Url.split("/");
            String vedionName = elements[elements.length-1];

            if(!this.flag) {
                holder.videoNameTextView.setText(vedionName);
                holder.downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        httpClient.downLoad("http://" + Url, Setting.getClientPath(), downloadHandle);
                    }
                });
            }else {
                holder.downloadButton.setVisibility(View.GONE);
                holder.videoNameTextView.setText(Url);
            }
            holder.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, VideoActivity.class);
                    if(!flag){
                        intent.putExtra("Source","http://"+Url);
                    }else {
                        String data = hashMap.get(Url);
                        intent.putExtra("Source",data);
                    }
                    startActivity(intent);
                }
            });
            return view;

        }
    }
        class ViewHolder {
            TextView videoNameTextView;
            Button downloadButton;
            Button playButton;
        }

}

