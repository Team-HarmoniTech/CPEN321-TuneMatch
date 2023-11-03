package com.cpen321.tunematch;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MessageAdapter extends RecyclerView.Adapter  {

    private List<Message> messages;
    private User currentUser;
    private LayoutInflater inflater;
    private Context context;

    private BackendClient backend;

    public final static int MESSAGE_TYPE_RECEIVED = 1;
    public final static int MESSAGE_TYPE_SENT = 0;

    public MessageAdapter(List<Message> messages, User currentUser, @NonNull LayoutInflater inflater, @NonNull Context context, @NonNull BackendClient backend) {
        this.messages = messages;
        this.currentUser = currentUser;
        this.inflater = inflater;
        this.context = context;
        this.backend = backend;
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
            OnLongClickReportListener listener = new OnLongClickReportListener(position);
            view.getMessageText().setOnLongClickListener(listener);
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
                2,
                container.getPaddingRight(),
                isFirstMessage(position) ? container.getPaddingBottom() : 2);
    }

    // ChatGPT Usage: No
    public void updateMessages(List<Message> messages) {
        this.messages = messages;
        this.notifyDataSetChanged();
    }

    // ChatGPT Usage: No
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ChatGPT Usage: No
    public boolean isFirstMessage(int position) {
        return (position - 1 >= 0 && messages.get(position - 1).getSenderUserId() != currentUser.getUserId());
    }

    // ChatGPT Usage: No
    public boolean isLastMessage(int position) {
        return (position + 1 < messages.size() && messages.get(position + 1).getSenderUserId() != currentUser.getUserId());
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

    public class OnLongClickReportListener implements View.OnLongClickListener {
        private int position;

        public OnLongClickReportListener(int position) {
            super();
            this.position = position;
        }

        @Override
        public boolean onLongClick(View view) {
            // Create the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = inflater.inflate(R.layout.report_user_dialogue, null);
            builder.setView(dialogView);

            Message message = messages.get(position);

            // Find views in the dialog layout
            TextView nameText = dialogView.findViewById(R.id.reportTitle);
            TextInputEditText otherText = dialogView.findViewById(R.id.otherTextValue);
            Spinner spinner = dialogView.findViewById(R.id.spinner);
            Button submit = dialogView.findViewById(R.id.submitButton);

            nameText.setText("Report " + message.getSenderUsername());

            otherText.setVisibility(View.GONE);
            ArrayAdapter<BackendClient.ReportReason> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                    BackendClient.ReportReason.values());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            // Set items
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    BackendClient.ReportReason selectedValue = (BackendClient.ReportReason) adapterView.getItemAtPosition(i);
                    Log.d("MEssagead", selectedValue.toString() + BackendClient.ReportReason.OTHER.toString());
                    if (selectedValue.equals(BackendClient.ReportReason.OTHER)) {
                        otherText.setVisibility(View.VISIBLE);
                    } else {
                        otherText.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    otherText.setVisibility(View.GONE);
                }
            });

            final AlertDialog alertDialog = builder.create();
            // Submit report on submit clicked
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                List<Message> context = new ArrayList(messages);
                                backend.generateReport(
                                        message.getSenderUserId(),
                                        (BackendClient.ReportReason) spinner.getSelectedItem(),
                                        context,
                                        otherText.getText().toString()
                                );
                                Log.d("MessageAdapter", "user reported: " + message.getSenderUsername());
                            } catch (ApiException | RuntimeException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    alertDialog.dismiss();
                }
            });

            // Finally show dialog
            alertDialog.show();
            return true;
        }
    }
}
