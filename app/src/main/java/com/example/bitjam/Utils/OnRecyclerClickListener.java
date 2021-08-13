package com.example.bitjam.Utils;

/**
 * An interface for recycler click implementations.
 * <br>
 * Use this interface whenever click functionality needs to be added to a {@link androidx.recyclerview.widget.RecyclerView}.
 */
// Perhaps declaring the type at interface declaration is better
    // ok it is
public interface OnRecyclerClickListener<E> {
    void onItemClick(E item);
}