package com.example.zhouhaibin.webclient;

import android.content.Context;
import android.net.Uri;
import android.os.Message;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Handler;
import android.util.Log;

/**
 * Created by zhouhaibin on 2018/4/17.
 */


public class HttpClient {
    OkHttpClient okHttpClient = null;
    static int GETFRESPONSE = 0x123;
    static int POSTRESPONSE = 0x456;


    public HttpClient(){

        okHttpClient = new OkHttpClient();
    }


    public void Get(String url, final Handler handler)
    {

        final Request request = new Request.Builder()
                .url(url).build();
        Call call = okHttpClient.newCall(request);
        Log.d("okhttp","start get");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("okhttperror",request.toString());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String htmlStr =  response.body().string();
                Message msg = new Message();
                msg.what=GETFRESPONSE;
                msg.obj = htmlStr;
                handler.sendMessage(msg);
            }
        });
    }

    public void downLoad(final String url, final String destFileDir){
        final Request request = new Request.Builder().url(url).build();
        final Call call  = okHttpClient.newCall(request);
        Log.d("okhttp","start download");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("okhttp,","error");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len=0;
                FileOutputStream fos = null;
                try{
                    is = response.body().byteStream();
                    File file = new File(destFileDir,getFileName(url));
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1)
                    {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                }catch (IOException e){
                    Log.d("error",e.getMessage());
                }finally {
                    try
                    {
                        if (is != null) is.close();
                    } catch (IOException e)
                    {
                    }
                    try
                    {
                        if (fos != null) fos.close();
                    } catch (IOException e)
                    {
                    }
                }

            }
        });
    }
    public void post(final Uri uri,final String type, final Thread currentThread, final Handler handler){
        String target="";
        if(type.contentEquals("image")){
            target = android.os.Build.MODEL;
        }else if(type.contentEquals("video")){
            target = "video";
        }else {
            target = "file";
        }
        File file = new File(uri.getPath());
        String fileName= file.getName();
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .build();
        Request request = new Request.Builder()
                .url(Setting.getWebAddress()+"/upload?path=/"+target).post(requestBody).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //批量上传的时候如果出错了,当前线程再卡一会儿
                try{
                    Log.d("okhttp","error,sleep");
                    currentThread.sleep(2000);
                }catch (InterruptedException es){
                }
                Message msg = new Message();
                msg.obj=uri;
                msg.what=0x123;
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(type=="image"){
                    Images images = new Images(uri.getPath());
                    images.save();
                }
            }
        });


    }
    public void post(final Uri uri, final String type, final Thread currentThread){
        String target="";
        if(type.contentEquals("image")){
            target = android.os.Build.MODEL;
        }else if(type.contentEquals("video")){
            target = "video";
        }else {
            target = "file";
        }
        File file = new File(uri.getPath());
        String fileName= file.getName();
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .build();
        Request request = new Request.Builder()
                .url(Setting.getWebAddress()+"/upload?path=/"+target).post(requestBody).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //批量上传的时候如果出错了,当前线程再卡一会儿
                try{
                    Log.d("okhttp","error,sleep");
                    currentThread.sleep(150);
                }catch (InterruptedException es){

                }

            }

            @Override
            public void onResponse(Response response) throws IOException {
               if(type=="image"){
                   Images images = new Images(uri.getPath());
                   images.save();
               }
            }
        });


    }
    public String getFileName(String path)
    {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }



}
