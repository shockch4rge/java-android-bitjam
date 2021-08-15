package com.example.bitjam.viewmodels;


import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.bitjam.models.Playlist;
import com.example.bitjam.utils.PureLiveData;
import com.example.bitjam.models.Song;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SongViewModel extends ViewModel {
    private final String TAG = "(Firestore)";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final PureLiveData<List<Song>> myPermSongs = new PureLiveData<>(new ArrayList<>());
    private final PureLiveData<List<Song>> mySongs = new PureLiveData<>(new ArrayList<>());
    private final PureLiveData<List<Song>> myLikedSongs = new PureLiveData<>(new ArrayList<>());
    private final PureLiveData<Playlist> mSelectedPlaylist = new PureLiveData<>(Playlist.getEmpty());
    public final PureLiveData<Song> selectedSong = new PureLiveData<>(Song.getEmpty());
    private OnLikedListener mOnLikedListener;

    /**
     * Fetches all songs from Firestore and save into {@link #myPermSongs} ONE TIME.
     */
    public void getSongsFromDb() {
        db.collection("songs")
                .get()
                .addOnCompleteListener(task -> {
                    List<Song> tempSongs = new ArrayList<>();
                    // set db songs with localised instance
                    task.getResult().forEach(doc -> {
                        Song song = new Song(
                                doc.getId(),
                                doc.getString("title"),
                                doc.getString("artist"),
                                doc.getString("fileUrl"),
                                doc.getString("coverUrl"),
                                doc.getBoolean("isLiked"));

                        tempSongs.add(song);
                    });
                    if (myPermSongs.getValue().isEmpty()) {
                        myPermSongs.postValue(tempSongs);
                    }
                    mySongs.postValue(tempSongs);
                    Log.d(TAG, mySongs.getValue().size() + " added");
                })
                .addOnFailureListener(e -> Log.w(TAG, "Failed to grab songs from Firestore"));
    }

    /**
     * Adds a song to {@link #myLikedSongs}.
     *
     * @param song The song to add
     */
    public void addToLiked(Song song) {
        myLikedSongs.getValue().add(song);
        mOnLikedListener.onLike(song);
    }

    /**
     * Removes a song from {@link #myLikedSongs}.
     *
     * @param song The song to remove
     */
    public void removeFromLiked(Song song) {
        myLikedSongs.getValue().remove(song);
        mOnLikedListener.onUnlike(song);
    }

    /**
     * Selects a song to pass through the ViewModel.
     *
     * @param song The song to select
     */
    public void select(Song song) {
        selectedSong.setValue(song);
    }

    // Favour select (stream) more, but this is more readable
    private void oldSelect(Playlist playlist) {
        List<Song> queue = new ArrayList<>();

        for (DocumentReference songRef : playlist.getSongRefs()) {
            for (Song song : myPermSongs.getValue()) {
                if (song.getId().equals(songRef.getId())) {
                    queue.add(song);
                    break;
                }
            }
        }
        selectedSong.setValue(queue.get(0));
        mySongs.postValue(queue);
    }

    /**
     * Selects a playlist to pass through the ViewModel.
     *
     * @param playlist The playlist to select
     */
    public void select(Playlist playlist) {
        List<Song> permSongs = myPermSongs.getValue();
        List<Song> queue = new ArrayList<>();

        playlist.getSongRefs().forEach(songRef -> {
            Song matchedSong = permSongs.stream()
                    .filter(song -> song.getId().equals(songRef.getId()))
                    .findFirst()
                    .orElse(Song.getEmpty());

            queue.add(matchedSong);
        });
        mSelectedPlaylist.setValue(playlist);
        selectedSong.setValue(queue.get(0));
        mySongs.postValue(queue);
    }

    /**
     * @return A song that had to have been selected with {@link #select(Song)} before. Will return
     * {@link Song#getEmpty()} otherwise.
     */
    public PureLiveData<Song> getSelectedSong() {
        return selectedSong;
    }

    public PureLiveData<Playlist> getSelectedPlaylist() {
        return mSelectedPlaylist;
    }

    /**
     * Defaults the playlist title to 'All Songs'
     */
    public void clearSelectedPlaylist() {
        mSelectedPlaylist.setValue(Playlist.getEmpty());
    }

    /**
     * Global singleton of all songs.
     *
     * @return All songs
     */
    public PureLiveData<List<Song>> getSongs() {
        return mySongs;
    }

    /**
     * Global singleton of liked songs.
     *
     * @return All liked songs
     */
    public PureLiveData<List<Song>> getLikedSongs() {
        return myLikedSongs;
    }

    public interface OnLikedListener {
        // Listens for a song being liked
        void onLike(Song song);

        // Listens for a song being un-liked
        void onUnlike(Song song);
    }

    // Initialises the listener
    public void setOnLikedListener(OnLikedListener listener) {
        this.mOnLikedListener = listener;
    }
}
