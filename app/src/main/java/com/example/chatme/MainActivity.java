package com.example.chatme;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.Users.PASSWORD_KEY;
import static com.example.chatme.database.Constants.Users.USERNAME_KEY;
import static com.example.chatme.database.Constants.Users.USER_COLLECTION_KEY;
import static com.example.chatme.security.Hashing.hashPassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatme.blueprints.BaseActivity;
import com.example.chatme.clickables.Input;
import com.example.chatme.screens.CreateAccount;
import com.example.chatme.screens.ForgotPassword;
import com.example.chatme.screens.MainScreen;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing the IDs.
        initializeAssets();
        buttonClickedListener();
    }

    @Override
    public void initializeInputViews() {
        inputViews = new HashMap<>();
        inputViews.put(Input.WhichText.USERNAME, findViewById(R.id.usernameText));
        inputViews.put(Input.WhichText.PASSWORD, findViewById(R.id.passwordText));
    }

    @Override
    public void initializeInputButtons() {
        inputButtons = new HashMap<>();
        inputButtons.put(Input.WhichButton.SUBMIT, findViewById(R.id.loginButton));
        inputButtons.put(Input.WhichButton.CREATE_ACCOUNT, findViewById(R.id.createAccountButton));
        inputButtons.put(Input.WhichButton.FORGOT_PASSWORD, findViewById(R.id.forgotPasswordButton));
    }


    // Checks if a button has been clicked.
    @Override
    public void buttonClickedListener() {
        submitButtonClicked();
        createAccountButtonClicked();
        forgotPasswordButtonClicked();
    }

    private void submitButtonClicked() {

        inputButtons.get(Input.WhichButton.SUBMIT).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValidUsername() && isValidPassword()) {
                    // FIXME: Create a database and check if the database contains the username
                    checkDatabaseForValidUsername();
                }
            }
        });
    }

    private void forgotPasswordButtonClicked() {
        inputButtons.get(Input.WhichButton.FORGOT_PASSWORD).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });
    }

    private boolean isValidUsername() {
        String username = inputViews.get(Input.WhichText.USERNAME).getText().toString();
        if (username.isEmpty()) {
            makeToast("Invalid Username", this);
            return false;
        }
        return true;
    }

    private boolean isValidPassword() {
        String password = inputViews.get(Input.WhichText.PASSWORD).getText().toString();
        if (password.isEmpty()) {
            makeToast("Invalid Password", this);
            return false;
        }
        return true;
    }



    private void createAccountButtonClicked() {

        inputButtons.get(Input.WhichButton.CREATE_ACCOUNT).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME: Redirect to a new screen
                Intent intent = new Intent(MainActivity.this, CreateAccount.class);
                startActivity(intent);
            }
        });
    }



    private void checkDatabaseForValidUsername() {

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference dbRef = database.collection(USER_COLLECTION_KEY);

        String hashedPassword = hashPassword(inputViews.get(Input.WhichText.PASSWORD).getText().toString());

        dbRef.whereEqualTo(USERNAME_KEY, inputViews.get(Input.WhichText.USERNAME).getText().toString())
                .whereEqualTo(PASSWORD_KEY, hashedPassword)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isComplete()) {

                        if (task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            Intent intent = new Intent(getApplicationContext(), MainScreen.class);
                            intent.putExtra(USERNAME_KEY, inputViews.get(Input.WhichText.USERNAME).getText().toString());
                            intent.putExtra(PASSWORD_KEY, hashedPassword);
                            startActivity(intent);
                        } else {
                            makeToast("No user was found", this);
                        }
                    } else {
                        makeToast("Invalid username or password", this);
                    }
                });
    }
}
