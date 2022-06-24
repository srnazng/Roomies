package com.example.roomies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.R;
import com.example.roomies.model.Chore;

import java.util.List;

public class ChoreAdapter extends
        RecyclerView.Adapter<ChoreAdapter.ViewHolder> {

    private List<Chore> chores;

    // Pass in the chore array into the constructor
    public ChoreAdapter(List<Chore> chores) {
        this.chores = chores;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
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
        return chores.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView tvTitle;
        public TextView tvDescription;
        public TextView tvDue;
        public com.google.android.material.card.MaterialCardView card;
        public Button messageButton;

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
            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    card.setChecked(!card.isChecked());
                    return true;
                }
            });
        }

        public void bind(Chore chore){
            tvTitle.setText(chore.getTitle());
            tvDescription.setText(chore.getDescription());
            tvDue.setText(chore.getDue().toString());
        }
    }
}
