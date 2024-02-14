package com.example.chatme.screens;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.Chat.CONVERSATION_COLLECTION_KEY;
import static com.example.chatme.database.Constants.Chat.RECEIVER_USERNAME;
import static com.example.chatme.database.Constants.Chat.SENDER_MESSAGE;
import static com.example.chatme.database.Constants.Chat.SENDER_MESSAGE_TIME_SENT;
import static com.example.chatme.database.Constants.Chat.SENDER_USERNAME;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.chatme.R;
import com.example.chatme.adapters.ChatAdapter;
import com.example.chatme.blueprints.BaseActivity;
import com.example.chatme.blueprints.ChatMessage;
import com.example.chatme.clickables.Input;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatScreen extends BaseActivity {

    private String receiverUsername;
    private String senderMessage;

    private RecyclerView recyclerView;

    private List<ChatMessage> chatMessageList;


    ChatAdapter chatMessageAdapter;
    RecyclerView chatRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        initializeAssets();
        buttonClickedListener();
    }

    @Override
    public void initializeAssets() {
        initializeChatMessageList();

        initializeReceiverUsername();
        initializeConnectionToDatabase();

        initializeInputViews();
        initializeInputButtons();
    }

    private void initializeChatMessageList() {
        chatMessageList = new ArrayList<>();
    }

    @Override
    public void initializeInputViews() {
        inputViews = new HashMap<>();
        inputViews.put(Input.WhichText.PROFILE_HEADER, findViewById(R.id.profileHeader));
        initializeProfileHeader();
    }


    private String getLastIntentString(String string) {
        Intent intent = getIntent();
        return intent.getStringExtra(string);
    }

    private void initializeConnectionToDatabase() {
        // Connect to the database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference dbRef = database.collection(CONVERSATION_COLLECTION_KEY);

        // Find the sender and receiver's usernames.
        String senderUsername = getLastIntentString(SENDER_USERNAME);
        String receiverUsername = getLastIntentString(RECEIVER_USERNAME);

        System.out.println("Sender: " + senderUsername);
        System.out.println("Receiver: " + receiverUsername);


        // Find the sender and receiver messages.
        Query senderToReceiverQuery = dbRef.whereEqualTo(SENDER_USERNAME, senderUsername)
                                            .whereEqualTo(RECEIVER_USERNAME, receiverUsername);

        Query receiverToSenderQuery = dbRef.whereEqualTo(SENDER_USERNAME, receiverUsername)
                        .whereEqualTo(RECEIVER_USERNAME, senderUsername);

        chatMessageList = new ArrayList<>();

        // Load the sender's messages.
        senderToReceiverQuery.addSnapshotListener((result, error) -> {
            if (error != null) {
                makeToast("Failed to show messages", this);
                return;
            }

            for (QueryDocumentSnapshot document : result) {

                // Grab the each matching message
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setSenderUsername(document.getString(SENDER_USERNAME));
                chatMessage.setReceiverUsername(document.getString(RECEIVER_USERNAME));
                chatMessage.setSenderMessage(document.getString(SENDER_MESSAGE));
                chatMessage.setSenderMessageTimeSent(document.getDate(SENDER_MESSAGE_TIME_SENT));


                // Add them to the list.
                if (!doesMessageExist(chatMessageList, chatMessage)) {
                    chatMessageList.add(chatMessage);
                }
            }
            // Sort and display the messages on the screen.
            sortChatMessages(chatMessageList);
            setupRecyclerView(chatMessageList, receiverUsername);
        });

        // Load the receiver's messages.
        receiverToSenderQuery.addSnapshotListener((result, error) -> {
            if (error != null) {
                makeToast("Failed to show messages", this);
                return;
            }

            for (QueryDocumentSnapshot document : result) {
                // Grab the each matching message
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setSenderUsername(document.getString(SENDER_USERNAME));
                chatMessage.setReceiverUsername(document.getString(RECEIVER_USERNAME));
                chatMessage.setSenderMessage(document.getString(SENDER_MESSAGE));
                chatMessage.setSenderMessageTimeSent(document.getDate(SENDER_MESSAGE_TIME_SENT));

                // Add them to the list.
                if (!doesMessageExist(chatMessageList, chatMessage)) {
                    chatMessageList.add(chatMessage);
                }
            }
            // Sort and display the messages on the screen.
            sortChatMessages(chatMessageList);
            setupRecyclerView(chatMessageList, receiverUsername);
        });
    }

    // Checks for any matching message
    boolean doesMessageExist(List<ChatMessage> chatMessageList, ChatMessage chatMessage) {
        return chatMessageList.stream()
                .anyMatch(msg -> msg.getSenderMessage().equals(chatMessage.getSenderMessage()) &&
                                 msg.getReceiverUsername().equals(chatMessage.getReceiverUsername()) &&
                                 msg.getSenderMessageTimeSent().equals(chatMessage.getSenderMessageTimeSent()) &&
                                 msg.getSenderUsername().equals(chatMessage.getSenderUsername()));
    }

    private void setupRecyclerView(List<ChatMessage> chatMessageList, String receiverUsername) {
        if (chatMessageAdapter == null) {
            chatMessageAdapter = new ChatAdapter(chatMessageList, receiverUsername);
            chatRecyclerView = findViewById(R.id.chatRecyclerView);
            chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            chatRecyclerView.setAdapter(chatMessageAdapter);
            // Always sets the position to the most recent message.
            chatRecyclerView.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
        } else {
            // Update if already initialized.
            chatRecyclerView.scrollToPosition(chatMessageAdapter.getItemCount() - 1);

            // Always sets the position to the most recent message.
            chatMessageAdapter.notifyDataSetChanged();
        }
    }

    // Sorts the message list based on the time.
    private void sortChatMessages(List<ChatMessage> chatMessageList) {
        Collections.sort(chatMessageList, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage chatMessage1, ChatMessage chatMessage2) {
                return chatMessage1.getSenderMessageTimeSent().compareTo(chatMessage2.getSenderMessageTimeSent());
            }
        });

    }

    private void initializeReceiverUsername() {
        Intent intent = getIntent();
        receiverUsername = intent.getStringExtra(RECEIVER_USERNAME);
    }

    private void initializeProfileHeader() {
        Intent intent = getIntent();
        inputViews.get(Input.WhichText.PROFILE_HEADER).setText(intent.getStringExtra(RECEIVER_USERNAME));
    }

    @Override
    public void initializeInputButtons() {
        inputButtons = new HashMap<>();
        inputButtons.put(Input.WhichButton.BACK_BUTTON, findViewById(R.id.backToPreviousButton));
        inputButtons.put(Input.WhichButton.SEND_MESSAGE_BUTTON, findViewById(R.id.sendMessageButton));
    }

    @Override
    public void buttonClickedListener() {

        previousButtonListener();
        sendMessageButtonListener();
    }

    private void sendMessageButtonListener() {
        inputButtons.get(Input.WhichButton.SEND_MESSAGE_BUTTON).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView senderMessageTextView = findViewById(R.id.textMessage);
                senderMessage = senderMessageTextView.getText().toString();

                // Make sure the user, is sending a non-empty message.
                if (!senderMessage.isEmpty()) {

                    senderMessageTextView.setText("");
                    handleOnSendMessage();
                } else {
                    makeToast("Please enter a message.", getApplicationContext());
                }
            }
        });
    }

    private void handleOnSendMessage() {
        // Connect to the database.
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference dbRef = database.collection(CONVERSATION_COLLECTION_KEY);

        // Store the message in a HashMap.
        HashMap<String, Object> chatData = new HashMap<>();
        chatData.put(SENDER_USERNAME, getLastIntentString(SENDER_USERNAME));
        chatData.put(RECEIVER_USERNAME, getLastIntentString(RECEIVER_USERNAME));
        chatData.put(SENDER_MESSAGE, senderMessage);
        chatData.put(SENDER_MESSAGE_TIME_SENT, new Date());

        // Insert the HashMap within the database.
        database.collection(CONVERSATION_COLLECTION_KEY)
                .add(chatData)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Message sent");
                })
                .addOnFailureListener(exception -> {
                    makeToast("Message unable to send", this);
                });

        // Notify the adapter that there were changes made.
        chatMessageAdapter.notifyDataSetChanged();
    }





    public void previousButtonListener() {
        inputButtons.get(Input.WhichButton.BACK_BUTTON).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to the previous page.
                finish();
            }
        });
    }

}