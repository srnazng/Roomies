package com.example.roomies;
import static com.example.roomies.model.Recurrence.*;
import static com.example.roomies.utils.Utils.getMonthForInt;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.roomies.model.Recurrence;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomRecurrenceFragment extends DialogFragment {
    private Button btnCancelRecurrence;
    private Button btnDone;
    private EditText etNumber;
    private Spinner spFrequency;
    private androidx.constraintlayout.widget.ConstraintLayout layoutWeek;
    private ChipGroup cgDaysOfWeek;
    //  private androidx.constraintlayout.widget.ConstraintLayout layoutMonth;

    // radio group elements
    private RadioButton rbNever;
    private TextView tvNever;
    private RadioButton rbDate;
    private TextView tvOn;
    private static TextView tvEndDate;
    private RadioButton rbOccurrences;
    private EditText etNumOccurrences;
    private TextView tvAfter;
    private TextView tvOccurrence;

    // to pass back to activity
    private static Calendar endDate;
    private static Integer numOccurrences;
    private List<DaysOfWeek> daysOfWeek;

    public static final String TAG = "CustomRecurrenceFragment";

    public CustomRecurrenceFragment() {
        // Empty constructor is required for DialogFragment
    }

    // listener for activity
    public interface OnInputListener {
        void sendInput(Recurrence r, Calendar endDate, Integer numOccurrences);
    }

    public OnInputListener dialogListener;

    // create new dialog
    public static CustomRecurrenceFragment newInstance() {
        CustomRecurrenceFragment frag = new CustomRecurrenceFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_recurrence, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        etNumber = view.findViewById(R.id.etNumber);

        // Show soft keyboard automatically and request focus to field
        etNumber.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // set plurality of frequency items
        etNumber.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(etNumber.getText().toString().equals("1")){
                    int pos = spFrequency.getSelectedItemPosition();
                    ArrayAdapter<String> freqTypeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,
                            getResources().getStringArray(R.array.frequency_array));
                    spFrequency.setAdapter(freqTypeAdapter);
                    spFrequency.setSelection(pos);
                }
                else{
                    int pos = spFrequency.getSelectedItemPosition();
                    ArrayAdapter<String> freqTypeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,
                            getResources().getStringArray(R.array.frequency_array_plural));
                    spFrequency.setAdapter(freqTypeAdapter);
                    spFrequency.setSelection(pos);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // cancel button
        btnCancelRecurrence = view.findViewById(R.id.btnCancelRecurrence);
        btnCancelRecurrence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });

        // done button
        btnDone = view.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRecurrence();
                dismiss();
            }
        });

        // Specify end date of recurrence
        setRadioGroup(view);

        // show extra fields for certain frequency types
//        layoutMonth = view.findViewById(R.id.layoutMonth);
        layoutWeek = view.findViewById(R.id.layoutWeek);
        spFrequency = view.findViewById(R.id.spFrequency);
        spFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String freqType = spFrequency.getSelectedItem().toString();
                if(freqType.equals("week") || freqType.equals("weeks")){
                    layoutWeek.setVisibility(View.VISIBLE);
                }
                else{
                    layoutWeek.setVisibility(View.GONE);
                }
                /**
                if(freqType.equals("month") || freqType.equals("months")){
                    layoutMonth.setVisibility(View.VISIBLE);
                }
                else{
                    layoutMonth.setVisibility(View.GONE);
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                String freqType = spFrequency.getSelectedItem().toString();
                if(freqType.equals("week") || freqType.equals("weeks")){
                    layoutWeek.setVisibility(View.VISIBLE);
                }
                else{
                    layoutWeek.setVisibility(View.GONE);
                }

                /**
                if(freqType.equals("month") || freqType.equals("months")){
                    layoutMonth.setVisibility(View.VISIBLE);
                }
                else{
                    layoutMonth.setVisibility(View.GONE);
                }
                 */
            }
        });

        // select days of week to repeat if frequency type is weekly
        cgDaysOfWeek = view.findViewById(R.id.cgDaysOfWeek);
        daysOfWeek = new ArrayList<>();
        // initialize chips
        for(int i=0; i<cgDaysOfWeek.getChildCount(); i++){
            Chip chip = (Chip)cgDaysOfWeek.getChildAt(i);
            final int index = i;

            // selecting chips adds to list of days of week
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(chip.isChecked()){
                        daysOfWeek.add(DaysOfWeek.values()[index]);
                    }
                    else{
                        daysOfWeek.remove(DaysOfWeek.values()[index]);
                    }
                }
            });
        }
    }

    @Override public void onAttach(Context context)
    {
        super.onAttach(context);
        try {
            dialogListener
                    = (OnInputListener)getActivity();
        }
        catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: "
                    + e.getMessage());
        }
    }

    // create Recurrence object
    public void submitRecurrence(){
        Recurrence recurrence = new Recurrence();
        recurrence.setFrequency(Integer.parseInt(etNumber.getText().toString()));

        String freq = spFrequency.getSelectedItem().toString();
        String freqType = TYPE_DAY;
        if (freq.equals("week") || freq.equals("weeks")){
            freqType = TYPE_WEEK;
            String days = "";
            for(int i=0; i<daysOfWeek.size(); i++){
                days = days + daysOfWeek.get(i).ordinal() + ",";
            }
            recurrence.setDaysOfWeek(days);
        }
        else if(freq.equals("month") || freq.equals("months")){
            freqType = TYPE_MONTH;
        }
        else if(freq.equals("year") || freq.equals("years")){
            freqType = TYPE_YEAR;
        }
        recurrence.setFrequencyType(freqType);

        // actual end date determined in AddChoreActivity in case due date changes
        calculateEndDate();
        dialogListener.sendInput(recurrence, endDate, numOccurrences);
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
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            endDate.set(Calendar.YEAR, year);
            endDate.set(Calendar.MONTH, month);
            endDate.set(Calendar.DAY_OF_MONTH, day);
            tvEndDate.setText(getMonthForInt(month) + " " + day + ", " + year);
        }
    }

    // calculate end date
    public Calendar calculateEndDate(){
        Calendar c = Calendar.getInstance();
        if(rbNever.isChecked()){
            endDate = null;
            numOccurrences = null;
        }
        else if(rbDate.isChecked()){
            numOccurrences = null;
        }
        else if(rbOccurrences.isChecked()){
            endDate = null;
            numOccurrences = Integer.parseInt(etNumOccurrences.getText().toString());
        }
        return c;
    }

    // set up radio group for determining end of recurrence
    private void setRadioGroup(View view){

        // never repeats
        rbNever = view.findViewById(R.id.rbNever);
        tvNever = view.findViewById(R.id.tvNever);
        tvNever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbNever.setChecked(!rbNever.isChecked());
            }
        });

        // repeats on day
        rbDate = view.findViewById(R.id.rbDate);
        tvOn = view.findViewById(R.id.tvOn);
        tvOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbDate.setChecked(!rbDate.isChecked());
            }
        });
        endDate = Calendar.getInstance();
        tvEndDate = view.findViewById(R.id.tvEndDate);
        // get current time
        int year = endDate.get(Calendar.YEAR);
        int month = endDate.get(Calendar.MONTH);
        int day = endDate.get(Calendar.DAY_OF_MONTH);
        tvEndDate.setText(getMonthForInt(month) + " " + day + ", " + year);
        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbDate.setChecked(true);
                DialogFragment newFragment = new CustomRecurrenceFragment.DatePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        // repeats until a certain number of occurrences
        rbOccurrences = view.findViewById(R.id.rbOccurrences);
        etNumOccurrences = view.findViewById(R.id.etNumOccurrences);
        etNumOccurrences.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                rbOccurrences.setChecked(true);
                return false;
            }
        });
        tvAfter = view.findViewById(R.id.tvAfter);
        tvAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbOccurrences.setChecked(!rbOccurrences.isChecked());
            }
        });
        tvOccurrence = view.findViewById(R.id.tvOccurrence);
        tvOccurrence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbOccurrences.setChecked(!rbOccurrences.isChecked());
            }
        });
    }
}