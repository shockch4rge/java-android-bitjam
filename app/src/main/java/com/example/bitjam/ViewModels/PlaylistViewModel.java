package com.example.bitjam.ViewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.bitjam.Models.Playlist;
import com.example.bitjam.Models.Song;
import com.example.bitjam.Utils.PureLiveData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewModel extends ViewModel {
    private final String TAG = "(BitJam)";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final PureLiveData<List<Playlist>> myPlaylists = new PureLiveData<>(new ArrayList<>());

    /**
     * Fetch all the playlists from Firestore and save it into myPlaylists.
     *
     * @param sort The sorting order of the fetching. Can be {@link Query.Direction ASCENDING or DESCENDING}.
     */
    // myPlaylists is merely a localised instance of all the playlists from Firestore.
    // This makes updating the UI much easier, as all songs are added at the same time, rather than
    // waiting to iterate through the database.
    public void getPlaylistsFromDb(Query.Direction sort) {
        db.collection("playlists")
                .orderBy("title", sort)
                .get()
                .addOnCompleteListener(task -> {
                    List<Playlist> tempPlaylists = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        tempPlaylists.add(new Playlist(
                                doc.getId(),
                                doc.getString("title"),
                                (List<DocumentReference>) doc.get("songs")
                        ));
                    }
                    myPlaylists.setValue(tempPlaylists);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Failed to grab playlists from Firestore"));
    }

    /**
     * Creates a new playlist with a compulsory song.
     * This option is only shown when the user wants to add a song to a new playlist.
     *
     * @param title  The title of the playlist
     * @param songId The compulsory to add
     */
    // In order to get around some Firestore quirks, creating a new playlist requires the user to
    // include a song along with the name of the new playlist. This may seem unintuitive, but it
    // also helps to reduce a significant amount of code that might deem unnecessary.
    public void createNewPlaylist(String title, String songId) {
        Playlist playlist = new Playlist(title);
        db.collection("playlists")
                .add(playlist)
                .addOnSuccessListener(documentReference ->
                        addSongToExistingPlaylist(documentReference.getId(), songId))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

    /**
     * Adds a song to a specified existing playlist.
     *
     * @param playlistId The playlist ID to add the song to
     * @param songId     The song ID to add
     */
    // While constructing an entirely new song object with the same particulars in Firestore may be
    // easier and faster, we would rather get the existing song's document reference / file path
    // in the database. This will make working with Firestore very tedious, but this ensures that
    // we always query the same song object, as it might have flags that should be persistent across
    // the app, e.g. whether the song is liked.
    public void addSongToExistingPlaylist(String playlistId, String songId) {
        // gets specified doc refs
        DocumentReference songRef =
                db.collection("songs")
                        .document(songId);
        DocumentReference playlistRef =
                db.collection("playlists")
                        .document(playlistId);

        // add the song ref to the existing playlist
        playlistRef.update("songs", FieldValue.arrayUnion(songRef))
                .addOnSuccessListener(none -> Log.d(TAG, String.format("(%s) added to [%s]", songRef.getId(), playlistRef.getId())))
                .addOnFailureListener(e -> Log.w(TAG, String.format("Failed to append (%s) to [%s]", songRef.getId(), playlistRef.getId()) , e));
    }

    /**
     * Deletes a playlist at a specified path.
     *
     * @param documentPath The playlist path to delete
     */
    // We can delete a playlist in Firestore by referring to its document path. Nothing much to say.
    public void deletePlaylistInDb(String documentPath) {
        db.collection("playlists")
                .document(documentPath)
                .delete()
                .addOnSuccessListener(none -> Log.d(TAG, "Deleted " + documentPath + " from Firestore"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to delete " + documentPath + " in Firestore", e));
    }

    public PureLiveData<List<Playlist>> getPlaylists() {
        return myPlaylists;
    }
}
