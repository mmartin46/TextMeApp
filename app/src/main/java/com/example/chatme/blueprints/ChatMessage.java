package com.example.chatme.blueprints;

import android.graphics.Bitmap;

import java.util.Date;

public class ChatMessage {


    private String senderUsername;
    private String senderMessage;
    private Date senderMessageTimeSent;
    private String receiverUsername;

    private Bitmap senderBitmap;
    private Bitmap receiverBitmap;





    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderMessage() {
        return senderMessage;
    }

    public void setSenderMessage(String senderMessage) {
        this.senderMessage = senderMessage;
    }

    public Date getSenderMessageTimeSent() {
        return senderMessageTimeSent;
    }

    public void setSenderMessageTimeSent(Date senderMessageTimeSent) {
        this.senderMessageTimeSent = senderMessageTimeSent;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }




    public void setSenderBitmap(Bitmap senderBitmap) {
        this.senderBitmap = senderBitmap;
    }

    public Bitmap getSenderBitmap() {
        return senderBitmap;
    }

    public void setReceiverBitmap(Bitmap receiverBitmap) {
        this.receiverBitmap = receiverBitmap;
    }

    public Bitmap getReceiverBitmap() {
        return receiverBitmap;
    }


}
