package com.example.roomies.adapter;

import static com.example.roomies.utils.ChoreUtils.setPriorityColors;
import static com.example.roomies.utils.ChoreUtils.toDetail;
import static com.example.roomies.utils.Utils.formatDue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.R;
import com.example.roomies.model.Chore;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;
import java.util.List;

public class CompletedChoreAdapter extends
        RecyclerView.Adapter<CompletedChoreAdapter.ViewHolder> {

    private List<Chore> chores;
    private static Context context;

    // Pass in the chore array into the constructor
    public CompletedChoreAdapter(List<Chore> chores) {
        this.chores = chores;
    }

    @NonNull
    @Override
    public CompletedChoreAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View choreView = inflater.inflate(R.layout.item_completed_chore, parent, false);

        // Return a new holder instance
        CompletedChoreAdapter.ViewHolder viewHolder = new CompletedChoreAdapter.ViewHolder(choreView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CompletedChoreAdapter.ViewHolder holder, int position) {
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
        private TextView tvCompletedTitle;
        private TextView tvCompletedDue;
        private ImageView ivCompletedPriority;
        public static MaterialCardView completedCard;
        private static Chore chore;

        public static Chore getChore() { return chore; };

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tvCompletedTitle = itemView.findViewById(R.id.tvCompletedTitle);
            tvCompletedDue = itemView.findViewById(R.id.tvCompletedDue);
            completedCard = itemView.findViewById(R.id.completedCard);
            ivCompletedPriority = itemView.findViewById(R.id.ivCompletedPriority);
        }

        public void bind(Chore chore){
            this.chore = chore;

            // bind to layout
            tvCompletedTitle.setText(chore.getTitle());
            tvCompletedDue.setText(formatDue(chore, Calendar.getInstance()));
            completedCard.setLongClickable(false);
            completedCard.setChecked(true);
            completedCard.setCheckable(true);
            // go to detail page
            completedCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toDetail(context, chore, Calendar.getInstance());
                }
            });
            setPriorityColors(context, ivCompletedPriority, chore);
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
