package com.example.roomies;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipboardManager;

import com.example.roomies.model.Circle;

import org.parceler.Parcels;

// Gives join code of newly created circle before going to home screen
public class NewCircleActivity extends AppCompatActivity {
    private TextView tvCode;
    private Button btnContinue;
    private ImageView ivCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_circle);

        tvCode = findViewById(R.id.tvCode);
        btnContinue = findViewById(R.id.btnContinue);
        ivCopy = findViewById(R.id.ivCopy);

        // unwrap the circle passed in via intent, using its simple name as a key
        Circle circle = Parcels.unwrap(getIntent().getParcelableExtra("circle"));
        tvCode.setText(circle.getObjectId());

        // go to main activity
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NewCircleActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        // copy object id to clipboard
        ivCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboard(NewCircleActivity.this, circle.getObjectId());
            }
        });
    }

    // copy text to clipboard
    private void setClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copied text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard " + text, Toast.LENGTH_SHORT).show();
    }
}