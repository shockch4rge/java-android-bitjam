package com.example.bitjam.viewmodels;

import android.media.MediaPlayer;

import androidx.lifecycle.ViewModel;

import com.example.bitjam.utils.PureLiveData;
import com.example.bitjam.models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayingViewModel extends ViewModel {
    private final String TAG = "(PlayerViewModel)";
    private final MediaPlayer mp = new MediaPlayer();
    private final List<Song> mPermSongs = new PureLiveData<>(new ArrayList<Song>()).getValue();
    private final List<Song> mSongs = new PureLiveData<>(new ArrayList<Song>()).getValue();
    public final PureLiveData<Boolean> isPlaying = new PureLiveData<>(false);
    public final PureLiveData<Boolean> isShuffled = new PureLiveData<>(false);
    public final PureLiveData<Boolean> isLooping = new PureLiveData<>(false);
    public final PureLiveData<Song> currentSong = new PureLiveData<>(Song.getEmpty());
    private PlayerListener mPlayerListener;

    // Initialise self's completion listener
    public PlayingViewModel() {
        mp.setOnCompletionListener(mp -> mPlayerListener.onSongCompletion());
    }

    /**
     * Used for initialising a list of songs to play.
     *
     * @param songs The list of songs to play
     */
    // mPermSongs acts as a cache to store the original state of songs,
    // which mSongs sets itself to when un-shuffled.
    public void updateQueue(List<Song> songs) {
        mPermSongs.clear();
        mPermSongs.addAll(songs);
        mSongs.clear();
        mSongs.addAll(songs);
    }

    /**
     * Prepares the MediaPlayer with a specified song.
     *
     * @param song Set the current song to this.
     */
    public void prepareSong(Song song) {
        try {
            currentSong.setValue(song);
            mp.reset();
            mp.setDataSource(song.getSongUrl());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayerListener.onSongPrepared();
    }

    public void togglePlayPause() {
        if (mp.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void play() {
        mp.start();
        isPlaying.setValue(true);
    }

    public void pause() {
        mp.pause();
        isPlaying.setValue(false);
    }

    /**
     * Play the next index of the current list (can be currently shuffled).
     * If the next song index >= the current list's size, restart from index 0.
     */
    public void playNext() {
        int nextSongIndex = mSongs.indexOf(getCurrentSong()) + 1;

        if (nextSongIndex >= mSongs.size()) {
            // Restart from beginning of the queue
            nextSongIndex = 0;
        }

        prepareSong(mSongs.get(nextSongIndex));
        play();
        mPlayerListener.onNext();
    }

    public void playPrevious() {
        int prevSongIndex = mSongs.indexOf(getCurrentSong()) - 1;

        if (prevSongIndex < 0) {
            // Restart from beginning of the queue
            prevSongIndex = 0;
        } else if (getCurrentPos() >= 3000) {
            // Restart from the same song
            prevSongIndex += 1;
        }

        prepareSong(mSongs.get(prevSongIndex));
        play();
        mPlayerListener.onPrevious();
    }

    /**
     * @param time The time in milliseconds to seek to
     */
    public void moveTo(int time) {
        mp.seekTo(time);
    }

    public void toggleLoop() {
        if (mp.isLooping()) {
            disableLoop();
        } else {
            enableLoop();
        }
    }

    public void enableLoop() {
        mp.setLooping(true);
        isLooping.setValue(mp.isLooping());
    }

    public void disableLoop() {
        mp.setLooping(false);
        isLooping.setValue(mp.isLooping());
    }

    public void toggleShuffle() {
        if (isShuffled.getValue()) {
            disableShuffle();
        } else {
            enableShuffle();
        }
    }

    // mSongs is updated with a shuffled version of itself;
    // mPermSongs is unaffected.
    public void enableShuffle() {
        Collections.shuffle(mSongs);
        isShuffled.setValue(true);
        mPlayerListener.onShuffled(mSongs);
    }

    public void disableShuffle() {
        isShuffled.setValue(false);
        mPlayerListener.onUnshuffled(mPermSongs);
    }

    /**
     * @return First song that matches the current song's ID.
     */
    public Song getCurrentSong() {
        return mSongs.stream()
                .filter(song -> song.getId().equals(currentSong.getValue().getId()))
                .findFirst()
                .orElse(Song.getEmpty());
    }

    // Don't use this; when playNext is called, it plays the same song once more.
    // Use getCurrentSong instead.
    private Song getThisSong() {
        return currentSong.getValue();
    }

    /**
     * @return Duration of the current file
     * @throws IllegalStateException The file is invalid
     */
    public int getDuration() {
        int duration = mp.getDuration();

        if (duration == -1) {
            throw new IllegalStateException(TAG + " MediaPlayer file URL is not valid.");
        }

        return duration;
    }

    public int getCurrentPos() {
        return mp.getCurrentPosition();
    }

    // PlayerFragment implements these listeners
    public interface PlayerListener {

        void onSongPrepared();

        // Runs when playNext() is called
        void onNext();

        // Runs when playPrevious() is called
        void onPrevious();

        // Runs when the list is in a shuffled state
        void onShuffled(List<Song> songs);

        // Runs when the list is in its original state
        void onUnshuffled(List<Song> songs);

        // Initialised in the constructor.
        // Runs when mp.onCompleteListener() is called.
        void onSongCompletion();
    }

    // initialises the listener
    public void setPlayerListener(PlayerListener listener) {
        this.mPlayerListener = listener;
    }
}
