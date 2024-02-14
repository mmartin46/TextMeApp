package com.example.chatme.adapters;

import static com.example.chatme.manipulation.Modifiers.formatTime;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatme.R;
import com.example.chatme.blueprints.RecentMessage;
import com.example.chatme.listeners.RecentMessageListener;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class RecentMessageAdapter extends RecyclerView.Adapter<RecentMessageAdapter.RecentMessageViewHolder> {

    List<RecentMessage> recentMessageList;
    private final RecentMessageListener recentMessageListener;

    public RecentMessageAdapter(List<RecentMessage> recentMessageList, RecentMessageListener recentMessageListener) {
        this.recentMessageList = recentMessageList;
        this.recentMessageListener = recentMessageListener;
    }

    @NonNull
    @Override
    public RecentMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_message, parent, false);
        return new RecentMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentMessageViewHolder holder, int position) {
        RecentMessage recentMessage = recentMessageList.get(position);
        holder.bind(recentMessage);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recentMessageListener.onClicked(recentMessage);
            }
        });
    }



    public void setData(List<RecentMessage> recentMessageList) {
        this.recentMessageList = recentMessageList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recentMessageList.size();
    }

    // Item for the view holder to enumerate.
    static class RecentMessageViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView recentMessageTextUserIcon;
        TextView recentMessageTextView;
        TextView recentMessageTextTimeSent;

        TextView recentMessageUsernameTextView;

        public RecentMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            recentMessageTextView = itemView.findViewById(R.id.recentMessageTextView);
            recentMessageTextTimeSent = itemView.findViewById(R.id.recentMessageTextTimeSent);
            recentMessageTextUserIcon = itemView.findViewById(R.id.recentMessageTextUserIcon);
            recentMessageUsernameTextView = itemView.findViewById(R.id.recentMessageUsername);
        }


        public void bind(RecentMessage recentMessage) {
            recentMessageTextView.setText(recentMessage.getRecentUserText());
            recentMessageTextTimeSent.setText(formatTime(recentMessage.getRecentUserTextTimeSent()));
            recentMessageUsernameTextView.setText(recentMessage.getRecentSenderUsername());
            if (recentMessage.getRecentUserIcon() != null) {
                recentMessageTextUserIcon.setImageBitmap(recentMessage.getRecentUserIcon());
            }
        }

    }
}
