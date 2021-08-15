package com.example.bitjam.utils;

/**
 * Use this interface whenever click functionality needs to be added to a {@link androidx.recyclerview.widget.RecyclerView}.
 */
public interface OnRecyclerClickListener<E> {
    void onItemClick(E item);
}