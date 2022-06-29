package com.example.roomies.adapter;

import static com.example.roomies.utils.Utils.calendarDayOfWeek;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.R;
import com.example.roomies.model.CalendarDay;
import com.example.roomies.model.Chore;
import com.google.android.material.chip.Chip;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarAdapter extends
    RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<CalendarDay> dayList;
    private Context context;

    public static final String TAG = "CalendarAdapter";

    public CalendarAdapter(List<CalendarDay> list){
        dayList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate layout for each calendar day
        View view = inflater.inflate(R.layout.calendar_day, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarDay calendarDay = dayList.get(position);
        holder.bind(calendarDay);
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDayOfWeek;
        private Chip chipDayOfMonth;
        private RecyclerView rvEvents;
        private EventAdapter adapter;
        private List<Chore> choreList;

        public ViewHolder(View itemView) {
            super(itemView);

            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            chipDayOfMonth = itemView.findViewById(R.id.chipDayOfMonth);

            // Lookup the recyclerview in activity layout
            rvEvents = itemView.findViewById(R.id.rvEvents);
            if(choreList == null){
                choreList = new ArrayList<>();
            }
        }

        public void bind(CalendarDay cal){
            choreList = cal.getChores();

            // Create adapter passing in the sample user data
            adapter = new EventAdapter(choreList);
            // Attach the adapter to the recyclerview to populate items
            rvEvents.setAdapter(adapter);
            // Set layout manager to position the items
            rvEvents.setLayoutManager(new LinearLayoutManager(context));

            tvDayOfWeek.setText(calendarDayOfWeek(cal.getDay()));
            chipDayOfMonth.setText(cal.getDay().get(Calendar.DAY_OF_MONTH) + "");

            Calendar today = Calendar.getInstance();
            Calendar day = cal.getDay();
            if(day.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    day.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    day.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)){
                chipDayOfMonth.setChipBackgroundColorResource(R.color.turquoise);
            }
            else{
                chipDayOfMonth.setChipBackgroundColorResource(R.color.white);
            }
        }
    }
}