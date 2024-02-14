package com.example.chatme.adapters;

import android.graphics.drawable.shapes.Shape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatme.R;
import com.example.chatme.blueprints.User;
import com.example.chatme.listeners.UserListener;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private final UserListener userListener;
    // Initializes the data.
    public UserAdapter(List<User> userList, UserListener userListener) {

        this.userList = userList;
        this.userListener = userListener;
    }

    // Creates the instance of the viewholder to hold references to the views
    // for the layout.
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    // Sets the data to the views
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               userListener.onClicked(user);
           }
        });
    }


    @Override
    public int getItemCount() {

        return userList.size();
    }


    public void setData(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUsername;
        ShapeableImageView userIcon;
        TextView textViewRecentMessage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewUsername = itemView.findViewById(R.id.usernameForMessage);
            textViewRecentMessage = itemView.findViewById(R.id.recentMessage);
            userIcon = itemView.findViewById(R.id.recentConvoIcon);
        }

        public void bind(User user) {
            textViewUsername.setText(user.getUsername());
            if (user.getIconBitmap() != null) {
                userIcon.setImageBitmap(user.getIconBitmap());
            } else {
                userIcon.setImageResource(R.drawable.default_icon_frame_1);
            }
            textViewRecentMessage.setText(user.getRecentMessage());
        }
    }

}
