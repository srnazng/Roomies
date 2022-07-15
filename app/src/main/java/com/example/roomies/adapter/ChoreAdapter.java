package com.example.roomies.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.GoogleOauthActivity;
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
        }

        public void bind(Chore chore){
            this.chore = chore;
            tvTitle.setText(chore.getTitle());
            tvDescription.setText(chore.getDescription());
            tvDue.setText(formatDue(chore));
            card.setLongClickable(false);
            btnGoogleCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, GoogleOauthActivity.class);
                    i.putExtra("chore", chore);
                    context.startActivity(i);
                }
            });
        }

        public String formatDue(Chore chore){
            if(chore.getAllDay()){
                return "Due today";
            }

            Calendar time = Calendar.getInstance();
            time.setTime(chore.getDue());

            String due = "Due ";
            String minutes = time.get(Calendar.MINUTE) + "";
            if(time.get(Calendar.MINUTE) < 10) {
                minutes = "0" + minutes;
            }

            int hour = time.get(Calendar.HOUR_OF_DAY);

            if(hour > 12){
                hour -= 12;
                due = due + hour + ":" + minutes + " PM today";
            }
            else if(hour == 12){
                due = due + hour + ":" + minutes + " PM today";
            }
            else{
                due = due + hour + ":" + minutes + " AM today";
            }

            return due;
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
