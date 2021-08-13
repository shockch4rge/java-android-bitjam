package com.example.bitjam.Fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitjam.MainActivity;
import com.example.bitjam.R;
import com.example.bitjam.Adapters.PlayerAdapter;
import com.example.bitjam.Utils.OnRecyclerClickListener;
import com.example.bitjam.Dialogs.DialogAddToPlaylist;
import com.example.bitjam.Dialogs.DialogCreatePlaylist;
import com.example.bitjam.Utils.Anims;
import com.example.bitjam.Utils.Misc;
import com.example.bitjam.Utils.PopupBuilder;
import com.example.bitjam.ViewModels.PlaylistViewModel;
import com.example.bitjam.databinding.FragmentPlayerBinding;
import com.example.bitjam.Models.Song;
import com.example.bitjam.ViewModels.PlayingViewModel;
import com.example.bitjam.ViewModels.SongViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;
import java.util.Objects;


public class PlayingFragment extends Fragment {
    private final String TAG = "(BitJam)";
    private FragmentPlayerBinding B;
    private SongViewModel songVM;
    private PlayingViewModel playerVM;
    private PlaylistViewModel playlistVM;
    private BottomNavigationView mNavBar;
    private PlayerAdapter playerAdapter;
    private View mBottomSheet;
    private Handler handler;
    private Window ui;

    // Listens to liking/un-liking song events
    private final SongViewModel.OnLikedListener onLikedListener = new SongViewModel.OnLikedListener() {
        @Override
        public void onLike(Song song) {
            B.btnLike.setImageResource(R.drawable.svg_heart_selected);
            Misc.toast(requireView(), "'" + song.getTitle() + "' liked!");
        }

        @Override
        public void onUnlike(Song song) {
            B.btnLike.setImageResource(R.drawable.svg_heart_unselected);
            Misc.toast(requireView(), "'" + song.getTitle() + "' un-liked!");
        }
    };

    // Listens to specific events defined in PlayerViewModel
    private final PlayingViewModel.OnPlayingListener onPlayingListener = new PlayingViewModel.OnPlayingListener() {
        // updates elapsed/remaining time when next/previous is pressed
        @Override
        public void onNext() {
            B.seekBar.setMax(playerVM.getDuration());
            B.rtl.setText(getTimeFormat(playerVM.getDuration()));
            playerVM.disableLoop();
            setLikedBtnBasedOnFlag();
        }

        @Override
        public void onPrevious() {
            B.seekBar.setMax(playerVM.getDuration());
            B.rtl.setText(getTimeFormat(playerVM.getDuration()));
            playerVM.disableLoop();
            setLikedBtnBasedOnFlag();
        }

        @Override
        public void onShuffled(List<Song> songs) {
            playerAdapter.updateSongs(songs);
        }

        @Override
        public void onUnshuffled(List<Song> songs) {
            playerAdapter.updateSongs(songs);
        }

        // automatically plays the next song
        @Override
        public void onSongCompletion() {
            handler.removeCallbacks(refreshSeekBar);
            playerVM.playNext();
        }
    };

    // Listens to touch events on SeekBar
    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                B.etl.setText(getTimeFormat(progress));
            }
        }

        // Stops updating time labels even when song is playing
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeCallbacks(refreshSeekBar);
        }

        // Updates time labels when finger is lifted off SeekBar thumb; also restarts recursive callbacks
        // will also update at that position
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            playerVM.moveTo(seekBar.getProgress());
            handler.postDelayed(refreshSeekBar, 500);
        }
    };

    // Create PopupMenu with options
    private final PopupBuilder.OnPlayerMenuItemSelected onPlayerMenuItemSelected = new PopupBuilder.OnPlayerMenuItemSelected() {
        // Listens to 'Add to New Playlists' selection in the popup menu
        @Override
        public void onCreatePlaylistSelected() {
            DialogCreatePlaylist dialog = new DialogCreatePlaylist();

            dialog.setOnCreateChoiceClickListener(textInput -> {
                String title = textInput.getText().toString();
                Song thisSongId = playerVM.getCurrentSong();
                String songId = thisSongId.getId();
                String songTitle = thisSongId.getTitle();

                // Dialog automatically closes on choice click; no need to manually do it
                if (title.isEmpty()) {
                    Misc.toast(requireView(),
                            "Name cannot be empty!");
                } else {
                    playlistVM.createNewPlaylist(title, songId);
                    Misc.toast(requireView(),
                            title + " created with '" + songTitle + "' added!");
                }
                MainActivity.hideKeyboardIn(PlayingFragment.this.requireView());
            });

            dialog.show(getParentFragmentManager(), "OK");
        }

        // Listens to 'Add to Existing Playlist' selection in the popup menu
        @Override
        public void onAddToExistingPlaylistSelected() {

            // Must cast to Playlist class before using parameter
            DialogAddToPlaylist dialog = new DialogAddToPlaylist(playlist -> {
                Song thisSong = playerVM.getCurrentSong();
                String songId = thisSong.getId();
                String songTitle = thisSong.getTitle();

                playlistVM.addSongToExistingPlaylist(playlist.getId(), songId);
                Misc.toast(requireView(),
                        "'" + songTitle + " added to " + playlist.getTitle() + "!");
            });

            dialog.show(getParentFragmentManager(), "OK");
        }
    };

    // PlayerAdapter
    private final OnRecyclerClickListener<Song> onRecyclerClickListener = new OnRecyclerClickListener<Song>() {
        @Override
        public void onItemClick(Song song) {
            songVM.select(song);
            playerVM.prepareSong(songVM.getSelectedSong().getValue());
            playerVM.play();
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // View Binding. Removes the need for 'findViewById(id)'
        B = FragmentPlayerBinding.inflate(inflater, container, false);

        handler = new Handler();

        // ViewModels
        songVM = new ViewModelProvider(requireActivity()).get(SongViewModel.class);
        playerVM = new ViewModelProvider(requireActivity()).get(PlayingViewModel.class);
        playlistVM = new ViewModelProvider(requireActivity()).get(PlaylistViewModel.class);

        // Initialises the queue and loads playlists
        playerVM.updateQueue(songVM.getSongs().getValue());

        // Observers
        songVM.getSongs().observe(this, songs -> {
            playerAdapter.updateSongs(songs);
            B.playerRecycler.scrollToPosition(0);
            Anims.recyclerFall(B.playerRecycler);
        });

        playerVM.currentSong.observe(this, this::updateUiFromSong);
        playerVM.isPlaying.observe(this, this::onIsPlayingChange);
        playerVM.isLooping.observe(this, this::onIsLoopingChange);
        playerVM.isShuffling.observe(this, this::onIsShuffledChange);

        // Listeners
        songVM.setOnLikedListener(onLikedListener);
        playerVM.setOnPlayerListener(onPlayingListener);
        B.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        // Separate functions from animations to reduce unnecessary clutter
        B.btnPlaylistOptions.setOnClickListener(view -> PopupBuilder.forPlayer(view, onPlayerMenuItemSelected));
        B.btnPlayPause.setOnClickListener(view -> playerVM.togglePlayPause());
        B.btnNext.setOnClickListener(view -> playerVM.playNext());
        B.btnPrevious.setOnClickListener(view -> playerVM.playPrevious());
        B.btnLoop.setOnClickListener(view -> playerVM.toggleLoop());
        B.btnShuffle.setOnClickListener(view -> playerVM.toggleShuffle());
        B.btnLike.setOnClickListener(this::toggleLikeOnSong);

        // Button animations
        B.btnPlayPause.setOnTouchListener(Anims::smallShrink);
        B.btnNext.setOnTouchListener(Anims::smallShrink);
        B.btnPrevious.setOnTouchListener(Anims::smallShrink);
        B.btnLoop.setOnTouchListener(Anims::smallShrink);
        B.btnShuffle.setOnTouchListener(Anims::smallShrink);
        B.btnLike.setOnTouchListener(Anims::smallShrink);
        B.btnPlaylistOptions.setOnTouchListener(Anims::smallShrink);

        // Extracts selected song details
        Song newSong = songVM.getSelectedSong().getValue();
        playerVM.prepareSong(newSong);
        B.seekBar.setMax(playerVM.getDuration());
        B.rtl.setText(getTimeFormat(playerVM.getDuration()));

        // PlayerAdapter
        B.playerRecycler.setLayoutManager(new LinearLayoutManager(PlayingFragment.this.getContext()));
        playerAdapter = new PlayerAdapter(onRecyclerClickListener);
        B.playerRecycler.setAdapter(playerAdapter);

        playerVM.play();

        return B.getRoot();
    }

    private void onIsPlayingChange(boolean isPlaying) {
        if (isPlaying) {
            handler.postDelayed(refreshSeekBar, 500);
            B.btnPlayPause.setImageResource(R.drawable.svg_btn_pause);
        } else {
            handler.removeCallbacks(refreshSeekBar);
            B.btnPlayPause.setImageResource(R.drawable.svg_btn_play);
        }
    }

    private void onIsLoopingChange(boolean isLooping) {
        B.btnLoop.setColorFilter(ContextCompat.getColor(
                requireActivity(),
                isLooping ? R.color.pinkDefault : R.color.white)
        );
    }

    private void onIsShuffledChange(boolean isShuffling) {
        B.btnShuffle.setColorFilter(ContextCompat.getColor(
                requireActivity(),
                isShuffling ? R.color.pinkDefault : R.color.white
        ));
    }

    // Find bottom UI from activity
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = requireActivity().getWindow();
        mNavBar = requireActivity().findViewById(R.id.navBar);
        mBottomSheet = requireActivity().findViewById(R.id.bottomSheet);
    }

    // Make bottom UI visible
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mNavBar.setVisibility(View.VISIBLE);
        mBottomSheet.setVisibility(View.VISIBLE);
    }

    // No bottom UI while view exists
    @Override
    public void onResume() {
        super.onResume();
        mNavBar.setVisibility(View.GONE);
        mBottomSheet.setVisibility(View.GONE);
    }

    private void updateUiFromSong(Song song) {
        B.songHolder.setText(song.getTitle());
        B.artistHolder.setText(song.getArtist());
        Picasso.get().load(song.getCoverUrl()).into(B.coverArtHolder);
        Picasso.get().load(song.getCoverUrl()).into(swatchProcessor);
    }

    // If the current song is already liked, remove it from liked and vice versa.
    private void toggleLikeOnSong(View v) {
        Song currentSong = playerVM.getCurrentSong();
        if (currentSong.isLiked) {
            currentSong.setLiked(false);
            songVM.removeFromLiked(currentSong);
        } else {
            currentSong.setLiked(true);
            songVM.addToLiked(currentSong);
        }
    }

    // Updates the like button's ImageView based on the song's liked flag. Nothing much.
    private void setLikedBtnBasedOnFlag() {
        Song song = playerVM.getCurrentSong();
        B.btnLike.setImageResource(song.isLiked
                ? R.drawable.svg_heart_selected
                : R.drawable.svg_heart_unselected);
    }

    // For time labels
    private String getTimeFormat(int duration) {
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        return min + ":" + ((sec < 10) ? "0" + sec : sec);
    }

    // Update SeekBar and time labels every 0.5s
    private final Runnable refreshSeekBar = new Runnable() {
        @Override
        public void run() {
            B.seekBar.setProgress(playerVM.getCurrentPos());
            B.etl.setText(getTimeFormat(playerVM.getCurrentPos()));
            handler.postDelayed(this, 500);
        }
    };

    // Sets bg to dominant swatch of the cover art
    private final Target swatchProcessor = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Palette.from(bitmap).generate(palette -> {
                assert palette != null;
                assert palette.getDominantSwatch() != null;
                int dominantSwatch = palette.getDominantSwatch().getRgb();
                B.motionLayout.setBackgroundColor(dominantSwatch);
                ui.setStatusBarColor(dominantSwatch);
            });
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            Log.d("(Palette)", "Failed to load bitmap.");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };
}