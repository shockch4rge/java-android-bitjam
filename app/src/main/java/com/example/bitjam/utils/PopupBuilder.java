package com.example.bitjam.utils;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.PopupMenu;

import com.example.bitjam.fragments.PlayingFragment;
import com.example.bitjam.fragments.PlaylistsFragment;
import com.example.bitjam.R;

/**
 * A custom builder class for {@link PopupMenu}s.
 */
public class PopupBuilder {
    private static OnPlaylistMenuItemSelected mOnPlaylistMenuItemSelectedCallback;
    private static OnPlayerMenuItemSelected mOnPlayerMenuItemSelectedCallback;
    private static OnSortingMenuItemSelected mOnSortingMenuItemSelected;

    /**
     * Builds a popup menu for {@link PlayingFragment}'s
     * playlist button.
     *
     * @param v        The current view
     * @param callback Implementation methods for menu selection
     */
    @SuppressLint("NonConstantResourceId")
    public static void forPlayer(View v, OnPlayerMenuItemSelected callback) {
        setOnPlayerMenuItemSelectedCallback(callback);
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.setForceShowIcon(true);

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.ADD_TO_NEW_PLAYLIST:
                    mOnPlayerMenuItemSelectedCallback.onCreatePlaylistSelected();
                    return true;

                case R.id.ADD_TO_EXISTING_PLAYLIST:
                    mOnPlayerMenuItemSelectedCallback.onAddToExistingPlaylistSelected();
                    return true;

                default:
                    return false;
            }
        });

        popup.inflate(R.menu.player_popup);
        popup.show();
    }

    /**
     * Builds a popup menu for {@link PlaylistsFragment}'s row items' ellipsis button.
     *
     * @param v        The current view
     * @param callback Implementation methods for menu selection
     */
    @SuppressLint("NonConstantResourceId")
    public static void forPlaylist(View v, OnPlaylistMenuItemSelected callback) {
        setOnPlaylistMenuItemSelectedCallback(callback);
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.setForceShowIcon(true);

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.EDIT_PLAYLIST_NAME:
                    mOnPlaylistMenuItemSelectedCallback.onEditPlaylistNameItemSelected();
                    return true;

                case R.id.DELETE_PLAYLIST:
                    mOnPlaylistMenuItemSelectedCallback.onDeletePlaylistSelected();
                    return true;

                default:
                    return false;
            }
        });

        popup.inflate(R.menu.playlist_popup);
        popup.show();
    }

    /**
     * Builds a popup menu for {@link PlaylistsFragment}'s sort-order button.
     *
     * @param v        The current view.
     * @param callback Implementation methods for menu selection
     */
    @SuppressLint("NonConstantResourceId")
    public static void forSortOrder(View v, OnSortingMenuItemSelected callback) {
        setOnSortingMenuItemSelected(callback);
        PopupMenu popup = new PopupMenu(v.getContext(), v);

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.SORT_ASCENDING:
                    mOnSortingMenuItemSelected.onAscendingOrderSelected();
                    return true;

                case R.id.SORT_DESCENDING:
                    mOnSortingMenuItemSelected.onDescendingOrderSelected();
                    return true;

                default:
                    return false;
            }
        });

        popup.inflate(R.menu.sort_order_popup);
        popup.show();
    }

    public interface OnPlaylistMenuItemSelected {
        void onEditPlaylistNameItemSelected();

        void onDeletePlaylistSelected();
    }

    public interface OnPlayerMenuItemSelected {
        void onCreatePlaylistSelected();

        void onAddToExistingPlaylistSelected();
    }

    public interface OnSortingMenuItemSelected {
        void onAscendingOrderSelected();

        void onDescendingOrderSelected();
    }

    // Setter methods because I don't feel comfortable with direct assignment
    private static void setOnSortingMenuItemSelected(OnSortingMenuItemSelected callback) {
        PopupBuilder.mOnSortingMenuItemSelected = callback;
    }

    private static void setOnPlayerMenuItemSelectedCallback(OnPlayerMenuItemSelected callback) {
        PopupBuilder.mOnPlayerMenuItemSelectedCallback = callback;
    }

    private static void setOnPlaylistMenuItemSelectedCallback(OnPlaylistMenuItemSelected callback) {
        PopupBuilder.mOnPlaylistMenuItemSelectedCallback = callback;
    }


}
