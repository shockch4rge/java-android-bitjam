package com.example.bitjam.Models;

public class Song {
    private String mId;
    private String mTitle;
    private String mArtist;
    private String mFileLink;
    private String mCoverArt;
    public boolean isLiked;

    public Song() {
        // required empty constructor for Firestore
    }

    public Song(String id, String title, String artist, String fileLink, String coverArtLink, boolean isLiked) {
        mId = id;
        mTitle = title;
        mArtist = artist;
        mFileLink = fileLink;
        mCoverArt = coverArtLink;
        this.isLiked = isLiked;
    }

    public String getId() {
        return mId;
    }

    public String getSongName() {
        return mTitle;
    }

    public String getArtistName() {
        return mArtist;
    }

    public String getSongLink() {
        return mFileLink;
    }

    public String getCoverArtLink() {
        return mCoverArt;
    }

    public boolean getIsLiked() {
        return isLiked;
    }

    public void setLiked(boolean value) {
        isLiked = value;
    }


    /**
     *
     * @return An empty placeholder song for {@link com.example.bitjam.Utils.PureLiveData} instantiation
     */
    public static Song getEmptySong() {
        return new Song("", "", "", "", "", false);
    }
}