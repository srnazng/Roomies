package com.example.roomies.utils;

import static com.example.roomies.model.CircleManager.getCurrentCircle;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.example.roomies.model.Chore;
import com.example.roomies.model.Recurrence;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class GoogleCalendar {
    private static Calendar service;
    private static GoogleAccountCredential credential;
    final static HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final static JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    public static final String TAG = "GoogleCalendar";

    public static final List<String> daySymbols = Arrays.asList("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa");

    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    /**
     * Create Google Calendar event for chore
     * @param activity
     * @param account
     * @param chore
     */
    public static void createEvent(Activity activity,
                                   GoogleSignInAccount account,
                                   Chore chore,
                                   ArrayList<String> emails){

        Log.i(TAG, "Create Google Calendar event");

        credential = GoogleAccountCredential.usingOAuth2(
                        activity, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(account.getEmail());

        // build calendar
        service = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Roomies")
                .build();

        // create calendar event
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // create event
                    String description = "";
                    if(chore.getDescription() != null && !chore.getDescription().isEmpty()){
                        description = description + chore.getDescription();
                    }
                    if(chore.getPriority() != null){
                        description = description + "\nPriority: " + chore.getPriority();
                    }
                    if(emails != null && !emails.isEmpty()){
                        description = description + "\nAssigned to:\n";
                        for(int i=0; i<emails.size(); i++){
                            description = description + emails.get(i) + "\n";
                        }
                    }

                    final Event event = new Event()
                            .setSummary(chore.getTitle() != null ? chore.getTitle() : "")
                            .setLocation(getCurrentCircle() != null ? getCurrentCircle().getName() : "")
                            .setDescription(description);

                    // all day event
                    if(chore.getAllDay()){
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date due = new Date(chore.getDue().toString());
                        String startDateStr = dateFormat.format(new Date(due.getTime()));
                        String endDateStr = dateFormat.format(new Date(due.getTime() + 86400000));

                        DateTime startDateTime = new DateTime(startDateStr);
                        DateTime endDateTime = new DateTime(endDateStr);

                        // Must use the setDate() method for an all-day event (setDateTime() is used for timed events)
                        EventDateTime startEventDateTime = new EventDateTime().setDate(startDateTime);
                        EventDateTime endEventDateTime = new EventDateTime().setDate(endDateTime);

                        event.setStart(startEventDateTime);
                        event.setEnd(endEventDateTime);
                    }
                    // event with specific time
                    else{
                        DateTime endDateTime = new DateTime(chore.getDue());
                        EventDateTime end = new EventDateTime()
                                .setDateTime(endDateTime)
                                .setTimeZone(TimeZone.getDefault().getID());

                        java.util.Calendar startTime = java.util.Calendar.getInstance();
                        startTime.setTime(chore.getDue());
                        startTime.add(java.util.Calendar.MINUTE, chore.getDuration()*-1);

                        DateTime startDateTime = new DateTime(startTime.getTime());
                        EventDateTime start = new EventDateTime()
                                .setDateTime(startDateTime)
                                .setTimeZone(TimeZone.getDefault().getID());

                        Log.i(TAG, start + " " + end);
                        event.setStart(start);
                        event.setEnd(end);
                    }

                    // set recurrence
                    Recurrence r = chore.getRecurrence();
                    if(r != null){
                        String rule = "RRULE:FREQ=";

                        if(r.getFrequencyType().equals(Recurrence.TYPE_YEAR)){
                            rule = rule + "YEARLY;";
                            java.util.Calendar due = java.util.Calendar.getInstance();
                            due.setTime(chore.getDue());
                            int dayOfYear = due.get(java.util.Calendar.DAY_OF_YEAR);
                            rule = rule + "BYYEARDAY=" + dayOfYear + ";WKST=SU;";
                        }
                        else if(r.getFrequencyType().equals(Recurrence.TYPE_MONTH)){
                            rule = rule + "MONTHLY;";
                            java.util.Calendar due = java.util.Calendar.getInstance();
                            due.setTime(chore.getDue());
                            int dayOfMonth = due.get(java.util.Calendar.DAY_OF_MONTH);
                            rule = rule + "BYMONTHDAY=" + dayOfMonth + ";WKST=SU;";
                        }
                        else if(r.getFrequencyType().equals(Recurrence.TYPE_WEEK)){
                            rule = rule + "WEEKLY;";
                            rule = rule + "BYDAY=" + getDaysOfWeek(r) + ";WKST=SU;";
                        }
                        else{
                            rule = rule + "DAILY;";
                        }

                        if(r.getFrequency() > 1){
                            rule = rule + "INTERVAL=" + r.getFrequency() + ";";
                        }

                        if(r.getNumOccurrences() >= 0){
                            rule = rule + "COUNT=" + r.getNumOccurrences() + ";";
                        }
                        else{
                            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
                            String until = format.format(new Date(r.getEndDate().getTime()));
                            rule = rule + "UNTIL=" + until + ";";
                        }

                        String[] recurrence = new String[] {rule};

                        Log.i(TAG, "Event recurrence: " + rule);
                        event.setRecurrence(Arrays.asList(recurrence));
                    }

                    // add attendees
                    if(emails != null && !emails.isEmpty()){
                        Log.i(TAG, emails.toString());
                        List<EventAttendee> attendees = new ArrayList<>();
                        for(int i=0; i<emails.size(); i++){
                            attendees.add(new EventAttendee().setEmail(emails.get(i)));
                        }
                        event.setAttendees(attendees);
                    }

                    String calendarId = "primary";

                    // save event
                    service.events().insert(calendarId, event).execute();
                    System.out.printf("Event created: %s\n", event.getSummary());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                activity.finish();
            }
        });

        thread.start();
        Toast.makeText(activity, "Added to Google Calendar", Toast.LENGTH_SHORT).show();
    }

    // get list of days of week for recurring events of weekly frequency
    private static String getDaysOfWeek(Recurrence r){
        String days = "";
        String parseDays = r.getDaysOfWeek();


        if(!parseDays.isEmpty()){
            for(int i=0; i<daySymbols.size(); i++){
                if(parseDays.contains(i + "")){
                    days = days + daySymbols.get(i) + ",";
                }
            }
        }
        if(!days.isEmpty()){
            days = days.substring(0, days.length() - 1);
        }
        return days;
    }
}
