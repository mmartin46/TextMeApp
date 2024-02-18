package com.example.chatme.security;

import com.example.chatme.email.EmailProp;

import java.util.Random;

public class ConfirmCode {

    private final String SUBJECT = "TextMe - Confirmation Code";
    EmailProp emailProp;
    private String code;

    private void generateCode() {
        Random random = new Random();
        int codeAsInt;

        code = String.valueOf(random.nextInt(9999 - 1000 + 1) + 1000);
    }

    public String getCode() {
        return code;
    }

    private void setupMessage() {
        String message = "Please insert the following code to confirm your email.\n";
        message += code + "\n";
        emailProp.setMessage(message);
    }

    public ConfirmCode(String recipientEmail) {
        emailProp = new EmailProp();
        emailProp.setRecipientEmail(recipientEmail);
        emailProp.setSubject(SUBJECT);

        generateCode();
        setupMessage();
    }

    public EmailProp getEmail() {
        return emailProp;
    }
}
