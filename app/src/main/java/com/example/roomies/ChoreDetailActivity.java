package com.example.roomies;

import static com.example.roomies.utils.ChoreUtils.chipCompleted;
import static com.example.roomies.utils.ChoreUtils.getAllChoreAssignments;
import static com.example.roomies.utils.ChoreUtils.getChoreAssignment;
import static com.example.roomies.utils.ChoreUtils.isCompleted;
import static com.example.roomies.utils.ChoreUtils.markCompleted;
import static com.example.roomies.utils.ChoreUtils.setPriorityColors;
import static com.example.roomies.utils.Utils.formatDue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.roomies.model.Chore;
import com.example.roomies.model.ChoreAssignment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.List;

public class ChoreDetailActivity extends AppCompatActivity {
    Chore chore;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvDue;
    public static MaterialCardView card;
    private Button messageButton;
    private Button btnGoogleCalendar;
    private Calendar day;
    private ImageView ivCircle;
    private ChipGroup assigneeChips;

    public static final String TAG = "ChoreDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chore_detail);

        // get chore and day of chore from intent
        chore = getIntent().getParcelableExtra("chore");
        day = (Calendar) getIntent().getSerializableExtra("day");
        if(day == null){
            day = Calendar.getInstance();
        }

        // add chore details to card
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvTitle.setText(chore.getTitle());

        tvDescription = findViewById(R.id.tvDetailDescription);
        tvDescription.setText(chore.getDescription());
        if(chore.getDescription().isEmpty()){
            tvDescription.setVisibility(View.GONE);
        }

        tvDue = findViewById(R.id.tvDetailDue);
        tvDue.setText(formatDue(chore, day));

        card = findViewById(R.id.detailCard);
        card.setLongClickable(false);
        card.setCheckable(true);
        card.setChecked(isCompleted(getChoreAssignment(chore)));

        btnGoogleCalendar = findViewById(R.id.btnDetailGoogleCalendar);
        btnGoogleCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChoreDetailActivity.this, GoogleSignInActivity.class);
                i.putExtra("chore", chore);
                startActivity(i);
            }
        });

        ivCircle = findViewById(R.id.ivCircle);
        setPriorityColors(this, ivCircle, chore);

        assigneeChips = findViewById(R.id.assigneeChips);
        initializeChips();
    }

    // set chips to all users assigned chore
    public void initializeChips(){
        List<ChoreAssignment> assignees = getAllChoreAssignments(chore);

        if(assigneeChips == null){
            return;
        }

        // clear ChipGroup
        assigneeChips.removeAllViews();

        // create and add new chips
        for(int i=0; i<assignees.size(); i++){
            Chip chip = new Chip(this);
            ChoreAssignment c = assignees.get(i);
            chip.setText(c.getUser().getString("name"));
            chip.setClickable(false);
            chip.setCheckable(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.yellow)));
            chipCompleted(assignees.get(i), chip, day);

            // user can click their own chip to mark complete
            if(assignees.get(i).getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
                chip.setClickable(true);
                chip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        markCompleted(chore, chip.isChecked(), day);
                        Log.i(TAG, "checked: " + chip.isChecked());
                        card.setChecked(chip.isChecked());
                    }
                });
            }
            else{
                chip.setClickable(false);
            }
            assigneeChips.addView(chip);
        }
    }
}