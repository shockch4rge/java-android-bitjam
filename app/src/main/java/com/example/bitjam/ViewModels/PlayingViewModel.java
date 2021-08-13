package com.example.bitjam.ViewModels;

import android.media.MediaPlayer;

import androidx.lifecycle.ViewModel;

import com.example.bitjam.Utils.PureLiveData;
import com.example.bitjam.Models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PlayingViewModel extends ViewModel {
    private final String TAG = "(PlayerViewModel)";
    private final MediaPlayer mp = new MediaPlayer();
    private final List<Song> mPermSongs = new PureLiveData<>(new ArrayList<Song>()).getValue();
    private final List<Song> mSongs = new PureLiveData<>(new ArrayList<Song>()).getValue();
    public final PureLiveData<Boolean> isPlaying = new PureLiveData<>(false);
    public final PureLiveData<Boolean> isShuffling = new PureLiveData<>(false);
    public final PureLiveData<Boolean> isLooping = new PureLiveData<>(false);
    public final PureLiveData<Song> currentSong = new PureLiveData<>(Song.getEmpty());
    private final Random random = new Random();
    private PlayerListener mPlayerListener;

    // Rather than stuff everything into one method, we should separate responsibilities
    // across multiple ones. This allows for more modularity over our code and helps to reduce
    // coupling. I have extracted a few functions in this class, but there is much more de-linking
    // to go.

    // Initialise listener without calling a method
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
        } catch (IOException | IllegalStateException | IllegalArgumentException | SecurityException | NullPointerException e) {
            e.printStackTrace();
        }
        mPlayerListener.onSongPrepared();
    }

    public void togglePlayPause() {
        if (mp.isPlaying()) {
            pause();
            return;
        }
        play();
    }

    // Simple play method without having to worry about MediaPlayer's state or current song index.
    public void play() {
        mp.start();
        isPlaying.setValue(true);
    }

    // Same as play.
    public void pause() {
        mp.pause();
        isPlaying.setValue(false);
    }

    /**
     * Play the next index of the current list (can be currently shuffled).
     * If the next song index >= the current list's size, restart from index 0.
     */
    // Here, coupling can be reduced by making playNext play the next song without any checking
    // of the song's current index. This separates the responsibility of playNext always needing to
    // know of the song's current index/the song itself.
    public void playNext() {
        int nextSongIndex = mSongs.indexOf(getCurrentSong()) + 1;
        if (nextSongIndex >= mSongs.size()) {
            nextSongIndex = 0;
        }
        prepareSong(mSongs.get(nextSongIndex));
        play();
        mPlayerListener.onNext();
    }

    /**
     * If the previous song index is less than 0 or the elapsed time is >= 3s, just replay the song.
     */
    // In conjunction with playNext, more coupling can be reduced by simply making playPrevious play
    // the next song without checking the position of the player or the index location.
    // getCurrentPos should not be associated with PlayerViewModel. However, just for
    // convenience's sake, I have decided to go along with the coupling as it's not a severe issue.
    public void playPrevious() {
        int prevSongIndex = mSongs.indexOf(getCurrentSong()) - 1;
        if (prevSongIndex < 0) {
            prevSongIndex = 0;
        } else if (getCurrentPos() >= 3000) {
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
            return;
        }
        enableLoop();
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
        if (isShuffling.getValue()) {
            disableShuffle();
            return;
        }
        enableShuffle();
    }

    // mSongs is updated with a shuffled version of itself;
    // mPermSongs is unaffected.
    public void enableShuffle() {
        Collections.shuffle(mSongs);
        isShuffling.setValue(true);
        mPlayerListener.onShuffled(mSongs);
    }

    public void disableShuffle() {
        isShuffling.setValue(false);
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
