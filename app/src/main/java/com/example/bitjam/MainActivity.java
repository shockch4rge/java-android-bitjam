package com.example.bitjam;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.bitjam.ViewModels.PlaylistViewModel;
import com.example.bitjam.ViewModels.SongViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.Query;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNav;
    AppBarConfiguration appBarConfig;
    NavHostFragment navHostFragment;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hub);

        // Sets up the UI
        Window window = this.getWindow();
        View decorView = window.getDecorView();
        int uiOptions =
                View.SYSTEM_UI_FLAG_IMMERSIVE |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        window.setNavigationBarColor(getColor(R.color.white));
        decorView.setSystemUiVisibility(uiOptions);
        window.setStatusBarColor(getColor(R.color.white));

        // Fragment Navigation
        bottomNav = findViewById(R.id.navBar);
        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        assert navHostFragment != null;
        NavigationUI.setupWithNavController(bottomNav, navHostFragment.getNavController());
    }

    /**
     *
     * Hide the annoying keyboard without a stupid long method!
     * @param v The current view
     */
    public static void hideKeyboardIn(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}