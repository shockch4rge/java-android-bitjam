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
import com.example.bitjam.Models.Playlist;
import com.example.bitjam.R;
import com.example.bitjam.Adapters.PlayerAdapter;
import com.example.bitjam.Adapters.OnRecyclerClickListener;
import com.example.bitjam.Fragments.Dialogs.DialogAddToPlaylist;
import com.example.bitjam.Fragments.Dialogs.DialogCreatePlaylist;
import com.example.bitjam.Utils.Anims;
import com.example.bitjam.Utils.Misc;
import com.example.bitjam.Utils.PopupBuilder;
import com.example.bitjam.ViewModels.PlaylistViewModel;
import com.example.bitjam.databinding.FragmentPlayerBinding;
import com.example.bitjam.Models.Song;
import com.example.bitjam.ViewModels.PlayerViewModel;
import com.example.bitjam.ViewModels.SongViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;
import java.util.Objects;


public class PlayerFragment extends Fragment {
    private final String TAG = "(BitJam)";
    private FragmentPlayerBinding B;
    private SongViewModel songVM;
    private PlayerViewModel playerVM;
    private PlaylistViewModel playlistVM;
    private BottomNavigationView navBar;
    private PlayerAdapter playerAdapter;
    private Handler handler;
    private Window ui;

    // Listens to liking/un-liking song events
    private final SongViewModel.OnLikedListener onLikedListener = new SongViewModel.OnLikedListener() {
        @Override
        public void onLike(Song song) {
            B.btnLike.setImageResource(R.drawable.svg_heart_selected);
            Misc.toast(requireView(), "'" + song.getSongName() + "' liked!");
        }

        @Override
        public void onUnlike(Song song) {
            B.btnLike.setImageResource(R.drawable.svg_heart_unselected);
            Misc.toast(requireView(), "'" + song.getSongName() + "' un-liked!");
        }
    };

    // Listens to specific events defined in PlayerViewModel
    private final PlayerViewModel.OnPlayerListener onPlayerListener = new PlayerViewModel.OnPlayerListener() {
        // updates elapsed/remaining time when next/previous is pressed
        @Override
        public void onNext() {
            B.seekBar.setMax(playerVM.getDuration());
            playerVM.disableLoop();
            setLikedBtnColourByFlag();
        }

        @Override
        public void onPrevious() {
            B.seekBar.setMax(playerVM.getDuration());
            playerVM.disableLoop();
            setLikedBtnColourByFlag();
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
            handler.removeCallbacks(refreshDetails);
            playerVM.playNext();
        }

        @Override
        public void onSongPrepared(Song prepared) {
            updateUiFromSong(prepared);
        }
    };

    // Listens to touch events on SeekBar
    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        // Stops updating time labels even when song is playing
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeCallbacks(refreshDetails);
        }

        // Updates time labels when finger is lifted off SeekBar thumb; also restarts recursive callbacks
        // will also update at that position
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            playerVM.moveTo(seekBar.getProgress());
            B.rtl.setText(getTimeFormat(playerVM.getCurrentPos()));
            B.etl.setText(getTimeFormat(playerVM.getDuration() - playerVM.getCurrentPos()));
            handler.postDelayed(refreshDetails, 500);
        }
    };

    // Creates a PopupMenu with pre-defined choices
    private final PopupBuilder.OnPlayerMenuItemSelected onPlayerMenuItemSelected = new PopupBuilder.OnPlayerMenuItemSelected() {
        // Listens to 'Add to New Playlists' selection in the popup menu
        @Override
        public void onAddToNewPlaylistSelected() {
            Song thisSong = playerVM.getCurrentSong();

            DialogCreatePlaylist dialog = new DialogCreatePlaylist();

            dialog.setDialogListener(titleField -> {
                String title = titleField.getText().toString();

                // Dialog automatically closes on choice click; no need to manually do it
                if (title.isEmpty()) {
                    Misc.toast(requireView(), "Name cannot be empty!");
                } else {
                    playlistVM.createNewPlaylist(title, thisSong.getId());
                    Misc.toast(requireView(), "'" + title + "' created with '" + thisSong.getSongName() + "' added!");
                }
                MainActivity.hideKeyboardIn(requireView());
            });

            dialog.show(getParentFragmentManager(), "OK");
        }

        // Listens to 'Add to Existing Playlist' selection in the popup menu
        @Override
        public void onAddToExistingPlaylistSelected() {
            Song thisSong = playerVM.getCurrentSong();

            // Must cast to Playlist class before using parameter
            DialogAddToPlaylist dialog = new DialogAddToPlaylist(playlist -> {
                playlistVM.addSongToExistingPlaylist(
                        ((Playlist) playlist).getId(), thisSong.getId());
                Misc.toast(requireView(),
                        String.format("'%s' added to '%s'!",
                                thisSong.getSongName(), ((Playlist) playlist).getTitle()));
            });

            dialog.show(getParentFragmentManager(), "OK");
        }
    };

    // PlayerAdapter
    // posts new song to VM, grabs the song and prepares it
    private final OnRecyclerClickListener<Song> onRecyclerClickListener = new OnRecyclerClickListener<Song>() {
        @Override
        public void onItemClick(Song song) {
            songVM.select(song);
            playerVM.prepareSong(songVM.getSelectedSong().getValue());
            playerVM.play();
        }
    };

    @SuppressLint("NonConstantResourceId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // View Binding. Removes the need for 'findViewById(id)'
        B = FragmentPlayerBinding.inflate(inflater, container, false);

        // Navigation bar UI
        navBar = ((MainActivity) this.requireActivity()).findViewById(R.id.navBar);
        navBar.setVisibility(View.GONE);

        // UI
        ui = this.requireActivity().getWindow();

        handler = new Handler();

        // ViewModels
        songVM = new ViewModelProvider(requireActivity()).get(SongViewModel.class);
        playerVM = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        playlistVM = new ViewModelProvider(requireActivity()).get(PlaylistViewModel.class);

        // Initialises the queue
        playerVM.updateQueue(songVM.getSongs().getValue());
        playlistVM.getPlaylistsFromDb(Query.Direction.ASCENDING);

        // PlayerAdapter
        B.playerRecycler.setLayoutManager(new LinearLayoutManager(PlayerFragment.this.getContext()));
        playerAdapter = new PlayerAdapter(onRecyclerClickListener);
        B.playerRecycler.setAdapter(playerAdapter);

        // When there is a change in the queue orientation, run these callbacks.
        songVM.getSongs().observe(this, songs -> {
            playerAdapter.updateSongs(songs);
            B.playerRecycler.scrollToPosition(0);
            Anims.setLayoutAnimFall(B.playerRecycler);
        });

        playerVM.isPlaying.observe(this, this::onIsPlayingChange);
        playerVM.isLooping.observe(this, this::onIsLoopingChange);
        playerVM.isShuffling.observe(this, this::onIsShuffledChange);

        songVM.setOnLikedListener(onLikedListener);
        playerVM.setOnPlayerListener(onPlayerListener);
        B.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        B.btnPlaylistAdd.setOnClickListener(view ->
                PopupBuilder.forPlayer(view, onPlayerMenuItemSelected));

        // Extracts selected song details
        Song songData = songVM.getSelectedSong().getValue();
        String sID = songData.getId();
        String sTitle = songData.getSongName();
        String sArtist = songData.getArtistName();
        String sLink = songData.getSongLink();
        String sCoverArt = songData.getCoverArtLink();
        boolean sIsLiked = songData.isLiked;

        // Prepares the first-time set up of player.
        Song newSong = new Song(sID, sTitle, sArtist, sLink, sCoverArt, sIsLiked);
        playerVM.prepareSong(newSong);
        B.seekBar.setMax(playerVM.getDuration());
        B.etl.setText(getTimeFormat(playerVM.getCurrentPos()));
        B.rtl.setText(getTimeFormat(playerVM.getDuration() - playerVM.getCurrentPos()));

        // Button click listeners. They should only perform a single function.
        B.btnPlayPause.setOnClickListener(view -> playerVM.togglePlayPause());
        B.btnNext.setOnClickListener(view -> playerVM.playNext());
        B.btnPrevious.setOnClickListener(view -> playerVM.playPrevious());
        B.loopIcon.setOnClickListener(view -> playerVM.toggleLoop());
        B.shuffleIcon.setOnClickListener(view -> playerVM.toggleShuffle());
        B.btnLike.setOnClickListener(this::toggleLikeOnSong);

        playerVM.play();

        return B.getRoot();
    }

    private void onIsPlayingChange(Boolean isPlaying) {
        if (isPlaying) {
            handler.postDelayed(refreshDetails, 500);
            B.btnPlayPause.setImageResource(R.drawable.svg_btn_pause);
        } else {
            B.btnPlayPause.setImageResource(R.drawable.svg_btn_play);
            handler.removeCallbacks(refreshDetails);
        }
    }

    private void onIsLoopingChange(boolean isLooping) {
        B.loopIcon.setColorFilter(ContextCompat.getColor(
                requireActivity(),
                isLooping ? R.color.pinkDefault : R.color.white)
        );
    }

    private void onIsShuffledChange(boolean isShuffling) {
        B.shuffleIcon.setColorFilter(ContextCompat.getColor(
                requireActivity(),
                isShuffling ? R.color.pinkDefault : R.color.white
        ));
    }

    @Override
    public void onResume() {
        super.onResume();
        navBar.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        navBar.setVisibility(View.VISIBLE);
    }

    // updates the song title and artist name with current song data
    // background is set based on dominant colour of the song's cover art
    private void updateUiFromSong(Song song) {
        B.songHolder.setText(song.getSongName());
        B.artistHolder.setText(song.getArtistName());
        Picasso.get().load(song.getCoverArtLink()).into(B.coverArtHolder);
        // Gets dominant swatch from image and sets the view's background colour
        Picasso.get().load(song.getCoverArtLink()).into(swatchProcessor);
    }

    // If the current song is already liked, remove it from liked and vice versa.
    private void toggleLikeOnSong(View v) {
        Song thisSong = playerVM.getCurrentSong();
        if (thisSong.isLiked) {
            thisSong.setLiked(false);
            songVM.removeFromLiked(thisSong);
        } else {
            thisSong.setLiked(true);
            songVM.addToLiked(thisSong);
        }
    }

    // Updates the like button's ImageView based on the song's liked flag. Nothing much.
    private void setLikedBtnColourByFlag() {
        Song song = playerVM.getCurrentSong();
        if (song.isLiked) {
            B.btnLike.setImageResource(R.drawable.svg_heart_selected);
            return;
        }
        B.btnLike.setImageResource(R.drawable.svg_heart_unselected);
    }

    // anonymous inner class that updates time labels and seekBar progress every 0.5s
    final Runnable refreshDetails = new Runnable() {
        @Override
        public void run() {
            B.seekBar.setProgress(playerVM.getCurrentPos());
            B.rtl.setText(getTimeFormat(playerVM.getCurrentPos()));
            B.etl.setText(getTimeFormat(playerVM.getDuration() - playerVM.getCurrentPos()));
            handler.postDelayed(this, 500);
        }
    };

    // formats time labels
    // if elapsed seconds is less than 10, time labels = e.g. (2:0 + sec) = 2:07
    // otherwise time labels = e.g. (2: + elapsed seconds)
    private String getTimeFormat(int duration) {
        String timer;
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        timer = min + ":" + ((sec < 10) ? "0" + sec : sec);

        return timer;
    }

    // processes the dominant swatch of the current song's cover art
    // sets the background of the layout and status bar colours
    private final Target swatchProcessor = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Palette.from(bitmap).maximumColorCount(16).generate(palette -> {
                assert palette != null;
                int dominantSwatch = Objects.requireNonNull(palette.getDominantSwatch()).getRgb();
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