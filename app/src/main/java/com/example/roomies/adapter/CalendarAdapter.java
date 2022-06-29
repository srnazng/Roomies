package com.example.roomies.adapter;

import static com.example.roomies.utils.Utils.calendarDayOfWeek;
import static com.example.roomies.utils.Utils.getMonthForInt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.R;
import com.example.roomies.model.CalendarDay;
import com.google.android.material.chip.Chip;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

public class CalendarAdapter extends
    RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<CalendarDay> dayList;

    public CalendarAdapter(List<CalendarDay> list){
        dayList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate layout for each calendar day
        View contactView = inflater.inflate(R.layout.calendar_day, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
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
        public TextView tvDayOfWeek;
        public Chip chipDayOfMonth;

        public ViewHolder(View itemView) {
            super(itemView);

            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            chipDayOfMonth = itemView.findViewById(R.id.chipDayOfMonth);
        }

        public void bind(CalendarDay cal){
            tvDayOfWeek.setText(calendarDayOfWeek(cal.getDay()));
            chipDayOfMonth.setText(cal.getDay().get(Calendar.DAY_OF_MONTH) + "");
        }
    }
}