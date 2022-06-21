package com.example.roomies;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomies.model.Circle;
import com.example.roomies.model.UserCircle;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    private Button btnLogout;
    private Button btnManageAccount;
    private Button btnManageCircle;
    private TextView tvJoinCode;
    private Circle circle;
    private ImageView ivClipboard;

    public static final String TAG = "SettingsFragment";

    public SettingsFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance(Circle circle) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable("circle", circle);
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
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);

        Bundle bundle = this.getArguments();
        circle = bundle.getParcelable("circle");

        // button to edit account settings
        btnManageAccount = view.findViewById(R.id.btnManageAccount);
        btnManageAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toManageAccount();
            }
        });

        // button to edit circle settings
        btnManageCircle = view.findViewById(R.id.btnManageCircle);
        btnManageCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toManageCircle();
            }
        });

        // join code to share circle
        tvJoinCode = view.findViewById(R.id.tvJoinCode);
        tvJoinCode.setText(circle.getObjectId());

        // copy join code to clipboard
        ivClipboard = view.findViewById(R.id.ivClipboard);
        ivClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboard(getActivity(), circle.getObjectId());
            }
        });

        // logout button
        btnLogout = view.findViewById(R.id.btnLogOut);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(v);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getCircle();
    }

    public void logout(View v){
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null

        if(currentUser == null){
            Intent i = new Intent(getActivity(), LoginActivity.class);
            startActivity(i);
        }
    }

    private void setClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copied text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard " + text, Toast.LENGTH_SHORT).show();
    }

    // go to manage account page
    public void toManageAccount() {
        FragmentTransaction fragmentTransaction = (getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, ManageAccountFragment.newInstance());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // go to manage account page
    public void toManageCircle() {
        FragmentTransaction fragmentTransaction = (getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, ManageCircleFragment.newInstance(circle));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // query UserCircle objects that contain current user to get circles that user has joined
    public void getCircle(){
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
                    Intent i = new Intent(getActivity(), AddCircleActivity.class);
                    startActivity(i);
                    getActivity().finish();
                }

                // save received posts to list and notify adapter of new data
                circle = userCircles.get(0).getCircle();
            }
        });
    }
}