package com.example.roomies.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.R;
import com.example.roomies.model.Chore;

import java.util.Calendar;
import java.util.List;

public class EventAdapter extends
        RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Chore> choreList;
    private Context context;

    public static final String TAG = "EventAdapter";

    public EventAdapter(List<Chore> list){
        choreList = list;
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate layout for each calendar event
        View view = inflater.inflate(R.layout.calendar_event, parent, false);

        // Return a new holder instance
        EventAdapter.ViewHolder viewHolder = new EventAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
        Chore chore = choreList.get(position);
        holder.bind(chore);
    }

    @Override
    public int getItemCount() {
        return choreList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPriority;
        private TextView tvChore;
        private TextView tvTimeDue;

        public ViewHolder(View itemView) {
            super(itemView);

            ivPriority = itemView.findViewById(R.id.ivPriority);
            tvChore = itemView.findViewById(R.id.tvChore);
            tvTimeDue = itemView.findViewById(R.id.tvTimeDue);
        }

        public void bind(Chore chore){
            // set color based on priority
            if(chore.getPriority().equals(Chore.PRIORITY_HIGH)){
                ivPriority.setColorFilter(ContextCompat.getColor(context, R.color.orange), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            else if(chore.getPriority().equals(Chore.PRIORITY_MED)){
                ivPriority.setColorFilter(ContextCompat.getColor(context, R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            else{
                ivPriority.setColorFilter(ContextCompat.getColor(context, R.color.green), android.graphics.PorterDuff.Mode.SRC_IN);
            }

            // set chore name
            tvChore.setText(chore.getTitle());

            // set chore time
            if(!chore.getAllDay()){
                Calendar c = Calendar.getInstance();
                c.setTime(chore.getDue());

                int hour = c.get(Calendar.HOUR_OF_DAY);
                if(hour > 12) { hour -= 12; }

                String minute = "00";
                if(c.get(Calendar.MINUTE) < 10){
                    minute = "0" + c.get(Calendar.MINUTE);
                }

                String AM_PM = "AM";
                if( c.get(Calendar.AM_PM) == 1) { AM_PM = "PM"; }

                tvTimeDue.setText(hour + ":" + minute + " " + AM_PM);
            }
            else{
                tvTimeDue.setText("");
            }
        }
    }
}
