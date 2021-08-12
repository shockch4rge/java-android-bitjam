package com.example.bitjam.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitjam.Adapters.PlaylistAdapter;
import com.example.bitjam.Adapters.OnRecyclerClickListener;
import com.example.bitjam.Models.Playlist;
import com.example.bitjam.R;
import com.example.bitjam.Utils.Anims;
import com.example.bitjam.Utils.PopupBuilder;
import com.example.bitjam.ViewModels.PlaylistViewModel;
import com.example.bitjam.databinding.FragmentPlaylistsBinding;
import com.example.bitjam.Utils.Misc;
import com.example.bitjam.ViewModels.SongViewModel;
import com.google.firebase.firestore.Query;


public class PlaylistsFragment extends Fragment {
    private final String TAG = "(BitJam)";
    private FragmentPlaylistsBinding B;
    private PlaylistViewModel playlistVM;
    private SongViewModel songVM;
    private PlaylistAdapter playlistAdapter;
    private Window ui;

    // field that stores anonymous inner class which implements OnPlayerListener
    // Took one minute to implement when I thought it was going to take an hour...
    private final PopupBuilder.OnSortingMenuItemSelected onSortingMenuItemSelected = new PopupBuilder.OnSortingMenuItemSelected() {
        @Override
        public void onAscendingOrderSelected() {
            playlistVM.getPlaylistsFromDb(Query.Direction.ASCENDING);
            Misc.toast(requireView(), "Sorting list by ascending!");
        }

        @Override
        public void onDescendingOrderSelected() {
            playlistVM.getPlaylistsFromDb(Query.Direction.DESCENDING);
            Misc.toast(requireView(), "Sorting list by descending!");
        }
    };

    // Save song at the click position to SongViewModel and navigate to PlayerFragment
    private final OnRecyclerClickListener<Playlist> onRecyclerClickListener = new OnRecyclerClickListener<Playlist>() {
        @Override
        public void onItemClick(Playlist playlist) {
            songVM.select(playlist);

            NavController navController = NavHostFragment.findNavController(PlaylistsFragment.this);
            navController.navigate(PlaylistsFragmentDirections.NavigateToPlayerFromPlaylists());
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // View Binding. Removes the need for 'findViewById(id)'
        B = FragmentPlaylistsBinding.inflate(inflater, container, false);

        // UI
        ui = this.requireActivity().getWindow();
        ui.setStatusBarColor(getResources().getColor(R.color.white, null));

        // ViewModels
        playlistVM = new ViewModelProvider(requireActivity()).get(PlaylistViewModel.class);
        songVM = new ViewModelProvider(requireActivity()).get(SongViewModel.class);

        // update local cache with playlists from Firestore
        playlistVM.getPlaylistsFromDb(Query.Direction.ASCENDING);

        // When there is a change in playlists, update PlaylistAdapter
        playlistVM.getPlaylists().observe(getViewLifecycleOwner(), playlists -> {
            playlistAdapter.updatePlaylists(playlists);
            Anims.setLayoutAnimFall(B.playlistRecycler);
        });

        // Standard config for RecyclerView
        playlistAdapter = new PlaylistAdapter(onRecyclerClickListener, playlistVM);
        B.playlistRecycler.setLayoutManager(new LinearLayoutManager(PlaylistsFragment.this.getContext()));

        // Sets left-swiping action for the recycler
        B.playlistRecycler.setAdapter(playlistAdapter);

        // Initialises a click listener which inflates a PopupMenu when clicked
        B.sortOrderPopup.setOnClickListener(view -> PopupBuilder.forSortOrder(view, onSortingMenuItemSelected));

        return B.getRoot();
    }
}
