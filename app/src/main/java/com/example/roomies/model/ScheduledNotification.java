package com.example.roomies.model;

import static com.example.roomies.utils.ChoreUtils.getMyPendingChoresToday;
import static com.example.roomies.utils.ExpenseUtils.getMyPendingPayments;
import static com.example.roomies.utils.Messaging.sendToDeviceGroup;

import android.util.Log;

import com.parse.ParseUser;

import java.util.TimerTask;

public class ScheduledNotification extends TimerTask {
    private int numChoresPending = 0;
    private int numPaymentsPending = 0;
    private String type;

    public static final String TYPE_CHORE = "chore";
    public static final String TYPE_EXPENSE = "expense";

    public static final String TAG = "ScheduledNotification";

    public ScheduledNotification(String type){
        this.type = type;
    }

    @Override
    public void run() {
        // ensure logged in
        if(ParseUser.getCurrentUser() == null){
            return;
        }

        // get device group key of user
        String notificationKey = ParseUser.getCurrentUser().getString("notificationKey");
        if(type.equals(TYPE_CHORE) && getMyPendingChoresToday() != null){
            numChoresPending = getMyPendingChoresToday().size();

            // send notification
            if(numChoresPending > 0){
                Log.i(TAG, "Sending chore reminder for " + numChoresPending + " chores");
                sendToDeviceGroup(notificationKey, "Chore reminder", "You have " + numChoresPending + " pending chores today!" );
            }
        }
        else if(type.equals(TYPE_EXPENSE) && getMyPendingPayments() != null){
            numPaymentsPending = getMyPendingPayments().size();

            // send notification
            if(numPaymentsPending > 0){
                Log.i(TAG, "Sending expense reminder " + numPaymentsPending + " expenses");
                sendToDeviceGroup(notificationKey, "Payment reminder", "You have " + numPaymentsPending + " pending house expenses!" );
            }
        }
    }


}
