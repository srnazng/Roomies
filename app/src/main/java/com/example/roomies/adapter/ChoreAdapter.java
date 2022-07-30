package com.example.roomies.adapter;

import static com.example.roomies.utils.ChoreUtils.setPriorityColors;
import static com.example.roomies.utils.ChoreUtils.toDetail;
import static com.example.roomies.utils.Utils.formatDue;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.GoogleSignInActivity;
import com.example.roomies.R;
import com.example.roomies.model.Chore;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;
import java.util.List;

public class ChoreAdapter extends
        RecyclerView.Adapter<ChoreAdapter.ViewHolder> {

    private List<Chore> chores;
    private static Context context;

    // Pass in the chore array into the constructor
    public ChoreAdapter(List<Chore> chores) {
        this.chores = chores;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View choreView = inflater.inflate(R.layout.item_chore, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(choreView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chore chore = chores.get(position);
        holder.bind(chore);
    }

    @Override
    public int getItemCount() {
        if(chores == null){
            return 0;
        }
        return chores.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvDue;
        public static MaterialCardView card;
        private Button messageButton;
        private Button btnGoogleCalendar;
        private static Chore chore;
        private ImageView ivPriorityCircle;

        public static Chore getChore() { return chore; };

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDue = itemView.findViewById(R.id.tvDue);
            card = itemView.findViewById(R.id.card);
            btnGoogleCalendar = itemView.findViewById(R.id.btnGoogleCalendar);
            ivPriorityCircle = itemView.findViewById(R.id.ivPriorityCircle);
        }

        public void bind(Chore c){
            this.chore = c;
            tvTitle.setText(c.getTitle());
            tvDescription.setText(c.getDescription());
            tvDue.setText(formatDue(c, Calendar.getInstance()));
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toDetail(context, c, Calendar.getInstance());
                }
            });
            card.setLongClickable(false);
            btnGoogleCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, GoogleSignInActivity.class);
                    i.putExtra("chore", c);
                    context.startActivity(i);
                }
            });

            setPriorityColors(context, ivPriorityCircle, c);
        }
    }

    // get chores list
    public List<Chore> getData() {
        return chores;
    }

    // remove from list
    public void removeItem(int position) {
        chores.remove(position);
        notifyItemRemoved(position);
    }

    // restore item to list
    public void restoreItem(Chore item, int position) {
        chores.add(position, item);
        notifyItemInserted(position);
    }
}
