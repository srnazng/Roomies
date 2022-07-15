package com.example.roomies;

import static com.example.roomies.utils.GoogleCalendar.createEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.roomies.model.Chore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;

public class GoogleSignInActivity extends AppCompatActivity {

    private static final String TAG = "GoogleSignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;

    private SignInButton  btnSignIn;
    private Button btnAccountContinue;

    private GoogleSignInAccount account;

    private Chore chore;
    private ArrayList<String> emails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);
        // Views
        mStatusTextView = findViewById(R.id.status);

        // Configure sign-in to request the user's ID, email address, and basic profile
        // Request Google Calendar scope
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("53458730684-6criqijvd5283u750o0af7tlfj7t7igg.apps.googleusercontent.com")
                .requestScopes(new Scope("https://www.googleapis.com/auth/calendar"))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Customize sign-in button.
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignIn.setSize(SignInButton.SIZE_STANDARD);
        btnSignIn.setColorScheme(SignInButton.COLOR_LIGHT);

        // Continue button
        btnAccountContinue = findViewById(R.id.btnAccountContinue);
    }

    // check if already signed in
    @Override
    public void onStart() {
        super.onStart();

        // get chore and email
        chore = getIntent().getParcelableExtra("chore");
        emails = getIntent().getStringArrayListExtra("emails");
        if(emails != null){
            Log.i(TAG, "final emails: " + emails.toString());
        }

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    // finish sign in
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // get token
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);

            try {
                addEvent(account);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode() + " " + e.getMessage());
            updateUI(null);
        }
    }

    // start intent to sign in
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // sign out Google user
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    // update view depending on whether user already signed in
    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            mStatusTextView.setText( account.getDisplayName());
            Log.i(TAG, account.getDisplayName());

            btnSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signOut();
                    signIn();
                }
            });

            btnAccountContinue.setVisibility(View.VISIBLE);
            btnAccountContinue.setText("Continue with " + account.getEmail());
            btnAccountContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        addEvent(account);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else {
            mStatusTextView.setText("Signed out");
            btnSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signIn();
                }
            });
            btnAccountContinue.setVisibility(View.GONE);
        }
    }

    public void addEvent(GoogleSignInAccount account) throws IOException {
        Log.i(TAG, "addEvent");
        createEvent(this, account, chore, emails);
        finish();
    }
}