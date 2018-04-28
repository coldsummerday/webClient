package com.example.zhouhaibin.webclient;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhouhaibin on 2018/4/17.
 */

public class ImageService {
    private Context context;

    //只获取手机系统相册中照片
    private String prefix="";

    public ImageService(Context context) {
        this.context = context;
        prefix=context.getString(R.string.photedir);
    }
    public List<HashMap<String,String>> getImages(){
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};
        ContentResolver contentResolver = context.getContentResolver();
        String selection = MediaStore.Images.Media.MIME_TYPE + "=?";

        String[] selectionArgs = { "image/jpeg" };
        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";

        Cursor cursor = contentResolver.query(uri,projection,selection,selectionArgs,sortOrder);

        List<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>();

        if (cursor!=null)
        {
            HashMap<String,String> imageMap = null;
            cursor.moveToFirst();
            while (cursor.moveToNext())
            {
                imageMap = new HashMap<String, String>();


                String imageUri =cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
                if (imageUri.contains(this.prefix)){
                    imageMap.put("data",imageUri);
                }
                imageList.add(imageMap);
            }
            cursor.close();
        }
        return imageList;
    }
}
