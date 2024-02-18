package com.example.chatme.blueprints;

import android.graphics.Bitmap;

public class User {

    private String username;
    private Bitmap iconBitmap;
    private String recentMessage;

    public User(String username, Bitmap iconBitmap) {
        this.username = username;
        this.iconBitmap = iconBitmap;
    }

    public User(String username, Bitmap iconBitmap, String recentMessage) {
        this.username = username;
        this.iconBitmap = iconBitmap;
        this.recentMessage = recentMessage;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setIconBitmap(Bitmap icon) {
        this.iconBitmap = icon;
    }

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    public void setRecentMessage(String message) {
        this.recentMessage = message;
    }

    public String getRecentMessage() {
        return recentMessage;
    }



}
