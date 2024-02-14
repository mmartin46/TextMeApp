package com.example.chatme;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.Users.PASSWORD_KEY;
import static com.example.chatme.database.Constants.Users.USERNAME_KEY;
import static com.example.chatme.database.Constants.Users.USER_COLLECTION_KEY;
import static com.example.chatme.security.Hashing.hashPassword;
import static com.example.chatme.security.Requirements.containsSpace;
import static com.example.chatme.security.Requirements.hasASpecialSymbol;
import static com.example.chatme.security.Requirements.hasAtLeastTwoNumbers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatme.blueprints.BaseActivity;
import com.example.chatme.clickables.Input;
import com.example.chatme.screens.MainScreen;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

public class ChangePassword extends BaseActivity {

    private String oldPassword;
    private String newPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initializeAssets();
        buttonClickedListener();
    }

    private String getOldPassword() {
        return oldPassword;
    }

    private void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    private void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    private String getNewPassword() {
        return newPassword;
    }



    @Override
    public void initializeInputViews() {
        inputViews = new HashMap<>();
        inputViews.put(Input.WhichText.PASSWORD, findViewById(R.id.passwordText));
        inputViews.put(Input.WhichText.CONFIRM_PASSWORD, findViewById(R.id.confirmPasswordText));
        locateOldPassword();
    }

    private void configureNewPassword(String newPassword) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbRef = db.collection(USER_COLLECTION_KEY);

        String usernameOfLastScreen = getIntent().getStringExtra(USERNAME_KEY);

        dbRef.whereEqualTo(USERNAME_KEY, usernameOfLastScreen)
                .get()
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       for (QueryDocumentSnapshot document : task.getResult()) {

                           DocumentReference docRef = db.collection(USER_COLLECTION_KEY).document(document.getId());
                           docRef.update(PASSWORD_KEY, newPassword)
                                   .addOnSuccessListener(s -> makeToast("Password Successfully Changed", this))
                                   .addOnFailureListener(e -> makeToast("Failed to change password", this));

                           setNewPassword(document.getString(PASSWORD_KEY));
                       }
                   } else {
                       makeToast("Unable to change the password at this time.", this);
                   }
                });
    }


    private void locateOldPassword() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbRef = db.collection(USER_COLLECTION_KEY);

        String usernameOfLastScreen = getIntent().getStringExtra(USERNAME_KEY);


        dbRef.whereEqualTo(USERNAME_KEY, usernameOfLastScreen)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            setOldPassword(document.getString(PASSWORD_KEY));
                        }
                    } else {
                        makeToast("Unable to locate old password", this);
                    }
                });
    }

    @Override
    public void initializeInputButtons() {
        inputButtons = new HashMap<>();
        inputButtons.put(Input.WhichButton.SUBMIT, findViewById(R.id.submitButton));
    }

    @Override
    public void buttonClickedListener() {
        submitButtonListener();
    }

    public void submitButtonListener() {
        inputButtons.get(Input.WhichButton.SUBMIT).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String password = inputViews.get(Input.WhichText.PASSWORD).getText().toString();
                String confirmPassword = inputViews.get(Input.WhichText.CONFIRM_PASSWORD).getText().toString();
                if (password.equals(confirmPassword)) {
                    if (isValidPassword(password)) {
                        if (password.equals(getOldPassword())) {
                            makeToast("Password too similar to previous password", getApplicationContext());
                        } else {
                            configureNewPassword(hashPassword(password));
                            // FIXME: valid
                            Intent intent = new Intent(ChangePassword.this, MainScreen.class);

                            System.out.println(getOldPassword());
                            System.out.println(getNewPassword());

                            intent.putExtra(USERNAME_KEY, getIntent().getStringExtra(USERNAME_KEY));
                            intent.putExtra(PASSWORD_KEY, hashPassword(password));
                            startActivity(intent);
                        }
                    } else {

                    }
                } else {
                    makeToast("Passwords must be the same", getApplicationContext());
                }
            }
        });
    }

    // FIXME: not fully implemented, abstract functions in CreateAccount
    public boolean isValidPassword(String password) {
        return hasAtLeastTwoNumbers(password) &&
                hasASpecialSymbol(password) &&
                !containsSpace(password);
    }
}