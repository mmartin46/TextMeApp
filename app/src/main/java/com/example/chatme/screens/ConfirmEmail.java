package com.example.chatme.screens;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.CONFIRM_CODE;
import static com.example.chatme.database.Constants.Users.EMAIL_KEY;
import static com.example.chatme.database.Constants.Users.PASSWORD_KEY;
import static com.example.chatme.database.Constants.Users.USERNAME_KEY;
import static com.example.chatme.database.Constants.Users.USER_COLLECTION_KEY;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.chatme.ChangePassword;
import com.example.chatme.R;
import com.example.chatme.security.Requirements;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ConfirmEmail extends AppCompatActivity {

    private final int NUM_TRIES_THRESHOLD = 3;
    private AppCompatButton backToCreateScreenButton;
    private AppCompatButton createAccountButton;
    private TextView userCodeTextView;

    private String userEmail;
    private String userPassword;
    private int numTries;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_email);
        initializeAssets();
        findEmailInDatabase();
        checkListeners();
    }

    private void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    private String getUserPassword() {
        return userPassword;
    }

    public int getNumTries() {
        return numTries;
    }

    public void incNumTries() {
        ++numTries;
    }

    private void setNumTries(int numTries) {
        this.numTries = numTries;
    }

    private void findEmailInDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbRef = db.collection(USER_COLLECTION_KEY);

        String username = getIntent().getStringExtra(USERNAME_KEY);

        dbRef.whereEqualTo(USERNAME_KEY, username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            setUserEmail(document.getString(EMAIL_KEY));
                            setUserPassword(document.getString(PASSWORD_KEY));
                        }
                    }
                });
    }

    private void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    private String getUserEmail() {
        return userEmail;
    }



    private void initializeAssets() {
        setNumTries(0);
        initializeButtons();
        initializeUserCodeTextView();
    }
    private void initializeButtons() {
        backToCreateScreenButton = findViewById(R.id.backToPreviousButton);
        createAccountButton = findViewById(R.id.createAccountButton);
    }

    private void initializeUserCodeTextView() {
        userCodeTextView = findViewById(R.id.confirmCodeText);
    }

    private void checkListeners() {
        setBackToCreateScreenButtonListener();
        setCreateAccountButtonListener();
    }

    private void setBackToCreateScreenButtonListener() {
        backToCreateScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setCreateAccountButtonListener() {
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAuthenticCode()) {

                    // If the user tries to enter their password
                    // too many times, a different code will be sent
                    // to their email.
                    if (getNumTries() < NUM_TRIES_THRESHOLD) {
                        Intent intent = new Intent(ConfirmEmail.this, ChangePassword.class);
                        intent.putExtra(USERNAME_KEY, getIntent().getStringExtra(USERNAME_KEY));
                        intent.putExtra(PASSWORD_KEY, getIntent().getStringExtra(PASSWORD_KEY));

                        startActivity(intent);
                    } else {
                        makeToast("Too many attempts, please try again later.", getApplicationContext());
                    }
                } else {
                    if (getNumTries() < NUM_TRIES_THRESHOLD) {
                        makeToast("Incorrect code, please try again", getApplicationContext());
                        incNumTries();
                    } else {
                        makeToast("Too many attempts, please try again later.", getApplicationContext());
                    }
                }
            }
        });
    }

    private boolean isAuthenticCode() {
        String code = userCodeTextView.getText().toString();
        if (Requirements.hasAlpha(code)) {
            makeToast("The code contains only digits", this);
            return false;
        } else if (code.length() != Requirements.CODE_LENGTH) {
            makeToast("The code contains four digits", this);
            return false;
        } else {
            // FIXME: Check to confirm the code
            if (isCorrectCode(code)) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean isCorrectCode(String code) {
        String correctCode = getIntent().getStringExtra(CONFIRM_CODE);
        if (correctCode.equals(code)) {
            return true;
        }
        return false;
    }
}