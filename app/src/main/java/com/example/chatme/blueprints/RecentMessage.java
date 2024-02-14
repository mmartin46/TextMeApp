package com.example.chatme.blueprints;

import android.graphics.Bitmap;

import java.util.Date;

public class RecentMessage {

    private String recentSenderUsername;
    private Bitmap recentUserIcon;
    private String recentUserText;
    private Date recentUserTextTimeSent;

    public RecentMessage() {

    }

    public void setRecentSenderUsername(String recentSenderUsername) {
        this.recentSenderUsername = recentSenderUsername;
    }

    public String getRecentSenderUsername() {
        return recentSenderUsername;
    }

    public void setRecentUserIcon(Bitmap recentUserIcon) {
        this.recentUserIcon = recentUserIcon;
    }

    public Bitmap getRecentUserIcon() {
        return recentUserIcon;
    }

    public void setRecentUserText(String recentUserText) {
        this.recentUserText = recentUserText;
    }

    public String getRecentUserText() {
        return recentUserText;
    }

    public void setRecentUserTextTimeSent(Date recentUserTextTimeSent) {
        this.recentUserTextTimeSent = recentUserTextTimeSent;
    }

    public Date getRecentUserTextTimeSent() {
        return recentUserTextTimeSent;
    }

}
