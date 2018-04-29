package com.example.zhouhaibin.webclient;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.litepal.tablemanager.Connector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity  {

    private static String baseDir = "/storage/emulated/0/client";
    private static String videoDir = baseDir+"/video";

    private Context context;

    private static final int REQUEST_PERMISSION = 0;
    private Handler uiHandler = null;
    private HttpClient httpClient =null;
    private ImageView imageView;

    private Button downloadButton =null;
    private Button postButton = null;
    private Cloudfile[] cloudfiles;
    private SyncImageManager syncImageManager;
    private ImageService imageService;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_main);
        rePermissions();
       // imageView = (ImageView)findViewById(R.id.imageView);
        uiHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                Log.d("html", msg.obj.toString());
                JSONArray jsonArray = JSON.parseArray(msg.obj.toString());
                for (Object obj : jsonArray) {
                    JSONObject jsonObject = (JSONObject) obj;
                    System.out.println(jsonObject.getString("name")+":"+jsonObject.getString("type")+":"+jsonObject.getString("size"));
                }

            }
        };

        init();
        Connector.getDatabase();

        syncImageManager = new SyncImageManager();
        imageService = new ImageService(context);
        postButton = (Button)findViewById(R.id.Button2);
        downloadButton=(Button)findViewById(R.id.Button1);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String source_url = "http://192.168.199.244:8081/cloud/第十课.mkv";

                Intent intent = new Intent(context,VideoActivity.class);
                intent.putExtra("source_url",source_url);
                startActivity(intent);
              /*  Log.d("main","start down");
                syncImageManager.getFromWeb(context);
                */
            }
        });
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String source_url = "http://192.168.199.244:8081/cloud/第十课.mkv";
                Intent intent = new Intent(context,VideoActivity.class);
                intent.putExtra("source_url",source_url);
                startActivity(intent);
                /*
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
                Log.d("main","start");
                syncImageManager.postToWeb(urs);
                */
            }
        });

        /*
        httpClient = new HttpClient(this);
        httpClient.Get(getResources().getString(R.string.webaddress)+"/search",uiHandler);
        ImageService imageService = new ImageService(this);*/
        /*
        List<HashMap<String,String>> images = imageService.getImages();
        String data = images.get(1).get("data");
        Uri uri =Uri.parse(data);
        imageView.setImageURI(uri);
        httpClient.post(uri);
        */


    }



    private boolean rePermissions()
    {

        List<String> permissionList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);

        }

        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {


                        System.out.println("Permissions --> " + "Permission Granted: " + permissions[i]);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        System.out.println("Permissions --> " + "Permission Denied: " + permissions[i]);
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void showImages(List<HashMap<String, String>> images){
        if(images.size()>0){
            for(int i=0;i<images.size();i++)
            {
                HashMap<String,String> image = images.get(i);
                Log.d("image","data:"+image.get("data"));

            }
        }else {
            Log.d("image","no image data");
        }
    }


    private void init(){
        createDir(baseDir);
        createDir(videoDir);
    }

    private void createDir(String dirPath){
        File dir = new File(dirPath);
        //文件夹是否已经存在
        if (dir.exists()) {
            return ;
        }
        if (!dirPath.endsWith(File.separator)) {//不是以 路径分隔符 "/" 结束，则添加路径分隔符 "/"
            dirPath = dirPath + File.separator;
        }
        //创建文件夹
        if (dir.mkdirs()) {
            return ;
        }
    }
}
