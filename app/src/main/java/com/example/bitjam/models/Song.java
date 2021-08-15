package com.example.bitjam.models;

public class Song {
    private String id;
    private String title;
    private String artist;
    private String songUrl;
    private String coverUrl;
    public boolean isLiked;

    public Song() {
        // required empty constructor for Firestore
    }

    public Song(String id, String title, String artist, String songUrl, String coverUrl, boolean isLiked) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.songUrl = songUrl;
        this.coverUrl = coverUrl;
        this.isLiked = isLiked;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public boolean getIsLiked() {
        return isLiked;
    }

    public void setLiked(boolean value) {
        isLiked = value;
    }


    /**
     *
     * @return An empty placeholder song for {@link com.example.bitjam.utils.PureLiveData} instantiation
     */
    public static Song getEmpty() {
        return new Song("", "", "", "", "", false);
    }
}