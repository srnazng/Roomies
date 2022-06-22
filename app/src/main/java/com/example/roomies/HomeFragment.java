package com.example.roomies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.roomies.model.Circle;
import com.example.roomies.model.UserCircle;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Home fragment showing current user's Circle information
 */
public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";
    public static final int NUM_PROFILES_SHOWN = 5;

    private Circle circle;
    private List<UserCircle> userCircleList;
    private ImageView ivCirclePhoto;
    private TextView tvCircleName;

    private ImageView ivProfile1;
    private ImageView ivProfile2;
    private ImageView ivProfile3;
    private ImageView ivProfile4;
    private ImageView ivProfile5;
    private TextView tvExtraProfiles;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // initialize list of all UserCircles related to current circle
        userCircleList = new ArrayList<>();

        // bind layout
        ivCirclePhoto = view.findViewById(R.id.ivCirclePhoto);
        tvCircleName = view.findViewById(R.id.tvCircleName);

        // TODO: use for loop
        ivProfile1 = view.findViewById(R.id.ivProfile1);
        ivProfile2 = view.findViewById(R.id.ivProfile2);
        ivProfile3 = view.findViewById(R.id.ivProfile3);
        ivProfile4 = view.findViewById(R.id.ivProfile4);
        ivProfile5 = view.findViewById(R.id.ivProfile5);
        tvExtraProfiles = view.findViewById(R.id.tvExtraProfiles);

        updateCircle(getActivity(), view);

        return view;
    }

    /**
     * Query UserCircle objects that contain current user to get circles that user has joined
     * TODO: return circle, add to utils
     */
    public void updateCircle(Context context, View view){

        // specify what type of data we want to query - UserCircle.class
        ParseQuery<UserCircle> query = ParseQuery.getQuery(UserCircle.class).whereEqualTo(UserCircle.KEY_USER, ParseUser.getCurrentUser());
        // include data referred by circle key
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
                    Intent i = new Intent(getActivity(), AddCircleActivity.class);
                    startActivity(i);
                    getActivity().finish();
                }

                // save received posts to list and notify adapter of new data
                circle = userCircles.get(0).getCircle();

                // fill image and text on home screen
                Glide.with(context).load(circle.getImage().getUrl()).apply(RequestOptions.circleCropTransform()).into(ivCirclePhoto);
                tvCircleName.setText(circle.getName());

                updateAllProfiles(view);
            }
        });
    }

    /**
     * Get all UserCircles related to current circle
     * TODO: return list
     */
    private void updateAllProfiles(View view){
        // find profile of all users in current circle - query UserCircle.class
        ParseQuery<UserCircle> query = ParseQuery.getQuery(UserCircle.class).whereEqualTo(UserCircle.KEY_CIRCLE, circle);
        // include data referred by user key
        query.include(UserCircle.KEY_USER);
        // start an asynchronous call for UserCircle objects that include current circle
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
                    Intent i = new Intent(getActivity(), AddCircleActivity.class);
                    startActivity(i);
                    getActivity().finish();
                }

                // update userCircleList
                userCircleList.clear();
                userCircleList.addAll(userCircles);
                if(userCircleList.size() > 0){
                    fillProfileImages(view);
                }
            }
        });
    }

    /**
     * Show profile images of all users in circle
     * @param view
     */
    public void fillProfileImages(View view){
        // TODO: use for loop
        ParseFile image;
        if(userCircleList.size() > 0 && (image = userCircleList.get(0).getUser().getParseFile("image")) != null){
            Glide.with(getActivity()).load(image.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivProfile1);
            ivProfile1.setVisibility(view.VISIBLE);
        }

        if(userCircleList.size() > 1 && (image = userCircleList.get(1).getUser().getParseFile("image")) != null){
            Glide.with(getActivity()).load(image.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivProfile2);
            ivProfile2.setVisibility(view.VISIBLE);
        }
        else{
            ivProfile2.setVisibility(view.GONE);
        }

        if(userCircleList.size() > 2 && (image = userCircleList.get(2).getUser().getParseFile("image")) != null){
            Glide.with(getActivity()).load(image.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivProfile3);
            ivProfile3.setVisibility(view.VISIBLE);
        }
        else{
            ivProfile3.setVisibility(view.GONE);
        }

        if(userCircleList.size() > 3 && (image = userCircleList.get(3).getUser().getParseFile("image")) != null){
            Glide.with(getActivity()).load(image.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivProfile4);
            ivProfile4.setVisibility(view.VISIBLE);
        }
        else{
            ivProfile4.setVisibility(view.GONE);
        }

        if(userCircleList.size() > 4 && (image = userCircleList.get(4).getUser().getParseFile("image")) != null){
            Glide.with(getActivity()).load(image.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivProfile5);
            ivProfile5.setVisibility(view.VISIBLE);
        }
        else{
            ivProfile5.setVisibility(view.GONE);
        }

        // do not show additional user profile images
        // show how many additional users there are whose profile images are not shown
        if(userCircleList.size() > NUM_PROFILES_SHOWN){
            tvExtraProfiles.setVisibility(view.VISIBLE);
            int extra = userCircleList.size() - NUM_PROFILES_SHOWN;
            tvExtraProfiles.setText("+" + extra);
        }
        else{
            tvExtraProfiles.setVisibility(view.GONE);
        }
    }
}