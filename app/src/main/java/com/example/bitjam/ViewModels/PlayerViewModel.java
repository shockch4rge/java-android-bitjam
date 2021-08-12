package com.example.bitjam.ViewModels;

import android.media.MediaPlayer;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.bitjam.Utils.PureLiveData;
import com.example.bitjam.Models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerViewModel extends ViewModel {
    private final String TAG = "(PlayerViewModel)";
    private final MediaPlayer mp = new PureLiveData<>(new MediaPlayer()).getValue();
    private final List<Song> mPermSongs = new PureLiveData<>(new ArrayList<Song>()).getValue();
    private final List<Song> mSongs = new PureLiveData<>(new ArrayList<Song>()).getValue();
    public final PureLiveData<Boolean> isPlaying = new PureLiveData<>(false);
    public final PureLiveData<Boolean> isShuffling = new PureLiveData<>(false);
    public final PureLiveData<Boolean> isLooping = new PureLiveData<>(false);
    private final PureLiveData<Song> currentSong = new PureLiveData<>(Song.getEmptySong());
    private final Random random = new Random();
    private OnPlayerListener mPlayerListener;

    // Rather than stuff everything into one method, we should separate responsibilities
    // across multiple ones. This allows for more modularity over our code and helps to reduce
    // coupling. I have extracted a few functions in this class, but there is much more de-linking
    // to go.

    // Constructor to initialise MediaPlayer's setOnCompletionListener().
    public PlayerViewModel() {
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
    // MediaPlayer has to be reset, assigned a file URL to play and then prepared to be able to
    // use it. We can do all three functions in the same method as they fall under the category of
    // initialising MediaPlayer, and catch all throwables should they appear. Try/catch blocks are
    // generally very messy, so separating them from the important stuff is recommended.
    public void prepareSong(Song song) {
        try {
            currentSong.setValue(song);
            mp.reset();
            mp.setDataSource(currentSong.getValue().getSongLink());
            mp.prepare();
        } catch (IOException | IllegalStateException | IllegalArgumentException | SecurityException | NullPointerException e) {
            e.printStackTrace();
        }
        mPlayerListener.onSongPrepared(currentSong.getValue());
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
        shuffle(mSongs);
        isShuffling.setValue(true);
        mPlayerListener.onShuffled(mSongs);
    }

    public void disableShuffle() {
        isShuffling.setValue(false);
        mPlayerListener.onUnshuffled(mPermSongs);
    }

    /**
     * @return A song that has the same id as the current song
     */
    // Java's stream method in Collection is incredibly intuitive, allowing a 100% success rate
    // of matching particulars of a certain object. This method queries through mSongs, until it
    // finds a song that has the same ID as the current song. It then returns that queried song.
    public Song getCurrentSong() {
        return mSongs.stream()
                .filter(song -> song.getId().equals(currentSong.getValue().getId()))
                .findFirst()
                .orElse(null);
    }

    // This method returns the current song faster than getCurrentSong, but does not perform
    // a stream filter. Not recommended to use.
    public Song getThisSong() {
        return currentSong.getValue();
    }

    public int getDuration() {
        return mp.getDuration();
    }

    public int getCurrentPos() {
        return mp.getCurrentPosition();
    }

    /**
     * Assigns the {@code songs} parameter's size to an integer {@code n}.
     * <br>
     * Then, iterate through the list size while {@code int i < n}.
     * <br>
     * Store a random integer {@code change} with a boundary of 0 to {@code n - i}.
     * <br>
     * Refer to {@link #swap(List, int, int)} for the swapping algorithm.
     *
     * @param songs the list of songs to process
     */
    // Of course, we could just use Collections.shuffle... but what fun would that be?
    private List<Song> shuffle(List<Song> songs) {
        int n = songs.size();
        for (int i = 0; i < n; i++) {
            int change = random.nextInt(n - i);
            swap(songs, i, change);
        }
        return songs;
    }

    /**
     * A helper method for {@link #shuffle(List)}.
     * <br>
     * A list of songs is processed, then a song is grabbed at the 'i' index and stored in a temp field.
     * <br>
     * Once a value is assigned, replace the 'i' index of the list with the song at 'change' index.
     * <br>
     * Then, swap the song at 'change' index with the temp song field.
     * <br>
     * To be used in conjunction with a {@code for} loop for modularity.
     *
     * @param songs  the list of songs to process
     * @param i      index that gets a temp song
     * @param change swaps i, and swaps the temp song
     */
    private void swap(List<Song> songs, int i, int change) {
        Song helper = songs.get(i);
        songs.set(i, songs.get(change));
        songs.set(change, helper);
    }

    // PlayerFragment implements these listeners
    public interface OnPlayerListener {
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

        void onSongPrepared(Song preparedSong);
    }

    // initialises the listener
    public void setOnPlayerListener(OnPlayerListener listener) {
        this.mPlayerListener = listener;
    }
}
