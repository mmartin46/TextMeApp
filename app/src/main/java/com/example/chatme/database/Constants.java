package com.example.chatme.database;

public class Constants {

    public static class Users {
        public final static String USER_COLLECTION_KEY = "users";
        public final static String USERNAME_KEY = "username";
        public final static String PASSWORD_KEY = "password";
        public final static String EMAIL_KEY = "email";
        public final static String ICON_KEY = "icon";
    }

    public static class Chat {
        public final static String CONVERSATION_COLLECTION_KEY = "conversations";

        public final static String SENDER_USERNAME = "sender_username";
        public final static String RECEIVER_USERNAME = "receiver_username";

        public final static String SENDER_MESSAGE = "sender_message";

        public final static String SENDER_MESSAGE_TIME_SENT = "sender_message_time_sent";

        public final static String RECEIVER_ICON = "receiver_icon";
        public final static String SENDER_ICON = "sender_icon";
    }

    public final static String NULL_VALUE = "null";

    public final static String CONFIRM_CODE = "confirm_code";
}