package com.example.zhouhaibin.webclient;

/**
 * Created by zhouhaibin on 2018/4/22.
 */

public class Cloudfile {
    public String name;
    public String size;
    public String type;
    public Cloudfile(String name,String size,String type)
    {
        this.name =name;
        this.size = size;
        this.type = type;
    }

    /*
    public String getName(){
        return this.name;
    }
    public String getSize(){
        return this.size;
    }
    public String getType(){
        return this.type;
    }

    public void setName(String name){
        this.name = name;
    }
    public void setSize(String size){
        this.size = size;
    }
    public void setType(String type){
        this.type=type;
    }

    @Override
    public String toString() {
        return "name:"+this.name+"\ttype:"+this.type+"+\tsize"+this.size;
    }
    */
}
