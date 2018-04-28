package com.example.zhouhaibin.webclient;

import org.litepal.crud.DataSupport;

/**
 * Created by zhouhaibin on 2018/4/27.
 */

public class Images extends DataSupport{
    private int id;
    private String name;

    public Images(String name){
        this.name = name;
    }
    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }

    public void setId(int id){
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }

}
