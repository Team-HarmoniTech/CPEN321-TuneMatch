package com.cpen321.tunematch;

import android.view.View;

import androidx.annotation.NonNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter  {

    private List<Message> messages;
    private SessionUser currentUser;
    private Context context;

    public final static int MESSAGE_TYPE_RECEIVED = 0;
    public final static int MESSAGE_TYPE_SENT = 0;

    public MessageAdapter(Context context, List<Message> messages, SessionUser currentUser) {
        this.context = context;
        this.messages = messages;
        this.currentUser = currentUser;
    }

    // ChatGPT Usage: No
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_TYPE_SENT) {
            return new SentMessageViewHolder(
                    LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_sent_message, parent, false)
            );
        } else  {
            return new RecievedMessageViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_recieved_message, parent, false)
            );
        }
    }

    // ChatGPT Usage: No
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (getMessageType(position) == MESSAGE_TYPE_SENT) {
            SentMessageViewHolder view = (SentMessageViewHolder) holder;
            view.getMessageText().setText(message.getMessageText());
            view.getDateTimeText().setText(message.getTimestamp().toString());
        } else {
            RecievedMessageViewHolder view = (RecievedMessageViewHolder) holder;
            view.getMessageText().setText(message.getMessageText());
            view.getDateTimeText().setText(message.getTimestamp().toString());
            new DownloadImageTask(view.getProfileImage())
                    .execute(message.getSenderProfileImageUrl());
        }
    }

    // ChatGPT Usage: No
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ChatGPT Usage: No
    public int getMessageType(int position) {
        if (messages.get(position).getSenderUserId().equals(currentUser.getUserId())) {
            return MESSAGE_TYPE_SENT;
        } else {
            return MESSAGE_TYPE_RECEIVED;
        }
    }

    // ChatGPT Usage: No
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView dateTimeText;
        public TextView getMessageText() {
            return messageText;
        }
        public TextView getDateTimeText() {
            return dateTimeText;
        }
        public SentMessageViewHolder(@NonNull View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.textMessage);
            dateTimeText = (TextView) view.findViewById(R.id.textDateTime);
        }
    }

    // ChatGPT Usage: No
    static class RecievedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView dateTimeText;
        private final ImageView profileImage;
        public TextView getMessageText() {
            return messageText;
        }
        public TextView getDateTimeText() {
            return dateTimeText;
        }
        public ImageView getProfileImage() {
            return profileImage;
        }
        public RecievedMessageViewHolder(@NonNull View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.textMessage);
            dateTimeText = (TextView) view.findViewById(R.id.textDateTime);
            profileImage = (ImageView) view.findViewById(R.id.profileImage);
        }
    }
}
