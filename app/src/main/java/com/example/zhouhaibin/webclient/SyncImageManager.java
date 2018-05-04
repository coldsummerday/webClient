package com.example.zhouhaibin.webclient;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.daimajia.numberprogressbar.NumberProgressBar;

import org.apache.http.conn.scheme.HostNameResolver;
import org.litepal.crud.DataSupport;

/**
 * Created by zhouhaibin on 2018/4/27.
 */

public class SyncImageManager {

    private ImageManager imageManager =null;
    private HttpClient httpClient = null;
    private Handler downloadHandler = new Handler();
     Handler reTryHandler =null;
    public SyncImageManager(){
        this.imageManager = new ImageManager();
        this.httpClient = new HttpClient();
         reTryHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==0x123){
                    httpClient.post(Uri.parse(msg.obj.toString()),"image",Thread.currentThread());
                }
            }
        };
    }

    public void postToWeb(List<Uri> localImages,Activity activity,final Handler barHandler)  {
        List<Images> dbImages = imageManager.getDbImages();
        List<String> dbImagesString = new ArrayList<>();
        for(Images image :dbImages){
            dbImagesString.add(image.getName());
        }

        double index = 0.0;
        int count = localImages.size();
        double progress = 0.00;

        for(Uri uri:localImages){
            if (dbImagesString.contains(uri.getPath())){
               continue;
            }
            try{
                Thread.currentThread().sleep(300);
            }catch (InterruptedException e)
            {

            }
            httpClient.post(uri,"image",Thread.currentThread(),reTryHandler);
            index++;

            if((index/count)>(progress+0.01)){
                progress+=0.01;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("send","1%");
                    }
                });

            }
        }
    }

    public void getFromWeb(Context context, final Activity activity,final Handler barHandler){

        final HashMap<String,String> webImages = new HashMap<>();
        final ImageService imageService = new ImageService(context);
        final List<String> nowImageString = new ArrayList<>();
       Handler handler = new Handler(){
           @Override
           public void handleMessage(Message msg) {

               JSONArray jsonArray = JSON.parseArray(msg.obj.toString());
               for (Object obj : jsonArray) {
                   JSONObject jsonObject = (JSONObject) obj;
                   System.out.println(jsonObject.getString("name")+":"+jsonObject.getString("type")+":"+jsonObject.getString("size"));
                   if(jsonObject.getString("type").contentEquals("true")){
                       continue;
                   }
                   String webfilename = jsonObject.getString("name");
                   String[] elements = webfilename.split("/");
                   String finename = elements[elements.length-1];

                   String localFileName = Setting.getPicturePrefix()+finename;
                   webImages.put(localFileName,webfilename);
               }
               List<HashMap<String,String>> images = imageService.getImages();
            for (HashMap<String,String> data:images)
            {
                String localUri = data.get("data");
                nowImageString.add(localUri);
            }
               double index = 0.0;
               int count = webImages.size();
               double progress = 0.00;

            for(String key :webImages.keySet())
            {
                if(!nowImageString.contains(key)){

                    httpClient.downLoad("http://"+webImages.get(key),Setting.getClientPath(),downloadHandler);
                }
                if((index/count)>(progress+0.01)){
                    progress+=0.01;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("send","1%");
                        }
                    });
                }
            }
           }
       };
       httpClient.Get(Setting.getWebAddress()+"/search?context="+android.os.Build.MODEL+"&&type=jpg",handler);
    }
}
