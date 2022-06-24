package com.example.roomies;
import static com.example.roomies.HomeFragment.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.roomies.model.Chore;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddChoreActivity extends AppCompatActivity {
    private EditText etChoreName;
    private EditText etChoreDescription;
    private Switch switchAllDay;
    private RadioGroup radioPriority;
    private EditText etPoints;
    private static TextView tvTime;
    private static TextView tvDate;
    private ChipGroup chipUsers;
    private Button btnAdd;

    private List<ParseUser> assignedUsers;
    private static Date date;
    private Chore chore;

    private static Context context;
    public static final String TAG = "AddChoreActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chore);

        context = AddChoreActivity.this;

        // bind with layout
        etChoreName = findViewById(R.id.etChoreName);
        etChoreDescription = findViewById(R.id.etChoreDescription);
        radioPriority = findViewById(R.id.radioPriority);
        etPoints = findViewById(R.id.etPoints);

        // get current time
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // set default time to current time
        tvTime = findViewById(R.id.tvTime);
        tvTime.setText(convertFromMilitaryTime(hour, minute));
        tvTime.setVisibility(View.GONE);

        // timePicker dialog
        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        // all day switch
        switchAllDay = findViewById(R.id.switchAllDay);
        switchAllDay.setChecked(true);
        switchAllDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchAllDay.isChecked()){
                    // allow user to select time
                    tvTime.setVisibility(View.GONE);
                }
                else{
                    // don't allow user to select time
                    tvTime.setVisibility(View.VISIBLE);
                }
            }
        });

        // datePicker dialog
        tvDate = findViewById(R.id.tvDate);
        tvDate.setText(getMonthForInt(month) + " " + day + ", " + year);
        date = new Date();

        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        // ChipGroup of users to assign chore to
        chipUsers = findViewById(R.id.chipUsers);
        assignedUsers = new ArrayList<>();
        initializeChips();

        // submit chore
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChore();
            }
        });
    }

    // create chore object and add to database
    public void addChore(){
        // check a user has been assigned to chore
        if(assignedUsers.size() < 1){
            Toast.makeText(this, "No one assigned to chore", Toast.LENGTH_SHORT).show();
        }

        Chore entity = new Chore();

        entity.put("circle", currentCircle);
        entity.put("creator", ParseUser.getCurrentUser());
        entity.put("title", etChoreName.getText().toString());
        entity.put("description", etChoreDescription.getText().toString());
        entity.put("points", Integer.parseInt(etPoints.getText().toString()));
        entity.put("dueDate", date);

        // get priority from radio group
        int radioButtonID = radioPriority.getCheckedRadioButtonId();
        View radioButton = radioPriority.findViewById(radioButtonID);
        int idx = radioPriority.indexOfChild(radioButton);
        RadioButton r = (RadioButton) radioPriority.getChildAt(idx);
        String selectedPriority = r.getText().toString();
        entity.put("priority", selectedPriority);

        // only include time if not all day
        if(switchAllDay.isChecked()){
            entity.put("time", tvTime.getText());
        }

        chore = entity;

        // Saves the new object.
        entity.saveInBackground(e -> {
            if (e==null){
                //Save was done
                assignChores();
            }else{
                //Something went wrong
                Toast.makeText(this, "Could not add chore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // create ChoreAssignment object for each user assigned chore and add to database
    public void assignChores(){
        // loop through all assigned users
        for(int i=0; i<assignedUsers.size(); i++){
            ParseObject entity = new ParseObject("ChoreAssignment");

            entity.put("user", assignedUsers.get(i));
            entity.put("chore", chore);

            // Saves the new object.
            entity.saveInBackground(e -> {
                if (e==null){
                    //Save was done
                }else{
                    //Something went wrong
                    Toast.makeText(this, "Error assigning chore", Toast.LENGTH_SHORT).show();
                    return;
                }
            });
        }
        Toast.makeText(this, "Chore added success", Toast.LENGTH_SHORT).show();
        finish();
    }

    // create chips for assigning chore to users
    public void initializeChips(){

        if(userCircleList != null){
            // loop through users in circle
            for(int i=0; i<userCircleList.size(); i++){
                Chip chip = new Chip(this);
                chip.setText(userCircleList.get(i).getUser().getString("name"));
                chip.setCheckable(true);

                int userNum = i;
                chip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(chip.isChecked()){
                            // user selected to be assigned chore
                            assignedUsers.add(userCircleList.get(userNum).getUser());
                        }
                        else{
                            // user deselected to be assigned chore
                            assignedUsers.remove(userCircleList.get(userNum).getUser());
                        }
                    }
                });
                chipUsers.addView(chip);
            }
        }
    }

    // time picker dialog
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(context, this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        // set time text
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Time has been chosen by the user
            String time = convertFromMilitaryTime(hourOfDay, minute);
            date.setHours(hourOfDay);
            date.setMinutes(minute);
            date.setSeconds(0);
            tvTime.setText(time);
        }
    }

    // date picker dialog
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(requireContext(), this, year, month, day);
        }

        // set date text
        public void onDateSet(DatePicker view, int year, int month, int day) {
            date.setYear(year);
            date.setMonth(month - 1);
            date.setDate(day);
            tvDate.setText(getMonthForInt(month) + " " + day + ", " + year);
        }
    }

    // 24 hour time to 12 hour time with AM or PM
    public static String convertFromMilitaryTime(int hourOfDay, int minute){
        return ((hourOfDay > 12) ? hourOfDay % 12 : hourOfDay) + ":" + (minute < 10 ? ("0" + minute) : minute) + " " + ((hourOfDay >= 12) ? "PM" : "AM");
    }

    // get name of month from number
    public static String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11) {
            month = months[num];
        }
        return month;
    }
}