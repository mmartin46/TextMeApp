package com.example.chatme.security;

import com.example.chatme.BuildConfig;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(BuildConfig.hashalgorithm);
            byte[] getBytes = digest.digest(password.getBytes());

            StringBuilder hexPassword = new StringBuilder();
            for (byte b : getBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexPassword.append('O');
                }
                hexPassword.append(hex);
            }
            return hexPassword.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
