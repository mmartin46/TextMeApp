package com.example.chatme.screens;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.Chat.CONVERSATION_COLLECTION_KEY;
import static com.example.chatme.database.Constants.Chat.RECEIVER_USERNAME;
import static com.example.chatme.database.Constants.Chat.SENDER_ICON;
import static com.example.chatme.database.Constants.Chat.SENDER_MESSAGE;
import static com.example.chatme.database.Constants.Chat.SENDER_MESSAGE_TIME_SENT;
import static com.example.chatme.database.Constants.Chat.SENDER_USERNAME;
import static com.example.chatme.database.Constants.NULL_VALUE;
import static com.example.chatme.database.Constants.Users.ICON_KEY;
import static com.example.chatme.database.Constants.Users.PASSWORD_KEY;
import static com.example.chatme.database.Constants.Users.USERNAME_KEY;
import static com.example.chatme.database.Constants.Users.USER_COLLECTION_KEY;
import static com.example.chatme.manipulation.BitmapHandler.HALF_ROTATION_RIGHT;
import static com.example.chatme.manipulation.BitmapHandler.rotateBitmap;
import static com.example.chatme.manipulation.Modifiers.deserializeIcon;
import static com.example.chatme.security.Hashing.hashPassword;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import com.example.chatme.adapters.RecentMessageAdapter;
import com.example.chatme.blueprints.ChatMessage;
import com.example.chatme.blueprints.RecentMessage;
import com.example.chatme.listeners.RecentMessageListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;

import com.example.chatme.MainActivity;
import com.example.chatme.R;
import com.example.chatme.clickables.Input;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainScreen extends AppCompatActivity implements RecentMessageListener {


    private List<RecentMessage> recentMessageList;
    private RecyclerView recyclerView;
    private RecentMessageAdapter recentMessageAdapter;

    private TextView headerTextView;
    private Map<Input.WhichButton, View> inputButtons;
    Map<String, String> whichString;

    ShapeableImageView editProfileButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        initializeAssets();
        buttonListener();
    }


    private void connectToFirebase() {
        // Load the database.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference conversationRef = db.collection(CONVERSATION_COLLECTION_KEY);
        recentMessageList = new ArrayList<>();

        String username = whichString.get(USERNAME_KEY);
        Map<String, RecentMessage> recentMessageMap = new HashMap<>();

        conversationRef.whereEqualTo(SENDER_USERNAME, username)
                .addSnapshotListener((senderResult, senderError) -> {
                    handleConversationSnapshot(senderResult, senderError, recentMessageMap, username);
                });

        conversationRef.whereEqualTo(RECEIVER_USERNAME, username)
                .addSnapshotListener((receiverResult, receiverError) -> {
                    handleConversationSnapshot(receiverResult, receiverError, recentMessageMap, username);
                });
    }

    private void handleConversationSnapshot(QuerySnapshot result, FirebaseFirestoreException error, Map<String, RecentMessage> recentMessageMap, String username) {
        // If there is an error loading the messages.
        if (error != null) {
            makeToast("Failed to show messages", this);
            return;
        }

        for (QueryDocumentSnapshot document : result) {
            String otherUsername;

            if (document.getString(SENDER_USERNAME).equals(username)) {
                // If this is the receiver we will set the username to
                // the receiver.
                otherUsername = document.getString(RECEIVER_USERNAME);
            } else {
                // Else set it as the sender's username.
                otherUsername = document.getString(SENDER_USERNAME);
            }

            RecentMessage recentMessage = new RecentMessage();

            // Load icon in database
            findIconInUserDataBase(otherUsername, recentMessage);
            recentMessage.setRecentSenderUsername(otherUsername);
            recentMessage.setRecentUserText(document.getString(SENDER_MESSAGE));
            recentMessage.setRecentUserTextTimeSent(document.getDate(SENDER_MESSAGE_TIME_SENT));

            // Check if a recent message already exists from this user.
            if (recentMessageMap.containsKey(otherUsername)) {
                RecentMessage existingMessage = recentMessageMap.get(otherUsername);
                // Check timestamps to determine the most recent message
                if (recentMessage.getRecentUserTextTimeSent().after(existingMessage.getRecentUserTextTimeSent())) {
                    recentMessageMap.put(otherUsername, recentMessage);
                }
            } else {
                // Just insert the message.
                recentMessageMap.put(otherUsername, recentMessage);
            }
        }

        // Clear the list before adding more messages
        recentMessageList.clear();
        recentMessageList.addAll(recentMessageMap.values());
        // Sort the messages based on the time
        Collections.sort(recentMessageList, (message1, message2) ->
                message2.getRecentUserTextTimeSent().compareTo(message1.getRecentUserTextTimeSent()));
        // Notify the recycler view.
        setupRecyclerView(recentMessageList);
    }

    private void findIconInUserDataBase(String username, RecentMessage recentMessage) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbRef = db.collection(USER_COLLECTION_KEY);

        dbRef.whereEqualTo(USERNAME_KEY, username)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String serializedIcon = document.getString(ICON_KEY);


                            Bitmap deserializedIcon = deserializeIcon(serializedIcon);


                            recentMessage.setRecentUserIcon(deserializedIcon);
                        }



                        setupRecyclerView(recentMessageList);
                    } else {
                        makeToast("Failed to get icons", this);
                    }
                });
    }



    // Create a stream to check if there is any match in the recentMessageList
    // and the given recentMessage.
    boolean doesMessageExist(List<RecentMessage> recentMessageList, RecentMessage recentMessage) {
        return recentMessageList.stream()
                .anyMatch(msg -> msg.getRecentSenderUsername().equals(recentMessage.getRecentSenderUsername()) &&
                        msg.getRecentUserTextTimeSent().equals(recentMessage.getRecentUserTextTimeSent()) &&
                        msg.getRecentUserText().equals(recentMessage.getRecentUserText()));
    }

    private Bitmap getDefaultIconBitmap(Context context) {
        Bitmap defaultIcon = null;
        try {
            defaultIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_icon_frame_1);

        } catch (Exception e) {
            System.err.println("Problem loading default icon");
        }
        return defaultIcon;
    }

    // Sets up the recentMessageList with the recyclerView.
    private void setupRecyclerView(List<RecentMessage> recentMessageList) {
        // If the recyclerView isn't initialized, find the correct view by ID,
        // then set the adapter.
        if (recentMessageAdapter == null) {
            recentMessageAdapter = new RecentMessageAdapter(recentMessageList, this);
            recyclerView = findViewById(R.id.recentMessageRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(recentMessageAdapter);
        } else {
            // Notify the adapter if it is initialized for any changes.
            recentMessageAdapter.setData(recentMessageList);
        }
    }

    private void initializeAssets() {
        initializeProfileButton();
        initializeHeader();
        initializeButtons();
        connectToFirebase();
    }

    private void initializeButtons() {
        inputButtons = new HashMap<>();
        inputButtons.put(Input.WhichButton.BACK_BUTTON, findViewById(R.id.backToPreviousButton));
        inputButtons.put(Input.WhichButton.EDIT_PROFILE, findViewById(R.id.editProfileButton));
        inputButtons.put(Input.WhichButton.ADD_USER, findViewById(R.id.addUserButton));
        editProfileButton = findViewById(R.id.editProfileButton);
        editProfileButton.setImageResource(R.drawable.default_icon_frame_1);
    }

    private void initializeHeader() {
        headerTextView = findViewById(R.id.greetHeader);
        setHeaderTextViewText();
    }

    private void buttonListener() {
        backButtonListener();
        profileButtonListener();
        addUserButtonListener();
    }

    private void backButtonListener() {
        inputButtons.get(Input.WhichButton.BACK_BUTTON).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addUserButtonListener() {
        inputButtons.get(Input.WhichButton.ADD_USER).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainScreen.this, AddUsersScreen.class);
                // Filter the users so we don't have to see our own username.
                intent.putExtra(USERNAME_KEY, whichString.get(USERNAME_KEY));
                startActivity(intent);
            }
        });
    }

    private void initializeProfileButton() {
        Intent intent = getIntent();

        whichString = new HashMap<>();
        whichString.put(USERNAME_KEY, intent.getStringExtra(USERNAME_KEY));
        whichString.put(PASSWORD_KEY, intent.getStringExtra(PASSWORD_KEY));



        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference dbRef = database.collection(USER_COLLECTION_KEY);

        dbRef.whereEqualTo(USERNAME_KEY, whichString.get(USERNAME_KEY))
                .whereEqualTo(PASSWORD_KEY, whichString.get(PASSWORD_KEY))
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

        // Decode it if qualifications are met.
        if (!iconSerialized.equals(NULL_VALUE)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                byte[] decodedByteArray = Base64.decode(iconSerialized, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                // Set the image bitmap.
                decodedBitmap = rotateBitmap(decodedBitmap, HALF_ROTATION_RIGHT);
                editProfileButton.setImageBitmap(decodedBitmap);
            }
        } else {
            editProfileButton.setImageResource(R.drawable.default_icon_frame_1);
        }
    }

    private void profileButtonListener() {
        inputButtons.get(Input.WhichButton.EDIT_PROFILE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainScreen.this, EditProfile.class);
                // Send the username and password to the next intent.
                intent.putExtra(USERNAME_KEY, whichString.get(USERNAME_KEY));
                intent.putExtra(PASSWORD_KEY, whichString.get(PASSWORD_KEY));
                startActivity(intent);
            }
        });
    }

    private void setHeaderTextViewText() {
        Intent intent = getIntent();
        whichString.get(USERNAME_KEY);
        headerTextView.setText("Hello " + whichString.get(USERNAME_KEY) + "!");
    }

    @Override
    public void onClicked(RecentMessage recentMessage) {

        Intent intent = new Intent(MainScreen.this, ChatScreen.class);
        intent.putExtra(RECEIVER_USERNAME, recentMessage.getRecentSenderUsername());
        intent.putExtra(SENDER_USERNAME, whichString.get(USERNAME_KEY));
        startActivity(intent);
    }
}