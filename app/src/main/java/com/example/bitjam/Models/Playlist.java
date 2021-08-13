package com.example.bitjam.Models;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Playlist {
    private String title;
    private String id;
    private List<DocumentReference> songs;

    public Playlist() {
        // required empty constructor for Firestore
    }

    // We would use this when we want to create a new playlist. No song is needed.
    public Playlist(String title) {
        this.title = title;
    }

    // We would use this when querying for all the available playlists from Firestore. We want to
    // fetch all the particulars of each playlist as well.
    public Playlist(String id, String title, List<DocumentReference> songs) {
        this.id = id;
        this.title = title;
        this.songs = songs;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<DocumentReference> getSongRefs() {
        return songs;
    }
}
