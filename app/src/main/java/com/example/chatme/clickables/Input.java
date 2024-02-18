package com.example.chatme.clickables;

import android.content.Context;
import android.widget.Toast;

public class Input {
    public static enum WhichText {
        USERNAME,
        PASSWORD,
        EMAIL,
        CONFIRM_EMAIL,
        CONFIRM_PASSWORD,

        CONFIRM_USERNAME, PROFILE_HEADER
    }

    public static enum WhichButton {
        SUBMIT,
        FORGOT_PASSWORD,
        CREATE_ACCOUNT,
        BACK_BUTTON,
        CONFIRM_EMAIL,
        SELECT_IMAGE,
        EDIT_PROFILE,
        ADD_USER, SEND_MESSAGE_BUTTON, CONFIRM_USERNAME, SEARCH_BUTTON
    }

    public static void makeToast(CharSequence text, Context context) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
