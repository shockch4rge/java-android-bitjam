package com.example.bitjam.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.InflateException;
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

import com.example.bitjam.MainActivity;
import com.example.bitjam.R;
import com.example.bitjam.Adapters.OnRecyclerClickListener;
import com.example.bitjam.Adapters.LibraryAdapter;
import com.example.bitjam.databinding.FragmentLibraryBinding;
import com.example.bitjam.Models.Song;
import com.example.bitjam.Utils.Anims;
import com.example.bitjam.ViewModels.SongViewModel;

import java.util.ArrayList;
import java.util.List;


public class LibraryFragment extends Fragment {
    private final String TAG = "(BitJam)";
    private LibraryAdapter libraryAdapter;
    private SongViewModel songVM;
    private Window ui;

    // TextWatcher for EditText
    private final TextWatcher onTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            filter(s.toString());
        }
    };

    // transfers the clicked song to the fragment
    private final OnRecyclerClickListener<Song> onRecyclerClickListener = new OnRecyclerClickListener<Song>() {
        @Override
        public void onItemClick(Song song) {
            songVM.select(song);

            // Navigates to PlayerFragment when a song is clicked
            NavController navController = NavHostFragment.findNavController(LibraryFragment.this);
            navController.navigate(LibraryFragmentDirections.NavigateToPlayerFromLibrary());
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) throws InflateException {
        // View Binding. Removes the need for 'findViewById(id)'
        FragmentLibraryBinding B = FragmentLibraryBinding.inflate(inflater, container, false);

        // UI
        ui = this.requireActivity().getWindow();
        ui.setStatusBarColor(getResources().getColor(R.color.white, null));

        // ViewModels
        songVM = new ViewModelProvider(requireActivity()).get(SongViewModel.class);
        songVM.getSongsFromDb();


        // When there is a change in songs, update this adapter. There should be none though.
        songVM.getSongs().observe(getViewLifecycleOwner(), songs -> {
            libraryAdapter.updateSongs(songs);
            B.searchRecycler.scrollToPosition(0);
            Anims.setLayoutAnimFall(B.searchRecycler);
        });

        // Attach the adapter to the RecyclerView. Without it, rows would not be populated.
        libraryAdapter = new LibraryAdapter(onRecyclerClickListener);
        // Without LayoutManager, there would be no structure for RecyclerView to follow.
        // Default layout params would be null
        B.searchRecycler.setLayoutManager(new LinearLayoutManager(LibraryFragment.this.getContext()));
        B.searchRecycler.setAdapter(libraryAdapter);
        // Listeners
        B.searchBar.addTextChangedListener(onTextChangedListener);

        return B.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.hideKeyboardIn(this.requireView());
    }

    // updates the adapter for every letter typed in the search query
    // songs from ViewModel get copied to 'filtered'
    // adapter updates according to the filtered list
    private void filter(String text) {
        List<Song> filtered = new ArrayList<>();

        for (Song song : songVM.getSongs().getValue()) {
            if (song.getSongName().toLowerCase().contains(text.toLowerCase())) {
                filtered.add(song);
            }
        }
        libraryAdapter.updateSongs(filtered);
    }
}
