package com.example.roomies.adapter;

import static com.example.roomies.model.CircleManager.getGroceryCollection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomies.R;
import com.example.roomies.model.GroceryItem;

import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ViewHolder> {
    List<GroceryItem> shoppingList;
    Context context;

    public static final String TAG = "ShoppingAdapter";

    public ShoppingAdapter(Context context, List<GroceryItem> list){
        this.context = context;
        shoppingList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_shopping_list, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroceryItem item = shoppingList.get(position);
        // bind user with list item
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox =  itemView.findViewById(R.id.checkBox);
        }

        public void bind(GroceryItem item){
            // custom to grocery item
            checkBox.setText(item.getName());
            checkBox.setChecked(item.getCompleted());
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getGroceryCollection().toggleGroceryCompletion(item);
                }
            });
        }
    }
}
