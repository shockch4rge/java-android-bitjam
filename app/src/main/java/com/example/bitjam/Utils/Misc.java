package com.example.bitjam.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.bitjam.R;
import com.google.android.material.snackbar.Snackbar;


public class Misc {

    // This class basically provides easy access to compacted forms of otherwise verbose toasts/
    // snacks. Static context, so no need to instantiate an object.

    /**
     *
     * Creates a short {@link Toast}.
     * @param v The current view
     * @param text The text to show
     */
    public static void toast(View v, String text) {
        Toast.makeText(v.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * Creates a long {@link Toast}.
     * @param v The current view
     * @param text The text to show
     */
    public static void cheer(View v, String text) {
        Toast.makeText(v.getContext(), text, Toast.LENGTH_LONG).show();
    }

    /**
     *
     * Creates a short {@link Snackbar} with no set action.
     * @param v The current view
     * @param text The text to show
     */
    public static void snack(View v, String text) {
        Snackbar.make(v, text, Snackbar.LENGTH_SHORT);
    }

    /**
     *
     * Creates a long {@link Snackbar} with no set action.
     * @param v The current view
     * @param text The text to show
     */
    public static void feast(View v, String text) {
        Snackbar.make(v, text, Snackbar.LENGTH_LONG);
    }

}
