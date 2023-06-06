package com.example.musicbuddy;

import android.net.Uri;

public class MusicList {
    private String title, artist, durtion;
    private boolean isPlaying;
    private Uri musicFile;

    public MusicList(String title, String artist, String durtion, boolean isPlaying,Uri musicFile) {
        this.title = title;
        this.artist = artist;
        this.durtion = durtion;
        this.isPlaying = isPlaying;
        this.musicFile=musicFile;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDurtion() {
        return durtion;
    }

    public Uri getMusicFile() {
        return musicFile;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isPlaying() {
        return isPlaying;


    }
}
