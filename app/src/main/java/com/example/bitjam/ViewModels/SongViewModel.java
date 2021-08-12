package com.example.bitjam.ViewModels;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.bitjam.Models.Playlist;
import com.example.bitjam.Utils.PureLiveData;
import com.example.bitjam.Models.Song;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SongViewModel extends ViewModel {
    private final String TAG = "(Firestore)";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final PureLiveData<List<Song>> myLikedSongs = new PureLiveData<>(new ArrayList<>());
    private final PureLiveData<Song> mSelectedSong = new PureLiveData<>(Song.getEmptySong());
    private final PureLiveData<List<Song>> mySongs = new PureLiveData<>(new ArrayList<>());
    private final PureLiveData<List<Song>> myPermSongs = new PureLiveData<>(new ArrayList<>());
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
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        tempSongs.add(new Song(
                                doc.getId(),
                                doc.getString("title"),
                                doc.getString("artist"),
                                doc.getString("fileUrl"),
                                doc.getString("coverUrl"),
                                doc.getBoolean("isLiked")
                        ));
                    }
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
        mSelectedSong.setValue(song);
    }

    /**
     * Selects a playlist to pass through the ViewModel.
     *
     * @param playlist The playlist to select
     */
    public void select(Playlist playlist) {
        List<Song> songsInPlaylist = new ArrayList<>();

        for (DocumentReference songRef : playlist.getSongs()) {
            for (Song song : myPermSongs.getValue()) {
                if (song.getId().equals(songRef.getId())) {
                    songsInPlaylist.add(song);
                    break;
                }
            }
        }
        mSelectedSong.setValue(songsInPlaylist.get(0));
        mySongs.postValue(songsInPlaylist);
    }

    public void selectStream(Playlist playlist) {
        List<Song> songsInPlaylist = new ArrayList<>();
        playlist.getSongs()
                .forEach(songRef -> {
                    for (Song song : myPermSongs.getValue()) {
                        if (song.getId().equals(songRef.getId())) {
                            songsInPlaylist.add(song);
                            break;
                        }
                    }
                });
        mSelectedSong.setValue(songsInPlaylist.get(0));
        mySongs.setValue(songsInPlaylist);
    }

    /**
     * @return A song that had to have been selected with {@link #select(Song)} before. Will return
     * {@link Song#getEmptySong()} otherwise.
     */
    public PureLiveData<Song> getSelectedSong() {
        return mSelectedSong;
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
