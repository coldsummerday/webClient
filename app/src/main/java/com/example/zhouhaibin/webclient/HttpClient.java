package com.example.zhouhaibin.webclient;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
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
import java.util.Set;

import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by zhouhaibin on 2018/4/17.
 */


public class HttpClient {
    OkHttpClient okHttpClient = null;
    static int GETFRESPONSE = 0x123;
    static int POSTRESPONSE = 0x456;
    private String SelectPath="";


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
                Message msg = new Message();
                msg.what=Setting.RESPONSEERROR;
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String htmlStr =  response.body().string();
                Message msg = new Message();
                msg.what=Setting.RESPONSEOK;
                msg.obj = htmlStr;
                handler.sendMessage(msg);
            }
        });
    }

    public void downLoad(final String url, final String destFileDir,final Handler handler){
        final Request request = new Request.Builder().url(url).build();
        final Call call  = okHttpClient.newCall(request);

        call.enqueue(new Callback() {


            @Override
            public void onFailure(Request request, IOException e)
            {

                Message msg = handler.obtainMessage();
                msg.what=Setting.RESPONSEERROR;
                handler.sendMessage(msg);
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
                Message msg = handler.obtainMessage();
                msg.what=Setting.RESPONSEOK;
                handler.sendMessage(msg);
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
                Message msg = handler.obtainMessage();
                msg.obj=uri;
                msg.what=Setting.RESPONSEERROR;
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(type=="image"){
                    Images images = new Images(uri.getPath());
                    images.save();
                }
                Message msg =handler.obtainMessage();
                msg.what=Setting.RESPONSEOK;
                handler.sendMessage(msg);
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

    public void postFile(final Uri uri, final Context context,final Handler handler,boolean flag){

        //flag为true的时候,为上传到私人文件夹
        String path = "";
        if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
            SelectPath = uri.getPath();
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
            SelectPath = getPath(context, uri);

        } else {//4.4以下下系统调用方法
            SelectPath = getRealPathFromURI(uri,context);
        }
        if(flag){
            String phoneName = android.os.Build.MODEL;
            path ="/upload?path=/"+phoneName+"/file";
        }else {
            path ="/upload?path=/file";
        }
        File file = new File(SelectPath);
        String fileName= file.getName();
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .build();
        Request request = new Request.Builder()
                .url(Setting.getWebAddress()+path).post(requestBody).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Message msg = new Message();
                msg.what= Setting.RESPONSEERROR;
                msg.obj=1;
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Message msg = new Message();
                msg.what= Setting.RESPONSEOK;
                msg.obj=1;
                handler.sendMessage(msg);
            }
        });
    }
    public void postVedio(final Uri uri, final Context context,final Handler handler){
        if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
            SelectPath = uri.getPath();
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
            SelectPath = getPath(context, uri);

        } else {//4.4以下下系统调用方法
            SelectPath = getRealPathFromURI(uri,context);
        }
        File file = new File(SelectPath);
        String fileName= file.getName();
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .build();
        Request request = new Request.Builder()
                .url(Setting.getWebAddress()+"/upload?path=/video").post(requestBody).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Message msg = new Message();
                msg.what= Setting.RESPONSEERROR;
                msg.obj=1;
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Message msg = new Message();
                msg.what= Setting.RESPONSEOK;
                msg.obj=1;
                handler.sendMessage(msg);
            }
        });
    }

    public String getFileName(String path)
    {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }


    public String getRealPathFromURI(Uri contentUri,Context context) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(null!=cursor&&cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }




}
