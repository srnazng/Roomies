package com.example.roomies.model;

import static com.example.roomies.ShoppingListFragment.updateList;
import static com.example.roomies.model.CircleManager.getCurrentCircle;

import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GroceryCollection {
    private List<GroceryItem> groceryList;

    public static final String TAG = "GroceryCollection";

    public GroceryCollection(Context context){
        groceryList = new ArrayList<>();
        initGroceries(context);
    }

    /**
     * Retrieve groceryList sorted based on completion
     * @return
     */
    public List<GroceryItem> getGroceryList() {
        groceryList.sort(new Comparator<GroceryItem>() {
            @Override
            public int compare(GroceryItem o1, GroceryItem o2) {
                if(o1.getCompleted() && !o2.getCompleted()){
                    return 1;
                }
                if(o1.getCompleted() == o2.getCompleted()){
                    return 0;
                }
                return -1;
            }
        });
        return groceryList;
    }

    /**
     * Create new thread to initialize groceries
     * @param context
     */
    public void initGroceries(Context context){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateGroceries(context);
            }
        });
        thread.start();
    }

    /**
     * Retrieve groceries for circle
     * @param context
     */
    public void updateGroceries(Context context){
        Log.i(TAG, "INIT GROCERIES");

        // only get groceries for current circle
        ParseQuery<GroceryItem> query = ParseQuery.getQuery(GroceryItem.class)
                .whereEqualTo(GroceryItem.KEY_CIRCLE, getCurrentCircle());

        // order groceries by time created
        query.orderByDescending(GroceryItem.KEY_CREATED_AT);

        // query locally then from network
        query.fromLocalDatastore().findInBackground().continueWithTask((task) -> {
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting groceries", e);
            }
            else{
                Log.i(TAG, "Retrieved groceries locally");
                setGroceries(task.getResult());
            }
            return query.fromNetwork().findInBackground();
        }, ContextCompat.getMainExecutor(context)).continueWithTask((task) -> {
            ParseException e = (ParseException) task.getError();
            if (e != null) {
                Log.e(TAG, "Issue with getting groceries", e);
            }
            else{
                Log.i(TAG, "Retrieved groceries from network");
                setGroceries(task.getResult());
                // save to local data store
                GroceryItem.pinAllInBackground(task.getResult());
            }
            return task;
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * Set groceryList to list retrieved from query
     * @param list
     */
    public void setGroceries(List<GroceryItem> list){
        groceryList.clear();
        groceryList.addAll(list);
        updateList();
    }

    /**
     * Toggle completion field of groceryItem
     * @param groceryItem
     */
    public void toggleGroceryCompletion(GroceryItem groceryItem){
        groceryItem.setCompleted(!groceryItem.getCompleted());
        groceryItem.setCompletedBy(ParseUser.getCurrentUser());
        groceryItem.saveInBackground(e -> {
            if(e != null){
                Log.e(TAG, "Error marking item completed " + e);
            }
            else{
                Log.i(TAG, "Marked grocery complete");
                updateList();
            }
        });
    }

    /**
     * Delete all completed grocery items in circle
     */
    public void deleteCompleted(){
        int size = groceryList.size();
        for(int i = size - 1; i >= 0; i--){
            GroceryItem item = groceryList.get(i);
            if(item.getCompleted()){
                item.deleteInBackground();
                groceryList.remove(item);
            }
        }
        updateList();
    }

    /**
     * Add circle's new grocery item to database
     * @param name  Name of grocery item
     */
    public void addGrocery(String name){
        Log.i(TAG, "addGrocery");
        if(name.isEmpty()){
            return;
        }
        GroceryItem entity = new GroceryItem();

        entity.setName(name);
        entity.setCircle(getCurrentCircle());

        // Saves the new object.
        entity.saveInBackground(e -> {
            if (e==null){
                groceryList.add(0, entity);
                updateList();
            }else{
                //Something went wrong
                Log.e(TAG, "Error adding grocery " + e);
            }
        });
    }
}
