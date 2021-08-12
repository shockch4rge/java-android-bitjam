package com.example.bitjam.Models;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Playlist {
    private String mTitle;
    private String mId;
    private List<DocumentReference> mSongs;

    public Playlist() {
        // required empty constructor for Firestore
    }

    // We would use this when we want to create a new playlist. No song is needed.
    public Playlist(String title) {
        mTitle = title;
    }

    // We would use this when querying for all the available playlists from Firestore. We want to
    // fetch all the particulars of each playlist as well.
    public Playlist(String id, String title, List<DocumentReference> songs) {
        mId = id;
        mTitle = title;
        mSongs = songs;
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public List<DocumentReference> getSongs() {
        return mSongs;
    }
}
