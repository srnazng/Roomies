package com.example.roomies;

import static com.example.roomies.utils.CircleUtils.*;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomies.model.Circle;

/**
 * Settings page
 */
public class SettingsFragment extends Fragment {
    private Button btnLogout;
    private Button btnManageAccount;
    private Button btnManageCircle;
    private Button btnLeaveCircle;
    private TextView tvJoinCode;
    private ImageView ivClipboard;

    public static final String TAG = "SettingsFragment";

    public SettingsFragment() {

    }

    /**
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
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
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);

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
        tvJoinCode.setText(getCurrentCircle().getObjectId());

        // copy join code to clipboard
        ivClipboard = view.findViewById(R.id.ivClipboard);
        ivClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboard(getActivity(), getCurrentCircle().getObjectId());
            }
        });

        // logout button
        btnLogout = view.findViewById(R.id.btnLogOut);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(getActivity());
            }
        });

        // button to leave circle
        btnLeaveCircle = view.findViewById(R.id.btnLeaveCircle);
        btnLeaveCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveCircle(getActivity());
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initCircle(false);
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
        fragmentTransaction.replace(R.id.frame, ManageCircleFragment.newInstance(getCurrentCircle()));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}