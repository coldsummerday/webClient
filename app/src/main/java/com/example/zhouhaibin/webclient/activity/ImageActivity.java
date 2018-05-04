package com.example.zhouhaibin.webclient.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.zhouhaibin.webclient.HttpClient;
import com.example.zhouhaibin.webclient.ImageManager;
import com.example.zhouhaibin.webclient.ImageService;
import com.example.zhouhaibin.webclient.Images;
import com.example.zhouhaibin.webclient.R;
import com.example.zhouhaibin.webclient.Setting;
import com.example.zhouhaibin.webclient.SyncImageManager;
import com.example.zhouhaibin.webclient.VideoActivity;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.example.zhouhaibin.webclient.service.ImageBackService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageActivity extends AppCompatActivity {


    private Context context;
    private Button postAllButton;
    private Button getAllButton;
    private Button postSelectButton;

    private ImageManager imageManager =null;
    private ImageService imageService;
    private HttpClient httpClient;

    private NumberProgressBar bnp;
    Handler downLoadHandler = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        imageManager = new ImageManager();
        imageService = new ImageService(context);
        setContentView(R.layout.activity_image);
        httpClient = new HttpClient();
        postAllButton = (Button)findViewById(R.id.Image_postImages);
        postSelectButton = (Button)findViewById(R.id.Image_postSelectImages);
        getAllButton = (Button)findViewById(R.id.Image_getImages);

       bnp = (NumberProgressBar)findViewById(R.id.image_prgressbar);
       bnp.setMax(100);
       bnp.setOnProgressBarListener(new OnProgressBarListener() {
           @Override
           public void onProgressChange(int current, int max) {

                if(current>=max){
                    Log.d("images","finished");
                    Toast.makeText(context,"任务完成!;",Toast.LENGTH_SHORT).show();
                }
           }
       });

        downLoadHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                DownImagesAsyncTask downImagesAsyncTask = new DownImagesAsyncTask();
                downImagesAsyncTask.execute(msg.obj.toString());
            }
        };

        getAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                httpClient.Get(Setting.getWebAddress()+"/search?context="+android.os.Build.MODEL+"&&type=jpg",downLoadHandler);

            }
        });
        postAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PostImagesAsyncTask postImagesAsyncTask = new PostImagesAsyncTask();
                postImagesAsyncTask.execute("begin");

            }
        });

        postSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("Image/*"); //选择视频 （mp4 3gp 是android支持的视频格式）
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            switch (requestCode){
                case 1:
                    final Handler handler = new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                          if(msg.what==Setting.RESPONSEOK){
                              Toast.makeText(context,"上传成功;",Toast.LENGTH_SHORT).show();
                          }else {
                              Toast.makeText(context,"上传失败;",Toast.LENGTH_SHORT).show();
                          }
                        }
                    };
                    httpClient.post(uri,"image",Thread.currentThread(),handler);
            }
        }
    }



    private class DownImagesAsyncTask extends AsyncTask<String,Integer,String>{

        private int index = 0;
        private int count = 0;
        private int percent = 0;
        Handler updateHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==Setting.RESPONSEOK){
                    index++;
                    if(getPercent(index,count)>(percent+1)){
                        percent = getPercent(index,count);
                        publishProgress(percent);
                    }

                }
            }
        };
        @Override
        protected void onPreExecute() {
            this.index = 0;
            this.count = 0;
            this.percent = 0;
            bnp.setProgress(0);
            postAllButton.setEnabled(false);
            postSelectButton.setEnabled(false);
            getAllButton.setEnabled(false);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {

                postAllButton.setEnabled(true);
                postSelectButton.setEnabled(true);
                getAllButton.setEnabled(true);
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            final HashMap<String,String> webImages = new HashMap<>();
            final List<String> nowImageString = new ArrayList<>();
                    JSONArray jsonArray = JSON.parseArray(strings[0]);
                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = (JSONObject) obj;
                        if(jsonObject.getString("type").contentEquals("true")){
                            continue;
                        }
                        String webfilename = jsonObject.getString("name");
                        String[] elements = webfilename.split("/");
                        String finename = elements[elements.length-1];
                        String localFileName = Setting.getPicturePrefix()+finename;
                        webImages.put(localFileName,webfilename);
                    }
                    List<String> downloadImagesList = new ArrayList<>();
                    List<HashMap<String,String>> images = imageService.getImages();
                    for (HashMap<String,String> data:images)
                    {
                        String localUri = data.get("data");
                        nowImageString.add(localUri);
                    }

                    for(String key :webImages.keySet())
                    {
                        if(!nowImageString.contains(key)){
                            downloadImagesList.add("http://"+webImages.get(key));

                        }
                    }
                    this.count=downloadImagesList.size();
                    if(this.count==0){
                        publishProgress(100);
                        return null;
                    }
                    for(String url:downloadImagesList){
                        httpClient.downLoad(url,Setting.getPicturePrefix(),updateHandler);
                    }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            bnp.setProgress(values[0]);
            super.onProgressUpdate(values);

        }
    }

    private class PostImagesAsyncTask extends AsyncTask<String,Integer,String>{

        private int index = 0;
        private int count = 0;
        private int percent = 0;
        Handler postHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==Setting.RESPONSEOK)
                {
                    index++;
                    if(getPercent(index,count)>(percent+1)){
                        percent = getPercent(index,count);
                        publishProgress(percent);
                    }
                }else if(msg.what==Setting.RESPONSEERROR){
                    httpClient.post(Uri.parse(msg.obj.toString()),"image",Thread.currentThread());
                }

            }
        };
        @Override
        protected void onPreExecute() {
            this.index = 0;
            this.count = 0;
            this.percent = 0;
            bnp.setProgress(0);
            postAllButton.setEnabled(false);
            postSelectButton.setEnabled(false);
            getAllButton.setEnabled(false);
            super.onPreExecute();

        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            bnp.setProgress(values[0]);
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String s) {
            postAllButton.setEnabled(true);
            postSelectButton.setEnabled(true);
            getAllButton.setEnabled(true);
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d("image","start post");
            List<HashMap<String,String>> images = imageService.getImages();
            List<Uri> urs = new ArrayList<>();
            for (HashMap<String,String> data:images)
            {
                String localUri = data.get("data");
                try {
                    Uri uri = Uri.parse(localUri);
                    urs.add(uri);
                }catch (Exception e)
                {
                    continue;
                }
            }
            List<Images> dbImages = imageManager.getDbImages();
            List<String> dbImagesString = new ArrayList<>();
            for(Images image :dbImages){
                dbImagesString.add(image.getName());
            }
            List<Uri> needPostImages = new ArrayList<>();
            for(Uri uri:urs) {
                if (dbImagesString.contains(uri.getPath())) {
                    continue;
                }
                needPostImages.add(uri);
            }
            this.count = needPostImages.size();
            if(this.count==0){
                publishProgress(100);
                return null;
            }
            Log.d("Image",String.valueOf(this.count));
            for(Uri uri:needPostImages){
                try{
                    Thread.currentThread().sleep(300);
                }catch (InterruptedException e)
                {
                }
                httpClient.post(uri,"image",Thread.currentThread(),postHandler);
            }
            return null;
        }
    }
    private static int getPercent(int index,int count){
        double a = (double)index;
        return (int)((a/count)*100);
    }
}
