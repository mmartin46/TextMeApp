package com.example.chatme.screens;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.example.chatme.R;
import com.example.chatme.adapters.UserAdapter;
import com.example.chatme.blueprints.BaseActivity;
import com.example.chatme.blueprints.User;
import com.example.chatme.clickables.Input;
import com.example.chatme.listeners.UserListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.Chat.RECEIVER_ICON;
import static com.example.chatme.database.Constants.Chat.RECEIVER_USERNAME;
import static com.example.chatme.database.Constants.Chat.SENDER_ICON;
import static com.example.chatme.database.Constants.Chat.SENDER_USERNAME;
import static com.example.chatme.database.Constants.NULL_VALUE;
import static com.example.chatme.database.Constants.Users.ICON_KEY;
import static com.example.chatme.database.Constants.Users.USERNAME_KEY;
import static com.example.chatme.database.Constants.Users.USER_COLLECTION_KEY;
import static com.example.chatme.manipulation.BitmapHandler.HALF_ROTATION_RIGHT;
import static com.example.chatme.manipulation.BitmapHandler.rotateBitmap;
import static com.example.chatme.manipulation.Modifiers.deserializeIcon;
import static com.example.chatme.manipulation.Modifiers.getDeserializedIcon;

import com.example.chatme.manipulation.Modifiers.*;


import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AddUsersScreen extends BaseActivity implements UserListener {

    private List<User> userList;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_users_screen);
        initializeAssets();
        buttonClickedListener();
    }

    private void initializeList() {
        userList = new ArrayList<>();
        initializeUsersFromDatabase();
    }

    private void initializeRecyclerView() {
        recyclerView = findViewById(R.id.userRecyclerView);

        userAdapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(userAdapter);
    }



    @Override
    public void initializeInputViews() {
        initializeList();
        initializeRecyclerView();
    }

    @Override
    public void initializeInputButtons() {
        inputButtons = new HashMap<>();
        inputButtons.put(Input.WhichButton.BACK_BUTTON, findViewById(R.id.backToPreviousButton));
        inputButtons.put(Input.WhichButton.SEARCH_BUTTON, findViewById(R.id.searchButton));
    }

    @Override
    public void buttonClickedListener() {
        backButtonListener();
        searchButtonListener();
    }

    private void backButtonListener() {
        inputButtons.get(Input.WhichButton.BACK_BUTTON).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void searchButtonListener() {
        inputButtons.get(Input.WhichButton.SEARCH_BUTTON).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String senderUsername = getIntent().getStringExtra(USERNAME_KEY);

                Intent intent = new Intent(AddUsersScreen.this, SearchScreen.class);
                intent.putExtra(SENDER_USERNAME, senderUsername);
                startActivity(intent);
            }
        });
    }

    private void initializeUsersFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbRef = db.collection(USER_COLLECTION_KEY);

        dbRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();

                QuerySnapshot queryDocumentSnapshots = task.getResult();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String username = document.getString(USERNAME_KEY);
                    Bitmap userIconDeserialized = getDeserializedIcon(document);

                    userList.add(new User(username, userIconDeserialized, ""));
                }

                // Notifies changes to the userAdapter.
                userAdapter.setData(userList);
            } else {
                makeToast("Unable to load users. Please try again later.", this);
            }
        });
    }



    @Override
    public void onClicked(User user) {
        String senderUsername = getIntent().getStringExtra(USERNAME_KEY);

        Intent intent = new Intent(AddUsersScreen.this, ChatScreen.class);
        intent.putExtra(SENDER_USERNAME, senderUsername);
        intent.putExtra(RECEIVER_USERNAME, user.getUsername());
        startActivity(intent);
    }
}