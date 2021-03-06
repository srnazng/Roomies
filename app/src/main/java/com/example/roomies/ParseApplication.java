package com.example.roomies;
import android.app.Application;

import com.example.roomies.model.Chore;
import com.example.roomies.model.ChoreAssignment;
import com.example.roomies.model.ChoreCompleted;
import com.example.roomies.model.Circle;
import com.example.roomies.model.Expense;
import com.example.roomies.model.ExpenseComment;
import com.example.roomies.model.GroceryItem;
import com.example.roomies.model.Recurrence;
import com.example.roomies.model.Transaction;
import com.example.roomies.model.UserCircle;
import com.parse.Parse;
import com.parse.ParseObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // register classes here
        ParseObject.registerSubclass(Circle.class);
        ParseObject.registerSubclass(UserCircle.class);
        ParseObject.registerSubclass(Chore.class);
        ParseObject.registerSubclass(ChoreAssignment.class);
        ParseObject.registerSubclass(ChoreCompleted.class);
        ParseObject.registerSubclass(GroceryItem.class);
        ParseObject.registerSubclass(Transaction.class);
        ParseObject.registerSubclass(Expense.class);
        ParseObject.registerSubclass(ExpenseComment.class);
        ParseObject.registerSubclass(Recurrence.class);

        // Use for troubleshooting -- remove this line for production
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // Use for monitoring Parse OkHttp traffic
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        // See https://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .enableLocalDataStore()
                .build());
    }
}
