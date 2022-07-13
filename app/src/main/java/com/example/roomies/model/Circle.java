package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.parceler.Parcel;

@ParseClassName("Circle")
public class Circle extends ParseObject {
    public static final String KEY_IMAGE = "image";
    public static final String KEY_NAME = "name";
    public static final String KEY_NOTIFICATION = "notificationKey";

    public String getNotificationKey() { return getString(KEY_NOTIFICATION); }

    public void setNotificationKey(String key) { put(KEY_NOTIFICATION, key); }

    public String getName(){ return getString(KEY_NAME); }

    public void setName(String name){
        put(KEY_NAME, name);
    }

    public ParseFile getImage(){
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile parseFile){
        put(KEY_IMAGE, parseFile);
    }
}
