package com.example.roomies;

import static com.example.roomies.model.CircleManager.getChoreCollection;
import static com.example.roomies.model.CircleManager.getCurrentCircle;
import static com.example.roomies.model.CircleManager.getUserCircleList;
import static com.example.roomies.utils.ChoreUtils.getEndDate;
import static com.example.roomies.utils.ChoreUtils.getRepeatMessage;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.roomies.model.Chore;
import com.example.roomies.model.ChoreAssignment;
import com.example.roomies.model.Recurrence;
import com.example.roomies.model.UserCircle;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EditChoreActivity extends AppCompatActivity implements CustomRecurrenceFragment.OnInputListener {
    private EditText etEditChoreName;
    private EditText etEditChoreDescription;
    private Switch switchEditAllDay;
    private Switch switchEditGoogleCalendar;
    private Switch switchEditInvite;
    private RadioGroup radioEditPriority;
    private RadioButton rbEditHigh;
    private RadioButton rbEditMed;
    private RadioButton rbEditLow;
    private EditText etEditDuration;
    private Spinner spEditDuration;
    private static TextView tvEditTime;
    private static TextView tvEditDate;
    private ChipGroup chipEditUsers;
    private Button btnUpdateChore;
    private ImageView ivEditRepeat;
    private TextView tvEditRepeat;

    private List<ChoreAssignment> originalChoreAssignments;
    private List<ParseUser> assignedUsers;
    private ArrayList<String> assignedEmails;
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
        setContentView(R.layout.activity_edit_chore);

        context = this;

        chore = getIntent().getParcelableExtra("chore");

        etEditChoreName = findViewById(R.id.etEditChoreName);
        etEditChoreName.setText(chore.getTitle());

        etEditChoreDescription = findViewById(R.id.etEditChoreDescription);
        etEditChoreDescription.setText(chore.getDescription());

        radioEditPriority = findViewById(R.id.radioEditPriority);
        rbEditHigh = findViewById(R.id.rbEditHigh);
        rbEditMed = findViewById(R.id.rbEditMed);
        rbEditLow = findViewById(R.id.rbEditLow);
        initRadio(chore);

        switchEditAllDay = findViewById(R.id.switchEditAllDay);
        switchEditAllDay.setChecked(chore.getAllDay());
        switchEditAllDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchEditAllDay.isChecked()){
                    // allow user to select time
                    Log.i(TAG, "set gone");
                    tvEditTime.setVisibility(View.GONE);
                }
                else{
                    // don't allow user to select time
                    Log.i(TAG, "set visible");
                    int hour = date.get(Calendar.HOUR_OF_DAY);
                    int minute = date.get(Calendar.MINUTE);
                    tvEditTime.setText(convertFromMilitaryTime(hour, minute));
                    tvEditTime.setVisibility(View.VISIBLE);
                }
            }
        });

        // get current time
        date = Calendar.getInstance();
        date.setTime(chore.getDue());
        int hour = date.get(Calendar.HOUR_OF_DAY);
        int minute = date.get(Calendar.MINUTE);
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);

        tvEditDate = findViewById(R.id.tvEditDate);
        tvEditDate.setText(getMonthForInt(month) + " " + day + ", " + year);
        tvEditDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new EditChoreActivity.DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        tvEditTime = findViewById(R.id.tvEditTime);
        tvEditTime.setVisibility(View.GONE);
        // timePicker dialog
        tvEditTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new EditChoreActivity.TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        if(!chore.getAllDay()){
            // set default time to current time
            tvEditTime.setText(convertFromMilitaryTime(hour, minute));
            tvEditTime.setVisibility(View.VISIBLE);
            tvEditDate.setVisibility(View.VISIBLE);
        }

        // set custom recurrence
        recurrence = chore.getRecurrence(); // no recurrence by default

        ivEditRepeat = findViewById(R.id.ivEditRepeat);
        tvEditRepeat = findViewById(R.id.tvEditRepeat);

        if(recurrence != null){
            endDate = Calendar.getInstance();
            endDate.setTime(recurrence.getEndDate());
            numOccurrences = recurrence.getNumOccurrences();
            tvEditRepeat.setText(getRepeatMessage(recurrence, endDate, numOccurrences));
        }

        ivEditRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatDialog();
            }
        });

        tvEditRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatDialog();
            }
        });

        // estimated time
        etEditDuration = findViewById(R.id.etEditDuration);
        spEditDuration = findViewById(R.id.spEditDuration);
        initDuration();

        // set plurality of frequency items
        etEditDuration.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(etEditDuration.getText().toString().equals("1")){
                    int pos = spEditDuration.getSelectedItemPosition();
                    ArrayAdapter<String> freqTypeAdapter = new ArrayAdapter<String>(EditChoreActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            getResources().getStringArray(R.array.duration_array));
                    spEditDuration.setAdapter(freqTypeAdapter);
                    spEditDuration.setSelection(pos);
                }
                else{
                    int pos = spEditDuration.getSelectedItemPosition();
                    ArrayAdapter<String> freqTypeAdapter = new ArrayAdapter<String>(EditChoreActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            getResources().getStringArray(R.array.duration_array_plural));
                    spEditDuration.setAdapter(freqTypeAdapter);
                    spEditDuration.setSelection(pos);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // add chore to Google Calendar
        switchEditGoogleCalendar = findViewById(R.id.switchEditGoogleCalendar);
        switchEditGoogleCalendar.setChecked(false);
        switchEditGoogleCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchEditGoogleCalendar.isChecked()){
                    switchEditInvite.setClickable(true);
                }
                else{
                    switchEditInvite.setClickable(false);
                    switchEditInvite.setChecked(false);
                }
            }
        });
        switchEditInvite = findViewById(R.id.switchEditInvite);
        switchEditInvite.setChecked(false);

        // ChipGroup of users to assign chore to
        chipEditUsers = findViewById(R.id.chipEditUsers);
        originalChoreAssignments = new ArrayList<>();
        assignedUsers = new ArrayList<>();
        assignedEmails = new ArrayList<>();
        initializeChips();

        btnUpdateChore = findViewById(R.id.btnUpdateChore);
        btnUpdateChore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recurrence != null){
                    addRecurrence();
                }
                else{
                    updateChore();
                }
            }
        });
    }

    // create Recurrence object
    public void addRecurrence() {
        Date d = getEndDate(endDate, numOccurrences, recurrence, date);
        if(d != null){
            recurrence.setEndDate(getEndDate(endDate, numOccurrences, recurrence, date));
        }

        if(numOccurrences == null){
            recurrence.setNumOccurrences(-1);
        }
        else{
            recurrence.setNumOccurrences(numOccurrences);
        }

        // Saves the new object.
        recurrence.saveInBackground(e -> {
            if (e==null){
                //Save was done
                updateChore();
            }else{
                //Something went wrong
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // set chips to all users assigned chore
    public void initializeChips(){
        List<UserCircle> userCircleList = getUserCircleList();
        List<ChoreAssignment> assignees = getChoreCollection().getAllChoreAssignments(chore);
        for(int i=0; i<assignees.size(); i++){
            originalChoreAssignments.add(assignees.get(i));
            assignedUsers.add(assignees.get(i).getUser());
            assignedEmails.add(assignees.get(i).getUser().getString("username"));
        }

        if(userCircleList != null){
            // loop through users in circle
            for(int i=0; i<userCircleList.size(); i++){
                Chip chip = new Chip(this);
                chip.setText(userCircleList.get(i).getUser().getString("name"));
                chip.setCheckable(true);
                chip.setChecked(isAssigned(userCircleList.get(i).getUser()));

                int userNum = i;
                chip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(chip.isChecked()){
                            // user selected to be assigned chore
                            assignedUsers.add(userCircleList.get(userNum).getUser());
                            String email = userCircleList.get(userNum).getUser().getString("username");

                            if(email != null && !email.isEmpty()){
                                assignedEmails.add(email);
                                Log.i(TAG, assignedEmails.toString());
                            }
                        }
                        else{
                            // user deselected to be assigned chore
                            assignedUsers.remove(userCircleList.get(userNum).getUser());
                            String email = userCircleList.get(userNum).getUser().getString("username");

                            if(email != null && !email.isEmpty()){
                                assignedEmails.removeIf(e -> e.equals(email));
                                Log.i(TAG, assignedEmails.toString());
                            }
                        }
                    }
                });
                chipEditUsers.addView(chip);
            }
        }
    }

    // create chore object and add to database
    public void updateChore(){
        // check a user has been assigned to chore
        if(assignedUsers.size() < 1){
            Toast.makeText(this, "No one assigned to chore", Toast.LENGTH_SHORT).show();
            return;
        }

        Chore entity = chore;

        entity.put("circle", getCurrentCircle());
        entity.put("lastEditedBy", ParseUser.getCurrentUser());
        entity.put("title", etEditChoreName.getText().toString());
        entity.put("description", etEditChoreDescription.getText().toString());

        int duration = Integer.parseInt(etEditDuration.getText().toString());
        if(spEditDuration.getSelectedItem().toString().equals("hours") ||
                spEditDuration.getSelectedItem().toString().equals("hour")){
            duration *= 60;
        }
        entity.put("duration", duration);

        entity.put("dueDatetime", date.getTime());
        entity.put("allDay", switchEditAllDay.isChecked());

        if(recurrence != null){
            entity.setRecurrence(recurrence);
        }

        // get priority from radio group
        int radioButtonID = radioEditPriority.getCheckedRadioButtonId();
        View radioButton = radioEditPriority.findViewById(radioButtonID);
        int idx = radioEditPriority.indexOfChild(radioButton);
        RadioButton r = (RadioButton) radioEditPriority.getChildAt(idx);
        String selectedPriority = r.getText().toString();
        entity.put("priority", selectedPriority);

        chore = entity;

        getChoreCollection().updateChore(entity, switchEditInvite.isChecked(),
                originalChoreAssignments, assignedUsers, assignedEmails, this);
    }

    public boolean isAssigned(ParseUser user){
        for(int i=0; i<assignedUsers.size(); i++){
            if(assignedUsers.get(i).getObjectId().equals(user.getObjectId())){
                return true;
            }
        }
        return false;
    }

    private void initDuration(){
        if(chore.getDuration() >= 60 && chore.getDuration() % 60 == 0){
            spEditDuration.setSelection(0);
            int time = chore.getDuration() / 60;
            etEditDuration.setText(time + "");
        }
    }

    // recurrence dialog
    private void showRepeatDialog() {
        FragmentManager fm = getSupportFragmentManager();
        CustomRecurrenceFragment customRecurrenceFragment = CustomRecurrenceFragment.newInstance(recurrence);
        customRecurrenceFragment.show(fm, "fragment_custom_recurrence");
    }

    private void initRadio(Chore chore){
        if(chore.getPriority().equals(Chore.PRIORITY_HIGH)){
            rbEditHigh.setChecked(true);
            rbEditLow.setChecked(false);
            rbEditMed.setChecked(false);
        }
        else if(chore.getPriority().equals(Chore.PRIORITY_MED)){
            rbEditHigh.setChecked(false);
            rbEditLow.setChecked(false);
            rbEditMed.setChecked(true);
        }
        else if(chore.getPriority().equals(Chore.PRIORITY_LOW)){
            rbEditHigh.setChecked(false);
            rbEditLow.setChecked(true);
            rbEditMed.setChecked(false);
        }
    }

    @Override
    public void sendInput(Recurrence r, Calendar endDate, Integer numOccurrences) {
        this.recurrence = r;
        this.endDate = endDate;
        this.numOccurrences = numOccurrences;
        tvEditRepeat.setText(getRepeatMessage(r, endDate, numOccurrences));
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
            tvEditTime.setText(time);
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
            tvEditDate.setText(getMonthForInt(month) + " " + day + ", " + year);
        }
    }
}