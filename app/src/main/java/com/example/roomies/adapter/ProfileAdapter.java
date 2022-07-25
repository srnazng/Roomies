package com.example.roomies.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.roomies.ProfileActivity;
import com.example.roomies.R;
import com.parse.ParseUser;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    List<ParseUser> profileList;
    Context context;

    public ProfileAdapter(Context context, List<ParseUser> list){
        this.context = context;
        profileList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_profile, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser user = profileList.get(position);
        // bind user with list item
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName;
        private ImageView ivUserProfile;
        private ConstraintLayout profileItem;

        public ViewHolder(View itemView) {
            super(itemView);

            // get layout items
            tvUserName =  itemView.findViewById(R.id.tvUserName);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            profileItem = itemView.findViewById(R.id.profileItem);
        }

        public void bind(ParseUser user){
            // add user info
            tvUserName.setText(user.getString("name"));
            Glide.with(context)
                    .load(user.getParseFile("image").getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivUserProfile);
            profileItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ProfileActivity.class);
                    i.putExtra("user", user);
                    ((Activity) context).startActivity(i);
                }
            });
        }
    }
}
