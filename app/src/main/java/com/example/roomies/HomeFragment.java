package com.example.roomies;

import static com.example.roomies.model.CircleManager.getChoreCollection;
import static com.example.roomies.model.CircleManager.getExpenseCollection;
import static com.example.roomies.model.CircleManager.getUserCircleList;
import static com.example.roomies.model.CircleManager.getCurrentCircle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.roomies.model.Chore;
import com.example.roomies.model.Circle;
import com.example.roomies.model.Expense;
import com.example.roomies.model.UserCircle;
import com.parse.ParseFile;

import java.util.List;

/**
 * Home fragment showing current user's Circle information
 */
public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";
    public static final int NUM_PROFILES_SHOWN = 5;

    private ImageView ivCirclePhoto;
    private TextView tvCircleName;

    private ImageView ivProfile1;
    private ImageView ivProfile2;
    private ImageView ivProfile3;
    private ImageView ivProfile4;
    private ImageView ivProfile5;
    private TextView tvExtraProfiles;

    private TextView tvPendingRequestsNum;
    private TextView tvPendingPaymentsNum;
    private TextView tvHighNum;
    private TextView tvMedNum;
    private TextView tvLowNum;

    private ConstraintLayout profileGroup;

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

        // bind layout
        ivCirclePhoto = view.findViewById(R.id.ivCirclePhoto);
        tvCircleName = view.findViewById(R.id.tvCircleName);
        tvPendingRequestsNum = view.findViewById(R.id.tvPendingRequestsNum);
        tvPendingPaymentsNum = view.findViewById(R.id.tvPendingPaymentsNum);
        tvHighNum = view.findViewById(R.id.tvHighNum);
        tvMedNum = view.findViewById(R.id.tvMedNum);
        tvLowNum = view.findViewById(R.id.tvLowNum);
        profileGroup = view.findViewById(R.id.profileGroup);
        profileGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // view list of users in circle
                Intent i = new Intent(getActivity(), CircleProfilesActivity.class);
                getActivity().startActivity(i);
            }
        });

        ivProfile1 = view.findViewById(R.id.ivProfile1);
        ivProfile2 = view.findViewById(R.id.ivProfile2);
        ivProfile3 = view.findViewById(R.id.ivProfile3);
        ivProfile4 = view.findViewById(R.id.ivProfile4);
        ivProfile5 = view.findViewById(R.id.ivProfile5);
        tvExtraProfiles = view.findViewById(R.id.tvExtraProfiles);

        updateCircle(getActivity(), view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCircle(getActivity(), getView());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        updateCircle(getActivity(), view);
    }

    /**
     * Query UserCircle objects that contain current user to get circles that user has joined
     */
    public void updateCircle(Context context, View view){
        if(getCurrentCircle() != null){
            Circle currentCircle = getCurrentCircle();

            // fill image and text on home screen
            Glide.with(context).load(currentCircle.getImage().getUrl()).apply(RequestOptions.circleCropTransform()).into(ivCirclePhoto);
            tvCircleName.setText(currentCircle.getName());

            updateStats();
            updateAllProfiles(view);
        }
        else{
            Intent i = new Intent(context, AddCircleActivity.class);
            startActivity(i);
            ((Activity)context).finish();
        }
    }

    /**
     * Get all UserCircles related to current circle
     * TODO: return list
     */
    private void updateAllProfiles(View view){
        List<UserCircle> userCircleList = getUserCircleList();
        if( userCircleList != null && userCircleList.size() > 0){
            fillProfileImages(view);
        }
    }

    /**
     * Show profile images of all users in circle
     * @param view
     */
    public void fillProfileImages(View view){
        // TODO: use for loop
        if(getActivity() == null){
            return;
        }

        List<UserCircle> userCircleList = getUserCircleList();

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

    /**
     * update home screen expense and chore stats
     */
    private void updateStats(){
        // expense stats
        List<Expense> paymentList = getExpenseCollection().getMyPendingPayments();
        if(paymentList != null){
            tvPendingPaymentsNum.setText("" + paymentList.size());
        }

        List<Expense> requestList = getExpenseCollection().getMyPendingRequests();
        if(requestList != null){
            tvPendingRequestsNum.setText("" + requestList.size());
        }

        // chores today stats
        List<Chore> choresToday = getChoreCollection().getMyChoresToday();
        if(choresToday != null){
            int numHigh = 0;
            int numMed = 0;
            int numLow = 0;
            for(int i=0; i<choresToday.size(); i++){
                if(choresToday.get(i).getPriority().equals(Chore.PRIORITY_HIGH)){
                    numHigh++;
                }
                else if(choresToday.get(i).getPriority().equals(Chore.PRIORITY_MED)){
                    numMed++;
                }
                else if(choresToday.get(i).getPriority().equals(Chore.PRIORITY_LOW)){
                    numLow++;
                }
            }
            tvHighNum.setText(numHigh + "");
            tvMedNum.setText(numMed + "");
            tvLowNum.setText(numLow + "");
        }
    }
}