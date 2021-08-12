package com.example.bitjam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bitjam.Utils.GoogleServices;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SignInActivity extends AppCompatActivity {
    private final String TAG = "(BitJam Sign-In)";
    private final GoogleServices services = new GoogleServices();
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onStart() {
        super.onStart();
        // Rather than waiting for onCreate to be called and loading unnecessary details, we can
        // simply check for an existing signed-in account in this activity's onStart method, which
        // skips straight to WelcomeActivity if true.
        GoogleSignInAccount lastSignedIn = GoogleSignIn.getLastSignedInAccount(this);
        if (lastSignedIn != null) {
            Log.d(TAG, "Auto Sign-in!");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);

        // UI
        Window window = this.getWindow();
        View decorView = window.getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        mGoogleSignInClient = services.getGsc(this, services.getGso());

        findViewById(R.id.sign_in_button).setOnClickListener(v -> signIn());
    }

    private void signIn() {
        // Google has provided their own sign-in intent, so we can use this
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        collectedResults.launch(signInIntent);
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(signInIntent);
        handleSignInResult(task);
    }

    // Simply checks if the sign-in was a success. If it was, go to WelcomeActivity
    private final ActivityResultLauncher<Intent> collectedResults = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    startActivity(new Intent(this, WelcomeActivity.class));
                    finish();
                    Log.d(TAG, "Launched activity with sign-in-button");
                }
            });

    // Returns a task completion result. On success, we log the account's ID.
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, account.getId());
        } catch (ApiException e) {
            Log.w(TAG, "Sign-in failure. Error code: " + e.getStatusCode());
            e.printStackTrace();
        }
    }
}
