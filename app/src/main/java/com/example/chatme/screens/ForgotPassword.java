package com.example.chatme.screens;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.CONFIRM_CODE;
import static com.example.chatme.database.Constants.Users.EMAIL_KEY;
import static com.example.chatme.database.Constants.Users.PASSWORD_KEY;
import static com.example.chatme.database.Constants.Users.USERNAME_KEY;
import static com.example.chatme.database.Constants.Users.USER_COLLECTION_KEY;
import static com.example.chatme.email.EmailHandler.sendEmail;
import static com.example.chatme.email.EmailHandler.sendEmailAsync;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.chatme.R;
import com.example.chatme.email.EmailHandler;
import com.example.chatme.email.EmailProp;
import com.example.chatme.security.ConfirmCode;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ForgotPassword extends AppCompatActivity {

    private TextView usernameTextView;

    private AppCompatButton confirmUsernameButton;
    private AppCompatButton backToLoginButton;
    private AppCompatButton forgotUsernameButton;

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    private String recipientEmail;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        initializeAssets();
        initializeButtonListeners();
    }

    private void initializeAssets() {
        usernameTextView = findViewById(R.id.usernameText);

        initializeButtons();
    }

    private void initializeButtons() {
        confirmUsernameButton = findViewById(R.id.confirmUsernameButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);
        forgotUsernameButton = findViewById(R.id.forgotUsernameButton);
    }

    private void initializeButtonListeners() {
        usernameButtonListener();
        backToLoginButtonListener();
        forgotUsernameButtonListener();
    }

    private void usernameButtonListener() {

        confirmUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidUsername()) {
                    Intent intent = new Intent(ForgotPassword.this, ConfirmEmail.class);
                    String username = usernameTextView.getText().toString();

                    findUserEmail(username, new FindUserEmailCallback() {
                        @Override
                        public void onUserEmailFound(String userEmail) {
                            // If the user enters a username that isn't valid, don't allow them
                            // access.

                            if (userEmail != null) {
                                ConfirmCode emailCode = new ConfirmCode(getRecipientEmail());
                                sendEmailAsync(new EmailProp(emailCode.getEmail()), new EmailHandler.EmailCallback() {
                                    @Override
                                    public void onEmailSent(boolean success) {
                                        if (success) {
                                            System.out.println("Email Sent");
                                        } else {
                                            System.out.println("Email Failed To Send");
                                        }
                                    }
                                });


                                intent.putExtra(USERNAME_KEY, username);
                                intent.putExtra(PASSWORD_KEY, getPassword());
                                intent.putExtra(CONFIRM_CODE, emailCode.getCode());
                                startActivity(intent);
                            } else {
                                makeToast("Username doesn't exist", getApplicationContext());
                            }
                        }
                    });
                } else {
                    makeToast("Username doesn't exist", getApplicationContext());
                }
            }
        });
    }

    private void setPassword(String password) {
        this.password = password;
    }

    private String getPassword() {
        return password;
    }

    private void findUserEmail(String username, FindUserEmailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbRef = db.collection(USER_COLLECTION_KEY);

        dbRef.whereEqualTo(USERNAME_KEY, username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            setRecipientEmail(document.getString(EMAIL_KEY));
                            setPassword(document.getString(PASSWORD_KEY));

                            // Notify the callback function that an email has been
                            // found.
                            callback.onUserEmailFound(document.getString(PASSWORD_KEY));
                        }

                        if (getRecipientEmail() == null) {
                            makeToast("Username doesn't exist", this);
                        }
                    } else {
                        callback.onUserEmailFound(null);
                        makeToast("Couldn't find email", this);
                    }
                });
    }


    private void forgotUsernameButtonListener() {
        forgotUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPassword.this, ForgotUsername.class);
                startActivity(intent);
                makeToast("This feature isn't fully implemented", getApplicationContext());
            }
        });
    }

    /*
     The FindUserEmailCallback should be called due to the search
     for the email being an asynchronous task,
     */
    private interface FindUserEmailCallback {

        // Implement how the callback function should react
        // if the email has been found.
        void onUserEmailFound(String userEmail);
    }

    private void backToLoginButtonListener() {
        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean isValidUsername() {
        String usernameText = (String) usernameTextView.getText().toString();
        if (usernameText.length() == 0) {
            makeToast("Invalid Username", this);
            return false;
        }
        return true;
    }



}