package com.example.zhouhaibin.webclient.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageBackService extends Service {

    private MyBinder mBinder = new MyBinder();
    public ImageBackService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class MyBinder extends Binder{
        public void startPostImages() {
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
            syncImageManager.postToWeb(urs);
            // 执行具体的下载任务
        }

        public void startGetImages(){

        }

    }
}
