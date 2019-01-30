package com.tritonsdk.impl;

import java.io.Serializable;

public class Track implements Serializable {
    private String title;
    private String artist;
    private boolean ads;

    public Track() {

    }

    public Track(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public Track(boolean ads) {
        this.ads = true;
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

    public boolean isAds() {
        return ads;
    }

    public void setAds(boolean ads) {
        this.ads = ads;
    }
}
