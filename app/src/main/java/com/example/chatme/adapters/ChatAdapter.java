package com.example.chatme.adapters;

import static com.example.chatme.clickables.Input.makeToast;
import static com.example.chatme.database.Constants.Chat.SENDER_USERNAME;
import static com.example.chatme.database.Constants.NULL_VALUE;
import static com.example.chatme.database.Constants.Users.ICON_KEY;
import static com.example.chatme.database.Constants.Users.USERNAME_KEY;
import static com.example.chatme.database.Constants.Users.USER_COLLECTION_KEY;
import static com.example.chatme.manipulation.BitmapHandler.HALF_ROTATION_RIGHT;
import static com.example.chatme.manipulation.BitmapHandler.rotateBitmap;
import static com.example.chatme.manipulation.Modifiers.deserializeIcon;
import static com.example.chatme.manipulation.Modifiers.formatTime;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatme.R;
import com.example.chatme.blueprints.ChatMessage;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private final List<ChatMessage> chatMessageList;
    private final String receiverUsername;

    private final int RECEIVER_VIEW_TYPE = 1;
    private final int SENDER_VIEW_TYPE = 2;


    public ChatAdapter(List<ChatMessage> chatMessageList, String receiverUsername) {
        this.chatMessageList = chatMessageList;
        this.receiverUsername = receiverUsername;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        // Create a sent or received message depending on the viewType.
        if (viewType == RECEIVER_VIEW_TYPE) {

            view = inflater.inflate(R.layout.received_message, parent, false);
            return new ReceivedMessageViewHolder(view);

        } else if (viewType == SENDER_VIEW_TYPE) {

            view = inflater.inflate(R.layout.sent_message, parent, false);
            return new SentMessageViewHolder(view);
        }
        throw new IllegalArgumentException("Invalid view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessageList.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bindMessage(chatMessage);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bindMessage(chatMessage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // If the current message is from the receiver, set it to a receiver view type.
        // If it's not, it should be a sender view type.
        if (chatMessageList.get(position).getReceiverUsername().equals(receiverUsername)) {
            return RECEIVER_VIEW_TYPE;
        } else {
            return SENDER_VIEW_TYPE;
        }
    }

    // Sent Message Item for the adapter to enumerate.
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private ShapeableImageView senderIconView;
        private TextView senderTextView;
        private TextView senderTimeView;
        private ShapeableImageView userIcon;


        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderIconView = itemView.findViewById(R.id.senderTextUserIcon);
            senderTextView = itemView.findViewById(R.id.senderSentText);
            senderTimeView = itemView.findViewById(R.id.senderTimeSent);
            userIcon = itemView.findViewById(R.id.senderTextUserIcon);
        }



        public void bindMessage(ChatMessage chatMessage) {
            senderTextView.setText(chatMessage.getSenderMessage());
            senderTimeView.setText(formatTime(chatMessage.getSenderMessageTimeSent()));
            findIconFromDatabase(chatMessage);
        }

        private void findIconFromDatabase(ChatMessage chatMessage) {
            // Load the database
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference dbRef = db.collection(USER_COLLECTION_KEY);

            // If it can be located, load it. If not,
            // use the default icon.
            dbRef.whereEqualTo(USERNAME_KEY, chatMessage.getSenderUsername())
                    .get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String serializedIcon = document.getString(ICON_KEY);
                                if (serializedIcon.equals("null")) {
                                    senderIconView.setImageResource(R.drawable.default_icon_frame_1);
                                } else {
                                    senderIconView.setImageBitmap(deserializeIcon(serializedIcon));
                                }
                            }
                        }
                    });

        }

    }


    // Received Message Item for the adapter to enumerate.
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView receivedTextView;
        private TextView receivedTimeView;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedTextView = itemView.findViewById(R.id.receiverSentText);
            receivedTimeView = itemView.findViewById(R.id.receiverTimeSent);
        }

        public void bindMessage(ChatMessage chatMessage) {
            receivedTextView.setText(chatMessage.getSenderMessage());
            receivedTimeView.setText(formatTime(chatMessage.getSenderMessageTimeSent()));
        }
    }
}
