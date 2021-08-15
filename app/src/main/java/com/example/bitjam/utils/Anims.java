package com.example.bitjam.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bitjam.R;

public class Anims {
    /**
     * Default {@link RecyclerView} loading animation.
     *
     * @param rv {@link RecyclerView} to animate
     */
    public static void recyclerFall(RecyclerView rv) {
        LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(rv.getContext(), R.anim.layout_fall);
        rv.setLayoutAnimation(controller);
        rv.scheduleLayoutAnimation();
    }

    /**
     *
     * @param v The button's view
     * @param event Direction
     * @return Default false
     */
    public static boolean smallShrink(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(30);
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            v.animate().scaleX(1f).scaleY(1f).setDuration(50);
        }

        return false;
    }

    /**
     *
     * @param v The button's view
     * @param event Direction
     * @return Default false
     */
    public static boolean bigShrink(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.animate().scaleX(0.7f).scaleY(0.7f).setDuration(70);
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            v.animate().scaleX(1f).scaleY(1f).setDuration(50);
        }

        return false;
    }

}
