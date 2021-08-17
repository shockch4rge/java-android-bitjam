package com.example.bitjam.fragments;

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
import com.example.bitjam.utils.OnRecyclerClickListener;
import com.example.bitjam.adapters.LibraryAdapter;
import com.example.bitjam.databinding.FragmentLibraryBinding;
import com.example.bitjam.models.Song;
import com.example.bitjam.utils.Anims;
import com.example.bitjam.viewmodels.SongViewModel;

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

    // Select the song and go to PlayerFragment on click
    private final OnRecyclerClickListener<Song> onRecyclerClickListener = new OnRecyclerClickListener<Song>() {
        @Override
        public void onItemClick(Song song) {
            songVM.clearSelectedPlaylist();
            songVM.select(song);

            NavController navController = NavHostFragment.findNavController(LibraryFragment.this);
            navController.navigate(LibraryFragmentDirections.NavigateToPlayerFromLibrary());
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // View Binding. Removes the need for 'findViewById(id)'
        FragmentLibraryBinding B = FragmentLibraryBinding.inflate(inflater, container, false);

        // UI
        ui = requireActivity().getWindow();
        ui.setStatusBarColor(requireActivity().getResources().getColor(R.color.white, null));

        // ViewModels
        songVM = new ViewModelProvider(requireActivity()).get(SongViewModel.class);
//        songVM.getSongsFromDb();

        // Observers
        songVM.getSongs().observe(getViewLifecycleOwner(), songs -> {
            libraryAdapter.updateWith(songs);
            Anims.recyclerFall(B.searchRecycler);
        });

        libraryAdapter = new LibraryAdapter(onRecyclerClickListener);
        B.searchRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        B.searchRecycler.setAdapter(libraryAdapter);

        // Listeners
        B.searchBar.addTextChangedListener(onTextChangedListener);

        return B.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.hideKeyboardIn(requireView());
    }

    // Update adapter with filtered list for every letter
    private void filter(String text) {
        List<Song> filtered = new ArrayList<>();
        List<Song> songs = songVM.getSongs().getValue();

        songs.forEach(song -> {
            if (song.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filtered.add(song);
            }
        });

        libraryAdapter.updateWith(filtered);
    }
}
