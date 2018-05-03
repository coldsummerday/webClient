package com.example.zhouhaibin.webclient.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.zhouhaibin.webclient.HttpClient;
import com.example.zhouhaibin.webclient.R;
import com.example.zhouhaibin.webclient.Setting;
import com.example.zhouhaibin.webclient.VideoActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class downLoadFileActivity extends AppCompatActivity {

    private ListView downLoadListView;
    private Handler syncHandler,downloadHandle;
    private HttpClient httpClient;
    private Context context;
    private List<String> listData = new ArrayList<>();
    private fileListAdapter fileListAdapter=null;
    private String phoneString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load_file);
        context = this;
        downLoadListView = (ListView)findViewById(R.id.download_listView);
        phoneString =android.os.Build.MODEL;
        httpClient = new HttpClient();

        fileListAdapter = new fileListAdapter(context,R.layout.vedio_item,listData);

        downLoadListView.setAdapter(fileListAdapter);

        syncHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                if (msg.what == Setting.RESPONSEOK) {
                    JSONArray jsonArray = JSON.parseArray(msg.obj.toString());
                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = (JSONObject) obj;
                        String url = jsonObject.getString("name");
                        String[] elements = url.split("/");
                        if(elements[2].contentEquals(phoneString)||elements[2].contentEquals("file")){
                            String type = jsonObject.getString("type");
                            if (type.contentEquals("true")||listData.contains(url)){
                                continue;
                            }
                            listData.add(url);
                        }

                    }
                    Log.d("downfile",listData.toString());
                    fileListAdapter.notifyDataSetChanged();
                }else if(msg.what==Setting.RESPONSEERROR){
                    Toast.makeText(context,"请检查网络连接情况",Toast.LENGTH_SHORT).show();
                }
            }
        };


        downloadHandle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==Setting.RESPONSEOK){
                    Toast.makeText(context,"下载成功;",Toast.LENGTH_SHORT).show();
                }else  {
                    Toast.makeText(context,"下载失败;",Toast.LENGTH_SHORT).show();
                }
            }
        };
        getData();

    }

    private void getData(){
        listData.clear();
        httpClient.Get(Setting.getWebAddress()+"/search?context="+phoneString+"%2Ffile",syncHandler);
        httpClient.Get(Setting.getWebAddress()+"/search?context=file",syncHandler);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(1,100,1,"重新加载");

        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case 100:
                getData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    class fileListAdapter extends ArrayAdapter<String> {
        private int resourceId;

        public fileListAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            this.resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String Url = getItem(position);
            View view;
            ViewHolder holder;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(resourceId, null);
                holder = new ViewHolder();
                holder.fileNameTextView = (TextView) view.findViewById(R.id.vedio_name_textView);
                holder.playButton = (Button) view.findViewById(R.id.Vedio_play_Button);
                holder.downloadButton = (Button)view.findViewById(R.id.Vedio_download_Button);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            String[] elements = Url.split("/");
            String vedionName = elements[elements.length-1];
            holder.fileNameTextView.setText(vedionName);
            if(vedionName.length()>20){
                holder.fileNameTextView.setTextSize(10);
            }
            holder.downloadButton.setVisibility(View.GONE);
            holder.playButton.setVisibility(View.VISIBLE);
            holder.playButton.setText("下载");
            holder.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    httpClient.downLoad("http://" + Url, Setting.getClientPath(), downloadHandle);
                }
            });

            return view;

        }
    }
    class ViewHolder {
        TextView fileNameTextView;
        Button downloadButton;
        Button playButton;
    }

}



