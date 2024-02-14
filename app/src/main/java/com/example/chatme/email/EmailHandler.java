package com.example.chatme.email;

import static com.example.chatme.clickables.Input.makeToast;

import android.content.res.Resources;
import android.os.AsyncTask;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chatme.BuildConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class EmailHandler {

    private static String email = BuildConfig.email;


    private static String password = BuildConfig.password;

    private static void setGmailServer(Properties props) {

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

    }




    public static boolean sendEmail(EmailProp emailProp) {
        // Setup mail server props.
        Properties props = new Properties();
        setGmailServer(props);

        // Check credentials
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailProp.getRecipientEmail()));
            message.setSubject(emailProp.getSubject());
            message.setText(emailProp.getMessage());

            Transport.send(message);

            return true;
        } catch (AddressException e) {
            System.err.println("Couldn't find email -> " + emailProp.getRecipientEmail());
            return false;
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Couldn't send message");
            return false;
        }
    }

    public static void sendEmailAsync(final EmailProp emailProp, final EmailCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {

            // Performs the actual email sending.
            @Override
            protected Boolean doInBackground(Void... voids) {
                return sendEmail(emailProp);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (callback != null) {
                    callback.onEmailSent(success);
                }
            }
        }.execute();
    }

    public interface EmailCallback {
        // Called when the email is sent.
        void onEmailSent(boolean success);
    }


    public static void sendEmail(String recipientEmail, String subject, String message) {
        sendEmail(new EmailProp(recipientEmail, subject, message));
    }
}
