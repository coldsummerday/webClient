package com.example.zhouhaibin.webclient.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.example.zhouhaibin.webclient.ImageService;
import com.example.zhouhaibin.webclient.SyncImageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageBackService extends Service {

    private ImageService imageService;
    private SyncImageManager syncImageManager = new SyncImageManager();
    private MyBinder mBinder = new MyBinder();
    public ImageBackService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

   public   class MyBinder extends Binder{
        public void startPostImages(final Context context, Activity activity,Handler handler) {


                    imageService = new ImageService(context);
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
                    syncImageManager.postToWeb(urs,activity,handler);

        }
        public void startGetImages(final Context context,Activity activity,Handler handler){
                    syncImageManager.getFromWeb(context,activity,handler);
        }

    }
}
