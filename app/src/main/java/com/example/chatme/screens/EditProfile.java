package com.example.chatme.screens;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.NULL_VALUE;
import static com.example.chatme.database.Constants.Users.EMAIL_KEY;
import static com.example.chatme.database.Constants.Users.ICON_KEY;
import static com.example.chatme.database.Constants.Users.PASSWORD_KEY;
import static com.example.chatme.database.Constants.Users.USERNAME_KEY;
import static com.example.chatme.database.Constants.Users.USER_COLLECTION_KEY;
import static com.example.chatme.manipulation.BitmapHandler.HALF_ROTATION_RIGHT;
import static com.example.chatme.manipulation.BitmapHandler.rotateBitmap;
import static com.example.chatme.security.Requirements.containsSpace;
import static com.example.chatme.security.Requirements.hasASpecialSymbol;
import static com.example.chatme.security.Requirements.hasAtLeastTwoNumbers;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import com.example.chatme.R;
import com.example.chatme.blueprints.BaseActivity;
import com.example.chatme.clickables.Input;
import com.example.chatme.security.Hashing;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditProfile extends BaseActivity {

    private static final int IMAGE_REQUEST_CODE = 1;
    private static final int READ_EXT_STORAGE_REQ_CODE = 2;

    ShapeableImageView selectImageButton;
    TextView selectImageText;
    byte[] selectedImageByteArray;
    String selectedImageByteArrayString;

    @Override
    public void initializeAssets() {
        initializeInputViews();
        initializeInputButtons();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        initializeAssets();
        buttonClickedListener();
        checkForPreviousIconInDatabase();
    }


    @Override
    public void initializeInputViews() {
        inputViews = new HashMap<>();
        inputViews.put(Input.WhichText.EMAIL, findViewById(R.id.emailText));
        inputViews.put(Input.WhichText.CONFIRM_EMAIL, findViewById(R.id.confirmEmailText));
        inputViews.put(Input.WhichText.PASSWORD, findViewById(R.id.passwordText));
        inputViews.put(Input.WhichText.CONFIRM_PASSWORD, findViewById(R.id.confirmPasswordText));
    }

    @Override
    public void initializeInputButtons() {
        inputButtons = new HashMap<>();
        selectImageButton = findViewById(R.id.selectImage);
        selectImageText = findViewById(R.id.selectImageText);
        selectedImageByteArray = null;


        inputButtons.put(Input.WhichButton.BACK_BUTTON, findViewById(R.id.backToPreviousButton));
        inputButtons.put(Input.WhichButton.SUBMIT, findViewById(R.id.submitButton));
    }

    @Override
    public void buttonClickedListener() {
        backButtonListener();
        selectImageButtonListener();
        submitButtonListener();
    }

    public void backButtonListener() {
        inputButtons.get(Input.WhichButton.BACK_BUTTON).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NOTE: May be changed later
                finish();
            }
        });
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
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

    private void checkForPreviousIconInDatabase() {
        // Load the database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference dbRef = database.collection(USER_COLLECTION_KEY);

        // Get material from the previous screen.
        Intent originalIntent = getIntent();

        // Grab the credentials from the previous screen.
        List<String> credentials = new ArrayList<>();
        credentials.add(originalIntent.getStringExtra(USERNAME_KEY));
        credentials.add(originalIntent.getStringExtra(PASSWORD_KEY));

        // Search for them within the database.
        dbRef.whereEqualTo(USERNAME_KEY, credentials.get(0))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();

                        // If there's a result.
                        if (!querySnapshot.isEmpty()) {
                            // Get the first document that pops up.
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String userId = document.getId();
                            initializeProfileIcon(document, userId);
                        }
                    }
                });
    }


    // Initializes the profile icon if the user has uploaded an icon,
    // if not, nothing will happen.
    private void initializeProfileIcon(DocumentSnapshot document, String userId) {
        // Get the serialized icon.
        Object iconObject = document.get(ICON_KEY);
        String iconSerialized = iconObject.toString();
        System.out.println(iconSerialized);

        // Decode it if qualifications are met.
        if (!iconSerialized.equals(NULL_VALUE)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                byte[] decodedByteArray = Base64.decode(iconSerialized, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                // Set the image bitmap.
                decodedBitmap = rotateBitmap(decodedBitmap, HALF_ROTATION_RIGHT);
                selectImageButton.setImageBitmap(decodedBitmap);
                selectImageText.setText("");
            }
        }
    }

    private void uploadIconToDatabase() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference dbRef = database.collection(USER_COLLECTION_KEY);

        // Check if the user is already found within the database.
        dbRef.whereEqualTo(EMAIL_KEY, inputViews.get(Input.WhichText.EMAIL).getText().toString())
                .whereEqualTo(PASSWORD_KEY, hashPassword(inputViews.get(Input.WhichText.PASSWORD).getText().toString()))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get the user Document ID
                            String userId = document.getId();

                            if (selectedImageByteArray != null) {
                                // If the icon could be loaded.
                                dbRef.document(userId).update(ICON_KEY, selectedImageByteArrayString)
                                        .addOnCompleteListener(success -> {
                                            makeToast("Icon uploaded", this);
                                        })
                                        .addOnFailureListener(e -> {
                                            makeToast("Changes failed", this);
                                        });
                            } else {
                                makeToast("No icon was uploaded", this);
                            }
                        }
                    } else {
                        makeToast("Invalid credentials", this);
                    }
                });
    }

    private void transferToMainScreen() {
        Intent originalIntent = getIntent();

        // Insert username and password if the username decides to go back.
        Intent nextIntent = new Intent(EditProfile.this, MainScreen.class);
        nextIntent.putExtra(USERNAME_KEY, originalIntent.getStringExtra(USERNAME_KEY));
        nextIntent.putExtra(PASSWORD_KEY, hashPassword(originalIntent.getStringExtra(PASSWORD_KEY)));
        startActivity(nextIntent);
    }

    private byte[] getSelectedImageBitmapString() {
        return selectedImageByteArray;
    }

    public void selectImageButtonListener() {
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (permissionNotAllowed()) {
                    requestPermission();
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, IMAGE_REQUEST_CODE);
                }
            }
        });
    }

    private boolean permissionNotAllowed() {
        return (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(EditProfile.this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE},
                READ_EXT_STORAGE_REQ_CODE);
    }

    public void submitButtonListener() {
        inputButtons.get(Input.WhichButton.SUBMIT).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadIconToDatabase();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // If an image has been selected
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && intent != null) {
            Uri selectedImageUri = intent.getData();
            loadProfileIcon(selectedImageUri);
        }
    }

    private void loadProfileIcon(Uri selectedImageUri) {

        try {

            Bitmap selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);


            // Set the icon for display purposes.
            selectImageButton.setImageDrawable(new BitmapDrawable(getResources(), selectedImageBitmap));
            TextView iconButtonTextView = findViewById(R.id.selectImageText);
            selectImageText.setText("");

            // Convert the bitmap into bytes
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            serializeIcon(selectedImageBitmap);

            iconButtonTextView.setText("");
        } catch (IOException e) {
            makeToast("Couldn't upload image", this);
        }
    }



    private void serializeIcon(Bitmap selectedImageBitmap) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOS);
        selectedImageByteArray = byteArrayOS.toByteArray();
        selectedImageByteArrayString = Base64.encodeToString(selectedImageByteArray, Base64.DEFAULT);
    }



}