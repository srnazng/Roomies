package com.example.roomies.utils;
import com.example.roomies.R;
import com.example.roomies.model.Circle;

import static com.example.roomies.model.CircleManager.getCurrentCircle;
import static com.example.roomies.utils.UserUtils.parseLogout;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.parse.ParseUser;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Messaging extends FirebaseMessagingService {
    private static Context context;
    public static final String TAG = "Messaging";

    public static final String DEVICE_GROUP_URL = "https://fcm.googleapis.com/fcm/notification";
    public static final String SEND_NOTIF_URL = "https://fcm.googleapis.com/fcm/send";

    private static String API_KEY;
    private static String PROJECT_ID;

    public Messaging(Context context) {
        this.context = context;

        // init keys for API requests
        API_KEY = context.getResources().getString(R.string.firebase_server_key);
        PROJECT_ID = context.getResources().getString(R.string.firebase_sender_id);

        if(ParseUser.getCurrentUser() != null && getCurrentCircle() != null){
            String token = ParseUser.getCurrentUser().getString("notificationToken");
            // register if no notification token
            if(token == null || token.isEmpty()){
                register();
            }
            else{
                checkUserTokenUpdate();
            }

            if(getCurrentCircle().getNotificationKey() == null ||
                getCurrentCircle().getNotificationKey().isEmpty()){
                generateCircleNotificationKey();
            }

            updateUserNotificationKey();
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    /**
     * clear token
     * @param withLogout
     */
    public static void clearFirebaseInstance(boolean withLogout){
        Log.i(TAG, "Delete token");
        FirebaseMessaging.getInstance().deleteToken();
        if(ParseUser.getCurrentUser() == null){
            Log.e(TAG, "no current user");
        }
        ParseUser.getCurrentUser().put("registerCircleNotifs", false);
        ParseUser.getCurrentUser().put("notificationToken", "");
        ParseUser.getCurrentUser().put("notificationKey", "");
        ParseUser.getCurrentUser().saveInBackground(e -> {
            if(e != null ){
                Log.e(TAG, e.getMessage());
            }
            if(withLogout){
                parseLogout(context);
            }
        });
    }

    /**
     * Add user to their own notification group
     */
    private static void addToUserGroup(){
        if(ParseUser.getCurrentUser().getString("notificationToken") == null){
            Log.e(TAG, "no notification token");
            return;
        }
        if(ParseUser.getCurrentUser().getString("notificationKey") == null){
            updateUserNotificationKey();
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    String content = "{\n    \"operation\": \"add\",\n    " +
                            "\"notification_key_name\": \"" + "user-" + ParseUser.getCurrentUser().getObjectId() + "\",\n    " +
                            "\"notification_key\": \"" + ParseUser.getCurrentUser().getString("notificationKey") + "\",\n     " +
                            "\"registration_ids\": [\n        " +
                            "\"" + ParseUser.getCurrentUser().getString("notificationToken") + "\"\n    ]\n}";
                    RequestBody body = RequestBody.create(mediaType, content);
                    Log.i(TAG, content);
                    Request request = new Request.Builder()
                            .url(DEVICE_GROUP_URL)
                            .method("POST", body)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", API_KEY)
                            .addHeader("project_id", PROJECT_ID)
                            .build();
                    ResponseBody responseBody = client.newCall(request).execute().body();

                    JSONObject obj = new JSONObject(responseBody.string());
                    if(obj.has("error")){
                        Log.i(TAG, obj.getString("error"));
                    }
                    else{
                        String key = obj.getString("notification_key");
                        Log.i(TAG, "User added to device group");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    /**
     * Create notification group for user
     */
    private static void updateUserNotificationKey() {
        Log.i(TAG, "updateUserNotificationKey");
        if(ParseUser.getCurrentUser().getString("notificationToken") == null){
            Log.e(TAG, "no notification token");
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, "");
                    String url = "https://fcm.googleapis.com/fcm/notification?notification_key_name=user-" + ParseUser.getCurrentUser().getObjectId();
                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", API_KEY)
                            .addHeader("project_id", PROJECT_ID)
                            .build();
                    Response response = client.newCall(request).execute();

                    ResponseBody responseBody = response.body();

                    JSONObject obj = new JSONObject(responseBody.string());
                    if(obj.has("error")){
                        Log.i(TAG, obj.getString("error"));
                        generateNewDeviceKey();
                    }
                    else{
                        String key = obj.getString("notification_key");
                        Log.i(TAG, "User device group key: " + key);
                        ParseUser.getCurrentUser().put("notificationKey", key);
                        ParseUser.getCurrentUser().saveInBackground();
                    }
                }
                catch (Exception err){
                    Log.e(TAG, err.getMessage());
                }

            }
        });

        thread.start();
    }

    public static void generateNewDeviceKey(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    Log.i(TAG, "generateNewDeviceKey");
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    String content = "{\n    \"operation\": \"create\",\n    " +
                            "\"notification_key_name\": \"" + "user-" + ParseUser.getCurrentUser().getObjectId() + "\",\n    " +
                            "\"registration_ids\": [\n        " +
                            "\"" + ParseUser.getCurrentUser().getString("notificationToken") + "\"\n    ]\n}";
                    RequestBody body = RequestBody.create(mediaType, content);
                    Log.i(TAG, content);
                    Request request = new Request.Builder()
                            .url(DEVICE_GROUP_URL)
                            .method("POST", body)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", API_KEY)
                            .addHeader("project_id", PROJECT_ID)
                            .build();
                    ResponseBody responseBody = client.newCall(request).execute().body();

                    JSONObject obj = new JSONObject(responseBody.string());
                    if(obj.has("error")){
                        Log.i(TAG, obj.getString("error"));
                    }
                    else{
                        String key = obj.getString("notification_key");
                        Log.i(TAG, "User Device Group key: " + key);
                        ParseUser.getCurrentUser().put("notificationKey", key);
                        ParseUser.getCurrentUser().saveInBackground();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /**
     * Create notification group for circle
     */
    private static void generateCircleNotificationKey() {
        if(ParseUser.getCurrentUser().getString("notificationToken") == null){
            Log.e(TAG, "no notification token");
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    String content = "{\n    \"operation\": \"create\",\n    " +
                            "\"notification_key_name\": \"" + getCurrentCircle().getObjectId() + "\",\n    " +
                            "\"registration_ids\": [\n        " +
                            "\"" + ParseUser.getCurrentUser().getString("notificationToken") + "\"\n    ]\n}";
                    RequestBody body = RequestBody.create(mediaType, content);
                    Log.i(TAG, content);
                    Request request = new Request.Builder()
                            .url(DEVICE_GROUP_URL)
                            .method("POST", body)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", API_KEY)
                            .addHeader("project_id", PROJECT_ID)
                            .build();
                    ResponseBody responseBody = client.newCall(request).execute().body();

                    JSONObject obj = new JSONObject(responseBody.string());
                    if(obj.has("error")){
                        Log.i(TAG, obj.getString("error"));
                    }
                    else{
                        String key = obj.getString("notification_key");
                        Log.i(TAG, "Circle key: " + key);
                        Circle circle = getCurrentCircle();
                        circle.setNotificationKey(key);
                        circle.saveInBackground();

                        if(!ParseUser.getCurrentUser().getBoolean("registerCircleNotifs")){
                            addToCircle();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    /**
     * Add current user to circle's notification group
     */
    public static void addToCircle(){
        if(ParseUser.getCurrentUser().getString("notificationToken") == null){
            Log.e(TAG, "no notification token");
            return;
        }
        if(getCurrentCircle().getNotificationKey() == null || getCurrentCircle().getNotificationKey().isEmpty() ){
            generateCircleNotificationKey();
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    String content = "{\n    \"operation\": \"add\",\n    " +
                            "\"notification_key_name\": \"" + getCurrentCircle().getObjectId() + "\",\n    " +
                            "\"notification_key\": \"" + getCurrentCircle().getNotificationKey() + "\",\n    " +
                            "\"registration_ids\": [\n        " +
                            "\"" + ParseUser.getCurrentUser().getString("notificationToken") + "\"\n    ]\n}";
                    RequestBody body = RequestBody.create(mediaType, content);
                    Log.i(TAG, content);
                    Request request = new Request.Builder()
                            .url(DEVICE_GROUP_URL)
                            .method("POST", body)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", API_KEY)
                            .addHeader("project_id", PROJECT_ID)
                            .build();
                    ResponseBody responseBody = client.newCall(request).execute().body();

                    JSONObject obj = new JSONObject(responseBody.string());
                    if(obj.has("error")){
                        Log.i(TAG, obj.getString("error"));
                    }
                    else{
                        String key = obj.getString("notification_key");
                        Log.i(TAG, "Circle key: " + key);
                        Circle circle = getCurrentCircle();
                        circle.setNotificationKey(key);
                        circle.saveInBackground();
                        ParseUser user = ParseUser.getCurrentUser();
                        user.put("registerCircleNotifs", true);
                        user.saveInBackground();

                        Log.i(TAG, "Current user added to circle device group");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    /**
     * Create a new notification token for current user
     */
    public static void register(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        Log.d(TAG, "registered: " + token);
                        ParseUser user = ParseUser.getCurrentUser();
                        user.put("notificationToken", token);
                        user.saveInBackground();

                        if(user.getString("notificationKey") != null
                            && !user.getString("notificationKey").isEmpty()){
                            addToUserGroup();
                        }
                        else{
                            updateUserNotificationKey();
                        }

                        if(!user.getBoolean("registerCircleNotifs")){
                            addToCircle();
                        }
                    }
                });
    }

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }

    /**
     * update ParseUser object
     */
    private void sendRegistrationToServer(String token) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("notificationToken", token);
        user.saveInBackground();
    }

    /**
     * Send notification to device group
     * @param groupKey
     * @param title
     * @param body
     */
    public static void sendToDeviceGroup(String groupKey, String title, String body){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    String content = "{\n  \"to\":\"" + groupKey + "\"," +
                            "\n  \"content_available\": true,\n  \"priority\": \"high\",\n  \"notification\": {\n      " +
                            "\"title\": \"" + title + "\",\n      " +
                            "\"body\": \"" + body + "\"\n   }\n}";
                    RequestBody body = RequestBody.create(mediaType, content);
                    Request request = new Request.Builder()
                            .url(SEND_NOTIF_URL)
                            .method("POST", body)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", API_KEY)
                            .build();
                    Response response = client.newCall(request).execute();
                    Log.i(TAG, title + " - " + body + " sent success");
                } catch (Exception e) {
                    Log.e(TAG, "send reminder failure ");
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    /**
     * Check if user's token in Parse is the same as actual token
     */
    public void checkUserTokenUpdate(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                Log.i(TAG, "check notificationToken");

                // Get new FCM registration token
                String token = task.getResult();
                Log.i(TAG, "\n" + token + "\n" + ParseUser.getCurrentUser().getString("notificationToken"));

                if(!token.equals(ParseUser.getCurrentUser().getString("notificationToken"))){
                    ParseUser user = ParseUser.getCurrentUser();
                    user.put("notificationToken", token);
                    user.saveInBackground(e -> {
                            if(e == null){
                                // Log and toast
                                Log.d(TAG, "updated token for " + ParseUser.getCurrentUser().getString("name") + ": " + token);
                                addToUserGroup();
                                addToCircle();
                            }
                        }
                    );
                }
            }
        });
    }
}
