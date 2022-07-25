package com.example.roomies;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseUser;

public class ProfileActivity extends AppCompatActivity {
    private ParseUser user;
    private TextView tvProfileName;
    private ImageView ivProfilePicture;
    private ImageView ivEmail;
    private ImageView ivVenmo;
    private ImageView ivCashApp;
    private TextView tvEmail;
    private TextView tvVenmo;
    private TextView tvCashApp;
    private TextView tvVenmoLabel;
    private TextView tvCashAppLabel;
    private String cashApp;
    private String venmo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // get user object
        user = getIntent().getParcelableExtra("user");

        // set layout
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileName.setText(user.getString("name"));

        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        Glide.with(ProfileActivity.this)
                .load(user.getParseFile("image").getUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfilePicture);

        tvEmail = findViewById(R.id.tvEmail);
        tvEmail.setText(user.getString("username"));
        ivEmail = findViewById(R.id.ivEmail);

        tvVenmo = findViewById(R.id.tvVenmo);
        ivVenmo = findViewById(R.id.ivVenmo);
        tvVenmoLabel = findViewById(R.id.tvVenmoLabel);

        tvCashApp = findViewById(R.id.tvCashApp);
        ivCashApp = findViewById(R.id.ivCashApp);
        tvCashAppLabel = findViewById(R.id.tvCashAppLabel);

        setButtons();
    }

    public void setButtons() {
        venmo = user.getString("venmo");
        View.OnClickListener venmoOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://account.venmo.com/u/" + venmo);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        };

        if (venmo == null || venmo.isEmpty()) {
            tvVenmo.setVisibility(View.GONE);
            tvVenmoLabel.setVisibility(View.GONE);
            ivVenmo.setVisibility(View.GONE);
        } else {
            tvVenmo.setVisibility(View.VISIBLE);
            tvVenmoLabel.setVisibility(View.VISIBLE);
            ivVenmo.setVisibility(View.VISIBLE);
            tvVenmo.setText(venmo);

            tvVenmo.setOnClickListener(venmoOnClick);
            ivVenmo.setOnClickListener(venmoOnClick);
        }

        View.OnClickListener cashAppOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://cash.app/" + cashApp);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        };

        cashApp = user.getString("cashApp");
        if (cashApp == null || cashApp.isEmpty()) {
            tvCashApp.setVisibility(View.GONE);
            tvCashAppLabel.setVisibility(View.GONE);
            ivCashApp.setVisibility(View.GONE);
        } else {
            tvCashApp.setVisibility(View.VISIBLE);
            tvCashAppLabel.setVisibility(View.VISIBLE);
            ivCashApp.setVisibility(View.VISIBLE);
            tvCashApp.setText(cashApp);

            tvCashApp.setOnClickListener(cashAppOnClick);
            ivCashApp.setOnClickListener(cashAppOnClick);
        }

        View.OnClickListener emailOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open email app
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                // set up email template
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{user.getString("username")});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Roomies Message");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hi " + user.getString("name") + "!\n");
                startActivity(Intent.createChooser(emailIntent, "Send email"));
            }
        };

        ivEmail.setOnClickListener(emailOnClick);
        tvEmail.setOnClickListener(emailOnClick);
    }
}