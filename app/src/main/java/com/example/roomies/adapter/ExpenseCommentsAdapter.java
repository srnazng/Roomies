package com.example.roomies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.roomies.R;
import com.example.roomies.model.ExpenseComment;
import com.parse.ParseFile;

import java.util.List;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class ExpenseCommentsAdapter extends RecyclerView.Adapter<ExpenseCommentsAdapter.ViewHolder> {
    List<ExpenseComment> comments;
    Context context;

    public static final String TAG = "CommentsAdapter";

    public ExpenseCommentsAdapter(Context context, List<ExpenseComment> comments){
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseComment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvComment;
        private TextView tvUser;
        private ImageView ivCommentProfile;

        // Constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            super(itemView);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvUser = itemView.findViewById(R.id.tvUser);
            ivCommentProfile = itemView.findViewById(R.id.ivCommentProfile);
        }

        // bind ExpenseComment with comment item view
        public void bind(ExpenseComment comment){
            tvComment.setText(comment.getComment());
            tvUser.setText(comment.getUser().getString("name"));
            ParseFile profile =  comment.getUser().getParseFile("image");
            if (profile != null) {
                Glide.with(context).load(profile.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivCommentProfile);
            }
        }
    }
}