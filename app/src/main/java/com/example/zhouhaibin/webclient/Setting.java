package com.example.zhouhaibin.webclient;

/**
 * Created by zhouhaibin on 2018/4/22.
 */

public class Setting {
    public static String webAddress = "http://192.168.199.244:8081";

    public static String getWebAddress(){
        return webAddress;
    }

    public static String getPicturePrefix()
    {
        return "/storage/emulated/0/DCIM/Camera/";
    }

    public static String getClientPath(){
        return "/storage/emulated/0/client/";
    }
    public static String webImagedir ="/images";

}
