package com.example.chatme.screens;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.NULL_VALUE;
import static com.example.chatme.database.Constants.Users.EMAIL_KEY;
import static com.example.chatme.database.Constants.Users.ICON_KEY;
import static com.example.chatme.database.Constants.Users.PASSWORD_KEY;
import static com.example.chatme.database.Constants.Users.USERNAME_KEY;
import static com.example.chatme.database.Constants.Users.USER_COLLECTION_KEY;
import static com.example.chatme.security.Hashing.hashPassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatme.R;
import com.example.chatme.blueprints.BaseActivity;
import com.example.chatme.clickables.Input;
import com.example.chatme.security.Requirements;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class CreateAccount extends BaseActivity {

    private boolean allowToMainScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        allowToMainScreen = true;
        initializeAssets();

        buttonClickedListener();
    }

    @Override
    public void initializeInputViews() {
        inputViews = new HashMap<>();
        inputViews.put(Input.WhichText.EMAIL, findViewById(R.id.emailText));
        inputViews.put(Input.WhichText.USERNAME, findViewById(R.id.usernameText));
        inputViews.put(Input.WhichText.PASSWORD, findViewById(R.id.passwordText));
        inputViews.put(Input.WhichText.CONFIRM_PASSWORD, findViewById(R.id.confirmPasswordText));
    }

    @Override
    public void initializeInputButtons() {
        inputButtons = new HashMap<>();
        inputButtons.put(Input.WhichButton.BACK_BUTTON, findViewById(R.id.backToLoginButton));
        inputButtons.put(Input.WhichButton.CONFIRM_EMAIL, findViewById(R.id.confirmEmailButton));
    }


    // Checks if a button has been clicked.
    @Override
    public void buttonClickedListener() {
        confirmEmailButtonClicked();
        backButtonClicked();
    }

    private void confirmEmailButtonClicked() {

        inputButtons.get(Input.WhichButton.CONFIRM_EMAIL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidCredentials()) {
                    // Adds credentials within the firebase
                    doesUserWithUsernameExist().thenAccept(userExists -> {
                        if (userExists) {
                            makeToast("The username has already been taken", getApplicationContext());
                        } else {
                            transferToMainScreen();
                        }
                    }).exceptionally(e -> {
                        makeToast("Please try again later", CreateAccount.this);
                        return null;
                    });

                }
            }
        });

    }

    private void transferToMainScreen() {
        addCredentialsToDatabase();

        // Adds the user's credentials to allow for profile modification.
        Intent intent = new Intent(CreateAccount.this, MainScreen.class);
        addUserCredentialsToIntent(intent);

        startActivity(intent);
    }

    private void addUserCredentialsToIntent(Intent intent) {
        intent.putExtra("username", inputViews.get(Input.WhichText.USERNAME).getText().toString());
        intent.putExtra("password", inputViews.get(Input.WhichText.PASSWORD).getText().toString());
        intent.putExtra("email", inputViews.get(Input.WhichText.EMAIL).getText().toString());
    }

    // FIXME: Abstract this
    private void backButtonClicked() {
        inputButtons.get(Input.WhichButton.BACK_BUTTON).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



    private boolean isValidEmail() {
        String email = inputViews.get(Input.WhichText.EMAIL).getText().toString();
        if (email.isEmpty() || !email.contains("@")) {
            makeToast("Invalid email", this);
            return false;
        }
        return true;
    }
    private boolean isValidUsername() {
        String username = inputViews.get(Input.WhichText.USERNAME).getText().toString();
        if (username.length() < Requirements.USERNAME_MIN_LENGTH) {
            makeToast("Please use at least 8 characters for a username", this);
            return false;
        } else if (areTooSimilar(Input.WhichText.EMAIL, Input.WhichText.USERNAME)) {
            makeToast("Username and email are too similar", this);
            return false;
        } else if (Requirements.containsSpace(username)) {
            makeToast("Username cannot contain spaces", this);
            return false;
        } else if (Requirements.hasGenericUsername(username)) {
            makeToast("Username is too generic", this);
            return false;
        }

        return true;
    }
    private boolean isValidPassword() {
        String password = inputViews.get(Input.WhichText.PASSWORD).getText().toString();
        if (password.length() < Requirements.PASSWORD_MIN_LENGTH) {
            makeToast("Please use at least 10 characters for a password", this);
            return false;
        } else if (!Requirements.hasAtLeastTwoNumbers(password)) {
            makeToast("Please use at least 2 numbers for a password", this);
            return false;
        } else if (!Requirements.hasASpecialSymbol(password)) {
            makeToast("Please use at least one symbol that's not a number or digit.", this);
            return false;
        } else if (!Requirements.hasUppercaseLetter(password)) {
            makeToast("Please use at least one uppercase letter for a password", this);
            return false;
        } else if (areTooSimilar(Input.WhichText.USERNAME, Input.WhichText.PASSWORD)) {
            makeToast("Username and password are too similar", this);
            return false;
        } else {
            return true;
        }
    }
    private boolean matchingPasswords() {
        String password = inputViews.get(Input.WhichText.PASSWORD).getText().toString();
        String confirmPassword = inputViews.get(Input.WhichText.CONFIRM_PASSWORD).getText().toString();
        if (!password.equals(confirmPassword)) {
            makeToast("Passwords do not match", this);
            return false;
        }
        return true;
    }

    private boolean areTooSimilar(Input.WhichText textType1, Input.WhichText textType2) {
        if (textType1 == Input.WhichText.EMAIL && textType2 == Input.WhichText.USERNAME) {

            String email = inputViews.get(textType1).getText().toString();
            String username = inputViews.get(textType2).getText().toString();
            if (email.contains("@")) {
                int atIndex = email.indexOf("@");
                if (username.equalsIgnoreCase(email.substring(0, atIndex))) {
                    return true;
                } else if (email.contains(username)) {
                    return true;
                }
            }
        } else if (textType1 == Input.WhichText.USERNAME && textType2 == Input.WhichText.PASSWORD) {
            String username = inputViews.get(textType1).getText().toString();
            String password = inputViews.get(textType2).getText().toString();
            if (username.contains(password)) {
                return true;
            } else if (username.equalsIgnoreCase(password)) {
                return true;
            }
        }
        return false;
    }



    private boolean isValidCredentials() {
        return isValidEmail() && isValidUsername() && isValidPassword() && matchingPasswords() && allowToMainScreen;
    }


    private CompletableFuture<Boolean> doesUserWithUsernameExist() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference dbRef = database.collection(USER_COLLECTION_KEY);

        dbRef.whereEqualTo(USERNAME_KEY, inputViews.get(Input.WhichText.USERNAME).getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();

                        // If a user exists notify the user.
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            future.complete(true);
                        } else {
                            future.complete(false);
                        }
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }


    private void addCredentialsToDatabase() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> userData = new HashMap<>();
        userData.put(EMAIL_KEY, inputViews.get(Input.WhichText.EMAIL).getText().toString());
        userData.put(USERNAME_KEY, inputViews.get(Input.WhichText.USERNAME).getText().toString());


        String password = inputViews.get(Input.WhichText.PASSWORD).getText().toString();
        // FIXME: hash this password
        String hashedPassword = hashPassword(password);
        userData.put(PASSWORD_KEY, hashedPassword);
        userData.put(ICON_KEY, NULL_VALUE);

        database.collection(USER_COLLECTION_KEY)
                .add(userData)
                .addOnSuccessListener(documentReference -> {
                    makeToast("Account Created", this);
                })
                .addOnFailureListener(exception -> {
                    makeToast("Please try again later...", this);
                });
    }

}