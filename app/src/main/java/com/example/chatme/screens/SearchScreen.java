package com.example.chatme.screens;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.Chat.RECEIVER_USERNAME;
import static com.example.chatme.database.Constants.Chat.SENDER_USERNAME;
import static com.example.chatme.database.Constants.Users.USERNAME_KEY;
import static com.example.chatme.database.Constants.Users.USER_COLLECTION_KEY;
import static com.example.chatme.manipulation.Modifiers.getDeserializedIcon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.example.chatme.R;
import com.example.chatme.adapters.UserAdapter;
import com.example.chatme.blueprints.BaseActivity;
import com.example.chatme.blueprints.User;
import com.example.chatme.clickables.Input;
import com.example.chatme.listeners.UserListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchScreen extends BaseActivity implements UserListener {
    private List<User> userList;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;

    private EditText userSearchText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);
        initializeAssets();
        initializeUsersFromDatabase();
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
    public void initializeInputViews() {
        initializeList();
        initializeRecyclerView();
        userSearchText = findViewById(R.id.userSearchText);
    }



    @Override
    public void initializeInputButtons() {
        inputButtons = new HashMap<>();
        inputButtons.put(Input.WhichButton.BACK_BUTTON, findViewById(R.id.backToPreviousButton));
    }

    @Override
    public void buttonClickedListener() {
        backButtonListener();
        searchInputListener();
    }

    private void searchInputListener() {

        userSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentText = s.toString().toLowerCase();

                // Filter the users based on the current text.
                List<User> filteredUsers = new ArrayList<>();

                for (User user : userList) {
                    if (user.getUsername().toLowerCase().contains(currentText)) {
                        filteredUsers.add(user);
                    }
                }
                // Update the adapter.
                userAdapter.setData(filteredUsers);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void backButtonListener() {
        inputButtons.get(Input.WhichButton.BACK_BUTTON).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClicked(User user) {
        Intent previousIntent = getIntent();
        String senderUsername = previousIntent.getStringExtra(SENDER_USERNAME);

        Intent intent = new Intent(SearchScreen.this, ChatScreen.class);
        intent.putExtra(SENDER_USERNAME, senderUsername);
        intent.putExtra(RECEIVER_USERNAME, user.getUsername());
        startActivity(intent);
    }
}