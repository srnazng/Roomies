package com.example.roomies.utils;

import static com.example.roomies.utils.JSONSharedPreferences.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.roomies.AddCircleActivity;
import com.example.roomies.R;
import com.example.roomies.model.Circle;
import com.example.roomies.model.UserCircle;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *  TODO: implement different utility functions
 */
public class Utils {
    public static final String TAG = "Utils";
     /**
     * query UserCircle objects that contain current user to get circles that user has joined
     */

    public static void updateCirclesPreferences(Activity activity){

        List<Circle> myCircles = new ArrayList<>();
        // specify what type of data we want to query - UserCircle.class
        ParseQuery<UserCircle> query = ParseQuery.getQuery(UserCircle.class).whereEqualTo(UserCircle.KEY_USER, ParseUser.getCurrentUser());
        // include data referred by user key
        query.include(UserCircle.KEY_USER);
        query.include(UserCircle.KEY_CIRCLE);
        // start an asynchronous call for UserCircle objects that include current user
        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting userCircles", e);
                    return;
                }

                // user has not joined a circle
                if(userCircles.isEmpty()){
                    // go to AddCircleActivity
                    Intent i = new Intent(activity, AddCircleActivity.class);
                    activity.startActivity(i);
                    activity.finish();
                }

                // save received posts to list and notify adapter of new data
                myCircles.clear();
                for(UserCircle userCircle : userCircles){
                    myCircles.add(userCircle.getCircle());
                }

                JSONArray arr = new JSONArray(myCircles);

            }
        });
    }
}
