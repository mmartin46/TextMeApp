package com.example.chatme.manipulation;

import static com.example.chatme.database.Constants.NULL_VALUE;
import static com.example.chatme.database.Constants.Users.ICON_KEY;
import static com.example.chatme.manipulation.BitmapHandler.HALF_ROTATION_RIGHT;
import static com.example.chatme.manipulation.BitmapHandler.rotateBitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Modifiers {
    public static String formatTime(Date date) {
        Format formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
        return formatter.format(date);
    }

    public static Bitmap deserializeIcon(String serializedIcon) {
        if (!serializedIcon.equals(NULL_VALUE)) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                byte[] decodedByteArray = Base64.decode(serializedIcon, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                decodedBitmap = rotateBitmap(decodedBitmap, HALF_ROTATION_RIGHT);
                // Set the image bitmap.
                return decodedBitmap;
            }
        }
        return null;
    }

    public static Bitmap getDeserializedIcon(QueryDocumentSnapshot document) {
        String userIconSerialized = document.getString(ICON_KEY);
        Bitmap userIconDeserialized = deserializeIcon(userIconSerialized);
        return userIconDeserialized;
    }
}
