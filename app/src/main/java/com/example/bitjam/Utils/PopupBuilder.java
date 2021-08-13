package com.example.bitjam.Utils;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.PopupMenu;

import com.example.bitjam.Fragments.PlayingFragment;
import com.example.bitjam.Fragments.PlaylistsFragment;
import com.example.bitjam.R;

/**
 * A custom builder class for {@link PopupMenu}s.
 */
public class PopupBuilder {
    private static OnPlaylistMenuItemSelected mOnPlaylistMenuItemSelectedCallback;
    private static OnPlayerMenuItemSelected mOnPlayerMenuItemSelectedCallback;
    private static OnSortingMenuItemSelected mOnSortingMenuItemSelected;

    // Creating multiple PopupMenus can and will result in a large amount of duplicated code that
    // gets scattered everywhere. This results in a lot of code smell and hence we should extract
    // the builder methods and compact them into a single one that we can call directly. The use of
    // a static context allows us to use the method without instantiating its class, which helps to
    // reduce memory usage from exposing unnecessary methods. I've done my best to delegate different
    // popups into their own methods, but there is still a ton of repeated code. There is actually
    // a better way to do it...

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

    // These nested interfaces allow any classes/anonymous inner classes that implement them to
    // trigger any callbacks without holding any actual references to this class. This is one of the
    // best ways to reduce coupling. However, too many methods in a listener can congest/dirty code
    // if used for various means, making some of the forced implemented methods useless.
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
