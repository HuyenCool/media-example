package com.example.mediaexample;

import android.graphics.Bitmap;

public class Song {
    private int id;
    private String title;
    private String artist;
    private Bitmap image;

    public Song(int id, String title, String artist, Bitmap image) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.image = image;
    }

    public Song() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
