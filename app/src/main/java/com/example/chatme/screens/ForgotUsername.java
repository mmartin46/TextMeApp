package com.example.chatme.screens;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.Users.EMAIL_KEY;
import static com.example.chatme.database.Constants.Users.USERNAME_KEY;
import static com.example.chatme.database.Constants.Users.USER_COLLECTION_KEY;
import static com.example.chatme.email.EmailHandler.sendEmailAsync;

import android.os.Bundle;
import android.view.View;

import com.example.chatme.R;
import com.example.chatme.blueprints.BaseActivity;
import com.example.chatme.clickables.Input;
import com.example.chatme.email.EmailHandler;
import com.example.chatme.email.EmailProp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

public class ForgotUsername extends BaseActivity {

    private String username;
    private final String USERNAME_SUBJECT = "Text Me - Lost Username";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_username);
        initializeAssets();
        buttonClickedListener();
    }


    @Override
    public void initializeInputViews() {
        inputViews = new HashMap<>();
        inputViews.put(Input.WhichText.CONFIRM_USERNAME, findViewById(R.id.confirmUsernameButton));
        inputViews.put(Input.WhichText.EMAIL, findViewById(R.id.emailText));
    }

    @Override
    public void initializeInputButtons() {
        inputButtons = new HashMap<>();
        inputButtons.put(Input.WhichButton.CONFIRM_USERNAME, findViewById(R.id.confirmEmailButton));
        inputButtons.put(Input.WhichButton.BACK_BUTTON, findViewById(R.id.backToPreviousButton));
    }

    @Override
    public void buttonClickedListener() {
        confirmUsernameButtonListener();
        backButtonListener();
    }

    public void confirmUsernameButtonListener() {
        inputButtons.get(Input.WhichButton.CONFIRM_USERNAME).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME: Implement the checking for the username.
                String userEmail = inputViews.get(Input.WhichText.EMAIL).getText().toString();
                findUserEmail(userEmail, new UserEmailCallback() {
                    @Override
                    public void onUserEmailFound(String username) {
                        // Link the username.
                        setUsername(username);
                        if (userEmail != null) {
                            sendEmailAsync(getUsernameEmail(userEmail), new EmailHandler.EmailCallback() {
                                @Override
                                public void onEmailSent(boolean success) {
                                    if (success) {

                                    } else {
                                        makeToast("A problem occurred. Please try again later.", getApplicationContext());
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onInvalidEmailAddress() {
                        makeToast("Invalid email address", getApplicationContext());
                    }

                    @Override
                    public void onProblemLocatingEmail() {
                        makeToast("Problem locating the email", getApplicationContext());
                    }
                });
                makeToast("Please check your email for the associated username", getApplicationContext());
            }
        });
    }


    public EmailProp getUsernameEmail(String userEmail) {
        EmailProp emailProp = new EmailProp();
        emailProp.setRecipientEmail(userEmail);
        emailProp.setSubject(USERNAME_SUBJECT);
        emailProp.setMessage("The username associated with your TextMe Account is \"" + getUsername() + "\".");
        return emailProp;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    /*
        Asynchronous method that locates the username
        within the database.
     */
    public void findUserEmail(String userEmail, UserEmailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbRef = db.collection(USER_COLLECTION_KEY);

        // Find user with a matching email.
        Query emailQuery = dbRef.whereEqualTo(EMAIL_KEY, userEmail);

        emailQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Get the email
                    String realEmail = document.getString(EMAIL_KEY);


                    // If the emails match, it's valid.
                    if (realEmail.equals(userEmail)) {
                        callback.onUserEmailFound(document.getString(USERNAME_KEY));
                        return;
                    } else {
                        // The email is invalid.
                        callback.onInvalidEmailAddress();
                    }
                }
            } else {
                // If we couldn't even get a successful query.
                callback.onProblemLocatingEmail();
            }
        });
    }

    public interface UserEmailCallback {
        void onUserEmailFound(String username);
        void onInvalidEmailAddress();
        void onProblemLocatingEmail();
    }

    public void backButtonListener() {
        inputButtons.get(Input.WhichButton.BACK_BUTTON).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the previous page.
                finish();
            }
        });
    }
}