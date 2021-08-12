package com.example.bitjam.Utils;

import android.content.Context;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bitjam.R;

import java.util.Objects;

public class Anims {
    public static void setLayoutAnimFall(RecyclerView rv) {
        LayoutAnimationController layoutAnimationController =
                AnimationUtils.loadLayoutAnimation(
                rv.getContext(),
                R.anim.layout_fall);
        rv.setLayoutAnimation(layoutAnimationController);
        rv.scheduleLayoutAnimation();
    }
}
