package com.cpen321.tunematch;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            return new MessageViewHolder(
                    LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_sent_message, parent, false)
            );
        } else  {
            return new MessageViewHolderWithImage(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_recieved_message, parent, false)
            );
        }
    }

    // ChatGPT Usage: No
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (getItemViewType(position) == MESSAGE_TYPE_SENT) {
            MessageViewHolder view = (MessageViewHolder) holder;
            view.getMessageText().setText(message.getMessageText());
        } else {
            MessageViewHolderWithImage view = (MessageViewHolderWithImage) holder;
            view.getMessageText().setText(message.getMessageText());

            //Add Report OnLongClickListener
            view.getMessageText().setOnLongClickListener(new OnLongClickReportListener(position));
        }
        recalculateView(holder, position);
    }

    // ChatGPT Usage: No
    public void updateMessages(List<Message> messages) {
        this.messages = messages;
        this.notifyDataSetChanged();
    }

    // ChatGPT Usage: No
    public void recalculateView(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (getItemViewType(position) == MESSAGE_TYPE_SENT) {
            MessageViewHolder view = (MessageViewHolder) holder;
            // Set padding to 0 if next to message from same user
            ConstraintLayout container = view.getContainer();
            container.setPadding(
                    container.getPaddingLeft(),
                    2,
                    container.getPaddingRight(),
                    isFirstMessage(position) ? getBottomSize() : 2);
        } else {
            MessageViewHolderWithImage view = (MessageViewHolderWithImage) holder;
            // Set padding to 0 if next to message from same user
            ConstraintLayout container = view.getContainer();
            container.setPadding(
                    container.getPaddingLeft(),
                    2,
                    container.getPaddingRight(),
                    isFirstMessage(position) ? getBottomSize() : 2);

            // Only render image if first
            if (isFirstMessage(position)) {
                new Thread(new DownloadImage(
                        view.getProfileImage(),
                        message.getSenderProfileImageUrl(),
                        R.drawable.default_profile_image
                )).start();
            } else {
                view.getProfileImage().setVisibility(View.INVISIBLE);
            }
        }
    }

    public int getBottomSize() {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8,
                this.context.getResources().getDisplayMetrics()
        );
    }

    // ChatGPT Usage: No
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ChatGPT Usage: No
    public boolean isFirstMessage(int position) {
        try {
            return !Objects.equals(messages.get(position + 1).getSenderUserId(), messages.get(position).getSenderUserId());
        } catch (IndexOutOfBoundsException e) {
            return true;
        }
    }

    // ChatGPT Usage: No
    @Override
    public int getItemViewType(int position) {
        Log.d("type", messages.get(position).getSenderUserId() + " and " + currentUser.getUserId());
        if (messages.get(position).getSenderUserId().equals(currentUser.getUserId())) {
            return MESSAGE_TYPE_SENT;
        } else {
            return MESSAGE_TYPE_RECEIVED;
        }
    }

    // ChatGPT Usage: No
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final ConstraintLayout container;
        public TextView getMessageText() {
            return messageText;
        }
        public ConstraintLayout getContainer() {
            return container;
        }
        public MessageViewHolder(@NonNull View view) {
            super(view);
            container = (ConstraintLayout) view.findViewById(R.id.container);
            messageText = (TextView) view.findViewById(R.id.textMessage);
        }
    }

    // ChatGPT Usage: No
    static class MessageViewHolderWithImage extends MessageViewHolder {
        private final ImageView profileImage;
        public ImageView getProfileImage() {
            return profileImage;
        }
        public MessageViewHolderWithImage(@NonNull View view) {
            super(view);
            profileImage = (ImageView) view.findViewById(R.id.profileImage);
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

            // Get corresponding message
            Message message = messages.get(position);

            // Find views in the dialog layout
            TextView nameText = dialogView.findViewById(R.id.reportTitle);
            TextInputEditText otherText = dialogView.findViewById(R.id.otherTextValue);
            Spinner spinner = dialogView.findViewById(R.id.spinner);
            Button submit = dialogView.findViewById(R.id.submitButton);

            // Initialize Start Values
            nameText.setText("Report " + message.getSenderUsername());
            otherText.setVisibility(View.GONE);
            ArrayAdapter<BackendClient.ReportReason> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                    BackendClient.ReportReason.values());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            // Hide other text box unless other is checked
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    BackendClient.ReportReason selectedValue = (BackendClient.ReportReason) adapterView.getItemAtPosition(i);
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
                                BackendClient.ReportReason reason = (BackendClient.ReportReason) spinner.getSelectedItem();
                                backend.generateReport(
                                        message.getSenderUserId(),
                                        reason,
                                        context,
                                        reason.equals(BackendClient.ReportReason.OTHER)
                                                ? otherText.getText().toString()
                                                : null
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
