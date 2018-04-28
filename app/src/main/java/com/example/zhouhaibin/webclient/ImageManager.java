package com.example.zhouhaibin.webclient;

import android.net.Uri;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouhaibin on 2018/4/27.
 */

public class ImageManager {


    public void saveImages(Uri[] uris){
        List<Images> images = new ArrayList<>();
        for(Uri uri :uris){

            File file = new File(uri.getPath());
            String fileName= file.getName();
            Images image = new Images(fileName);
            images.add(image);
        }
        DataSupport.saveAll(images);
    }

    public List<Images> getDbImages(){
        return DataSupport.findAll(Images.class);
    }
    public List<Images> getDbImages(String name){
        return DataSupport.where("name CONTAINS ?",name).find(Images.class);
    }


}
