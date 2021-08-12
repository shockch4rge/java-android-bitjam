package com.example.bitjam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.example.bitjam.ViewModels.PlaylistViewModel;
import com.example.bitjam.ViewModels.SongViewModel;
import com.google.firebase.firestore.Query;


public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Window window = this.getWindow();
        window.setStatusBarColor(getColor(R.color.pinkDefault));
        View decorView = window.getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        ImageButton btnWelcome = findViewById(R.id.btnWelcomeFace);
        CardView welcomeCard = findViewById(R.id.welcome_card_container);

        welcomeCard.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up));
        welcomeCard.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));

        btnWelcome.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        });
    }
}