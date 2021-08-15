package com.example.bitjam.fragments;

import android.annotation.SuppressLint;
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

import com.example.bitjam.adapters.PlaylistAdapter;
import com.example.bitjam.utils.OnRecyclerClickListener;
import com.example.bitjam.models.Playlist;
import com.example.bitjam.R;
import com.example.bitjam.utils.Anims;
import com.example.bitjam.utils.PopupBuilder;
import com.example.bitjam.viewmodels.PlaylistViewModel;
import com.example.bitjam.databinding.FragmentPlaylistsBinding;
import com.example.bitjam.utils.Misc;
import com.example.bitjam.viewmodels.SongViewModel;
import com.google.firebase.firestore.Query;


public class PlaylistsFragment extends Fragment {
    private final String TAG = "(BitJam)";
    private FragmentPlaylistsBinding B;
    private PlaylistViewModel playlistVM;
    private SongViewModel songVM;
    private PlaylistAdapter playlistAdapter;

    // field that stores anonymous inner class which implements OnPlayerListener
    // Took one minute to implement when I thought it was going to take an hour...
    private final PopupBuilder.OnSortingMenuItemSelected onSortingMenuItemSelected = new PopupBuilder.OnSortingMenuItemSelected() {
        @Override
        public void onAscendingOrderSelected() {
            playlistVM.getPlaylistsFromDb(Query.Direction.ASCENDING);
            Misc.toast(requireView(), "Sorting by ascending!");
        }

        @Override
        public void onDescendingOrderSelected() {
            playlistVM.getPlaylistsFromDb(Query.Direction.DESCENDING);
            Misc.toast(requireView(), "Sorting by descending!");
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

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // View Binding. Removes the need for 'findViewById(id)'
        B = FragmentPlaylistsBinding.inflate(inflater, container, false);

        // UI
        Window ui = requireActivity().getWindow();
        ui.setStatusBarColor(getResources().getColor(R.color.white, null));

        // ViewModels
        playlistVM = new ViewModelProvider(requireActivity()).get(PlaylistViewModel.class);
        songVM = new ViewModelProvider(requireActivity()).get(SongViewModel.class);

        // update local cache with playlists from Firestore
        playlistVM.getPlaylistsFromDb(Query.Direction.ASCENDING);

        // When there is a change in playlists, update PlaylistAdapter
        playlistVM.getPlaylists().observe(getViewLifecycleOwner(), playlists -> {
            playlistAdapter.updateWith(playlists);
            Anims.recyclerFall(B.playlistRecycler);
        });

        // Standard config for RecyclerView
        playlistAdapter = new PlaylistAdapter(onRecyclerClickListener, playlistVM);
        B.playlistRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Sets left-swiping action for the recycler
        B.playlistRecycler.setAdapter(playlistAdapter);

        // Initialises a click listener which inflates a PopupMenu when clicked
        B.sortOrderPopup.setOnClickListener(view -> PopupBuilder.forSortOrder(view, onSortingMenuItemSelected));

        B.sortOrderPopup.setOnTouchListener(Anims::smallShrink);

        return B.getRoot();
    }
}
