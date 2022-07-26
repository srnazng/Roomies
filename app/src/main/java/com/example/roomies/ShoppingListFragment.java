package com.example.roomies;

import static com.example.roomies.model.CircleManager.getGroceryCollection;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.roomies.adapter.ShoppingAdapter;
import com.example.roomies.model.GroceryItem;

import java.util.List;

public class ShoppingListFragment extends Fragment {
    private RecyclerView rvGroceries;
    private static ShoppingAdapter adapter;
    private EditText etGrocery;
    private Button btnAddGrocery;
    private ImageView ivSweep;

    private static List<GroceryItem> groceries;

    public ShoppingListFragment() {}

    public static ShoppingListFragment newInstance() {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        // bind to layout
        etGrocery = view.findViewById(R.id.etGrocery);
        btnAddGrocery = view.findViewById(R.id.btnAddGrocery);
        rvGroceries = view.findViewById(R.id.rvGroceries);
        ivSweep = view.findViewById(R.id.ivSweep);

        // add grocery item button
        btnAddGrocery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGroceryCollection().addGrocery(etGrocery.getText().toString());
                etGrocery.setText("");
            }
        });

        // clear completed items
        ivSweep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGroceryCollection().deleteCompleted();
            }
        });

        // initialize list of groceries
        groceries = getGroceryCollection().getGroceryList();
        // Create adapter passing in list of groceries
        adapter = new ShoppingAdapter(getActivity(), groceries);
        // Attach the adapter to the recyclerview to populate items
        rvGroceries.setAdapter(adapter);
        // Set layout manager to position the items
        rvGroceries.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    public static void updateList(){
        // update adapter
        groceries = getGroceryCollection().getGroceryList();
        adapter.notifyDataSetChanged();
    }
}