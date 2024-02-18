package com.example.chatme.email;

public class EmailProp {

    private String recipientEmail;
    private String message;
    private String subject;

    public EmailProp() {

    }

    public EmailProp(String recipientEmail, String subject, String message) {
        this.recipientEmail = recipientEmail;
        this.message = message;
        this.subject = subject;
    }

    public EmailProp(EmailProp emailProp) {
        this.recipientEmail = emailProp.getRecipientEmail();
        this.message = emailProp.getMessage();
        this.subject = emailProp.getSubject();
    }



    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
