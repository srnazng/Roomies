package com.example.roomies.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Expense")
public class Expense extends ParseObject {
    public static final String KEY_CREATOR = "creator";
    public static final String KEY_CIRCLE = "circle";
    public static final String KEY_NAME = "name";
    public static final String KEY_TOTAL = "total";
    public static final String KEY_PROOF = "proof";

    public ParseUser getCreator(){
        return getParseUser(KEY_CREATOR);
    }

    public void setCreator(ParseUser user){
        put(KEY_CREATOR, user);
    }

    public Circle getCircle(){
        return (Circle) getParseObject(KEY_CIRCLE);
    }

    public void setCircle(Circle circle) { put(KEY_CIRCLE, circle); }

    public String getName() { return getString(KEY_NAME); }

    public void setName(String name) { put(KEY_NAME, name); }

    public Double getTotal() { return getDouble(KEY_TOTAL); }

    public void setTotal(Float total) { put(KEY_TOTAL, total); }

    public ParseFile getProof() { return getParseFile(KEY_PROOF); }

    public void setProof(ParseFile proof) { put(KEY_PROOF, proof); }
}
