package com.cpen321.tunematch;

import android.view.View;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter  {

    private List<Message> messages;
    private SessionUser currentUser;

    public final static int MESSAGE_TYPE_RECEIVED = 0;
    public final static int MESSAGE_TYPE_SENT = 0;

    public MessageAdapter(List<Message> messages, SessionUser currentUser) {
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
        } else {
            RecievedMessageViewHolder view = (RecievedMessageViewHolder) holder;
            view.getMessageText().setText(message.getMessageText());
            // Only render image if first
            if (isFirstMessage(position)) {
                new DownloadImageTask(view.getProfileImage())
                        .execute(message.getSenderProfileImageUrl());
            }
        }

        // Set padding to 0 if next to message from same user
        ConstraintLayout container = ((SentMessageViewHolder) holder).getContainer();
        container.setPadding(
                container.getPaddingLeft(),
                isLastMessage(position) ? container.getPaddingTop() : 0,
                isFirstMessage(position) ? container.getPaddingRight() : 0,
                container.getPaddingBottom());
    }

    // ChatGPT Usage: No
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ChatGPT Usage: No
    public boolean isFirstMessage(int position) {
        return (position - 1 < 0 || messages.get(position - 1).getSenderUserId() != currentUser.getUserId());
    }

    // ChatGPT Usage: No
    public boolean isLastMessage(int position) {
        return (position + 1 >= messages.size() || messages.get(position + 1).getSenderUserId() != currentUser.getUserId());
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
        private final ConstraintLayout container;
        public TextView getMessageText() {
            return messageText;
        }
        public ConstraintLayout getContainer() {
            return container;
        }
        public SentMessageViewHolder(@NonNull View view) {
            super(view);
            container = (ConstraintLayout) view.findViewById(R.id.container);
            messageText = (TextView) view.findViewById(R.id.textMessage);
        }
    }

    // ChatGPT Usage: No
    static class RecievedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final ImageView profileImage;
        private final ConstraintLayout container;
        public TextView getMessageText() {
            return messageText;
        }
        public ImageView getProfileImage() {
            return profileImage;
        }
        public ConstraintLayout getContainer() {
            return container;
        }
        public RecievedMessageViewHolder(@NonNull View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.textMessage);
            profileImage = (ImageView) view.findViewById(R.id.profileImage);
            container = (ConstraintLayout) view.findViewById(R.id.container);
        }
    }
}
