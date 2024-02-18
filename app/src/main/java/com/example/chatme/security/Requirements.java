package com.example.chatme.security;

import android.widget.Toast;

public class Requirements {
    public static int USERNAME_MIN_LENGTH = 8;
    public static int PASSWORD_MIN_LENGTH = 10;
    public static int CODE_LENGTH = 4;

    public static int MAX_SAME_CHAR_FREQ = 5;


    public static boolean hasUppercaseLetter(String username) {
        for (char c : username.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsSpace(String username) {
        for (char c : username.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasGenericUsername(String username) {
        boolean sameCharacter = true;
        int numOfSameCharacter = 0;

        for (int i = 0; i < username.length() - 1; ++i) {
            if (username.charAt(i) != username.charAt(i + 1)) {
                sameCharacter = false;
            } else {
                numOfSameCharacter++;
            }
        }

        if (numOfSameCharacter >= MAX_SAME_CHAR_FREQ) {
            return true;
        }

        return sameCharacter;
    }

    public static boolean hasAtLeastTwoNumbers(String username) {
        int numberCount = 0;
        for (char c : username.toCharArray()) {
            if (Character.isDigit(c)) {
                numberCount++;
            }
        }
        if (numberCount >= 2) {
            return true;
        }
        return false;
    }

    public static boolean hasASpecialSymbol(String username) {
        for (char c : username.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAlpha(String username) {
        for (char c : username.toCharArray()) {
            if (Character.isAlphabetic(c)) {
                return true;
            }
        }
        return false;
    }
}
