package com.example.roomies;
import static com.example.roomies.HomeFragment.*;
import static com.example.roomies.model.Recurrence.*;
import static com.example.roomies.utils.Utils.convertFromMilitaryTime;
import static com.example.roomies.utils.Utils.getMonthForInt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.roomies.model.Chore;
import com.example.roomies.model.ChoreAssignment;
import com.example.roomies.model.Recurrence;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddChoreActivity extends AppCompatActivity implements CustomRecurrenceFragment.OnInputListener {
    private EditText etChoreName;
    private EditText etChoreDescription;
    private Switch switchAllDay;
    private RadioGroup radioPriority;
    private EditText etPoints;
    private static TextView tvTime;
    private static TextView tvDate;
    private ChipGroup chipUsers;
    private Button btnAdd;
    private ImageView ivRepeat;
    private TextView tvRepeat;

    private List<ParseUser> assignedUsers;
    private static Calendar date;
    private Chore chore;

    // from custom recurrence fragment
    private Recurrence recurrence;
    Calendar endDate;
    Integer numOccurrences;

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
        date = Calendar.getInstance();

        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        // set custom recurrence
        recurrence = null; // no recurrence by default
        ivRepeat = findViewById(R.id.ivRepeat);
        tvRepeat = findViewById(R.id.tvRepeat);

        ivRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatDialog();
            }
        });

        tvRepeat = findViewById(R.id.tvRepeat);
        tvRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatDialog();
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
                if(recurrence == null){
                    addChore();
                }
                else{
                    addRecurrence();
                }
            }
        });
    }

    // create chore object and add to database
    public void addChore(){
        // check a user has been assigned to chore
        if(assignedUsers.size() < 1){
            Toast.makeText(this, "No one assigned to chore", Toast.LENGTH_SHORT).show();
            return;
        }

        Chore entity = new Chore();

        entity.put("circle", currentCircle);
        entity.put("creator", ParseUser.getCurrentUser());
        entity.put("title", etChoreName.getText().toString());
        entity.put("description", etChoreDescription.getText().toString());
        entity.put("points", Integer.parseInt(etPoints.getText().toString()));
        entity.put("dueDatetime", date.getTime());
        entity.put("allDay", switchAllDay.isChecked());
        entity.setRecurrence(recurrence);

        // get priority from radio group
        int radioButtonID = radioPriority.getCheckedRadioButtonId();
        View radioButton = radioPriority.findViewById(radioButtonID);
        int idx = radioPriority.indexOfChild(radioButton);
        RadioButton r = (RadioButton) radioPriority.getChildAt(idx);
        String selectedPriority = r.getText().toString();
        entity.put("priority", selectedPriority);

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
            ChoreAssignment entity = new ChoreAssignment();

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
        //done
        Toast.makeText(this, "Chore added success", Toast.LENGTH_SHORT).show();
        finish();
    }

    // create Recurrence object
    public void addRecurrence() {
        Date d = getEndDate();
        if(d != null){
            recurrence.setEndDate(getEndDate());
        }

        if(numOccurrences == null){
            recurrence.setNumOccurrences(-1);
        }
        else{
            recurrence.setNumOccurrences(numOccurrences);
        }

        // Saves the new object.
        // Notice that the SaveCallback is totally optional!
        recurrence.saveInBackground(e -> {
            if (e==null){
                //Save was done
                addChore();
            }else{
                //Something went wrong
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // end date is last day of occurrence
    public Date getEndDate() {
        if(endDate == null && numOccurrences == null){
            // never end
            endDate = date;
            endDate.set(Calendar.YEAR, date.get(Calendar.YEAR) + 100);
        }
        else if(endDate == null){
            // after number of occurrences
            endDate = date;
            if(recurrence.getFrequencyType().equals(TYPE_DAY)){
                // add numOccurrence days to first due date
                endDate.add(Calendar.DAY_OF_MONTH, (numOccurrences - 1) * recurrence.getFrequency());
            }
            else if(recurrence.getFrequencyType().equals(TYPE_WEEK)){
                // find last day of week chore occurs on
                List<DaysOfWeek> days = new ArrayList<>();
                String daysList = recurrence.getDaysOfWeek();
                if (daysList != null) {
                    for (int i=0;i<daysList.length();i++){
                        if(daysList.charAt(i) != ','){
                            DaysOfWeek day = DaysOfWeek.values()[Integer.parseInt(String.valueOf(daysList.charAt(i)))];
                            days.add(day);
                        }
                    }
                    Log.e(TAG, "days list: " + days);
                }

                if(!days.isEmpty()){
                    DaysOfWeek last = days.get(0);
                    if(days != null && days.size() > 0){
                        for(int i=0; i<days.size(); i++){
                            if(days.get(i).compareTo(last) > 0){
                                last = days.get(i);
                            }
                        }
                        if(DaysOfWeek.values()[date.get(Calendar.DAY_OF_WEEK) - 1].compareTo(last) < 0){
                            int diff = last.ordinal() - DaysOfWeek.values()[date.get(Calendar.DAY_OF_WEEK) - 1].ordinal();
//                            endDate.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH) + diff);
                            endDate.add(Calendar.DAY_OF_YEAR, diff);
                            Log.e(TAG, "diff " + diff);
                        }
                    }

                    Log.e(TAG, "last: " + last.ordinal());
                    Log.e(TAG, "end on " + endDate.toString());
                }

                // find last occurrence based last day of week
                endDate.add(Calendar.WEEK_OF_YEAR, (numOccurrences - 1) * recurrence.getFrequency());
            }
            else if(recurrence.getFrequencyType().equals(TYPE_MONTH)){
                endDate.add(Calendar.MONTH, (numOccurrences - 1) * recurrence.getFrequency());
            }
            else if(recurrence.getFrequencyType().equals(TYPE_YEAR)){
                endDate.add(Calendar.YEAR, (numOccurrences - 1) * recurrence.getFrequency());
            }
        }
        return endDate.getTime();
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

    // recurrence dialog
    private void showRepeatDialog() {
        FragmentManager fm = getSupportFragmentManager();
        CustomRecurrenceFragment customRecurrenceFragment = CustomRecurrenceFragment.newInstance();
        customRecurrenceFragment.show(fm, "fragment_custom_recurrence");
    }

    // get recurrence information from dialog
    @Override
    public void sendInput(Recurrence r, Calendar endDate, Integer numOccurrences) {
        this.recurrence = r;
        this.endDate = endDate;
        this.numOccurrences = numOccurrences;
        getRepeatMessage();
    }

    // set text of repeat button
    public void getRepeatMessage(){
        if(recurrence == null){
            tvRepeat.setText("Does not repeat");
            return;
        }

        String message = "Repeats every ";
        if(recurrence.getFrequency() > 1){
            message = message + recurrence.getFrequency() + " ";
        }

        message = message + recurrence.getFrequencyType();

        if(recurrence.getFrequency() > 1){
            message = message + "s";
        }

        if(endDate != null){
            int month = endDate.get(Calendar.MONTH);
            int day = endDate.get(Calendar.DAY_OF_MONTH);
            int year = endDate.get(Calendar.YEAR);
            message = message + " until " + getMonthForInt(month) + " " + day + ", " + year;
        }
        else if(numOccurrences != null){
            message = message + " until " + numOccurrences + " occurrences";
        }

        tvRepeat.setText(message);
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
            date.set(Calendar.HOUR_OF_DAY, hourOfDay);
            date.set(Calendar.MINUTE, minute);
            date.set(Calendar.SECOND, 0);
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
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, month);
            date.set(Calendar.DAY_OF_MONTH, day);
            tvDate.setText(getMonthForInt(month) + " " + day + ", " + year);
        }
    }
}