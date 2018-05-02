package com.example.zhouhaibin.webclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.zhouhaibin.webclient.HttpClient;
import com.example.zhouhaibin.webclient.ImageService;
import com.example.zhouhaibin.webclient.R;
import com.example.zhouhaibin.webclient.Setting;
import com.example.zhouhaibin.webclient.SyncImageManager;
import com.example.zhouhaibin.webclient.VideoActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageActivity extends AppCompatActivity {


    private Context context;
    private Button postAllButton;
    private Button getAllButton;
    private Button postSelectButton;

    private SyncImageManager syncImageManager;
    private ImageService imageService;
    private HttpClient httpClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        syncImageManager = new SyncImageManager();
        imageService = new ImageService(context);
        setContentView(R.layout.activity_image);
        httpClient = new HttpClient();
        postAllButton = (Button)findViewById(R.id.Image_postImages);
        postSelectButton = (Button)findViewById(R.id.Image_postSelectImages);
        getAllButton = (Button)findViewById(R.id.Image_getImages);
        getAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncImageManager.getFromWeb(context);
            }
        });


        postAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            String SelectPath ="";
            Uri uri = data.getData();

            switch (requestCode){
                case 1:
                    final Handler handler = new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                          if(msg.what==0x456){
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
}
