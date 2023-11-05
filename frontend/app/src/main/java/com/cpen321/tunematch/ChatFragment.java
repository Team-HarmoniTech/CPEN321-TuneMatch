package com.cpen321.tunematch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatFragment extends Fragment {
    private RecyclerView chatWindow;
    private MessageAdapter chatAdapter;
    private TextInputEditText chatInput;
    private WebSocketService webSocketService;
    private boolean isServiceBound = false;
    ReduxStore model;

    // ChatGPT Usage: Partial
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            WebSocketService.LocalBinder binder = (WebSocketService.LocalBinder) service;
            webSocketService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };

    // ChatGPT Usage: No
    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), WebSocketService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // ChatGPT Usage: No
    @Override
    public void onPause() {
        super.onPause();
        if (isServiceBound) {
            getActivity().unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    // ChatGPT Usage: Partial
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chat, container, false);
        chatWindow = view.findViewById(R.id.chatWindow);
        chatInput = view.findViewById(R.id.chatInput);
        FloatingActionButton sendChat = view.findViewById(R.id.sendChatButton);

        model = ReduxStore.getInstance();
        initializeChat();

        model.getMessages().observe(getViewLifecycleOwner(), messages -> {
            chatAdapter.updateMessages(messages);
            // Scroll to the newly added item
            if (!messages.isEmpty()) {
                chatWindow.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });

        // Set focus to the chatInput
        chatInput.requestFocus();

        sendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSendMessage();
            }
        });

        return view;
    }

    // ChatGPT Usage: Partial
    private void onSendMessage() {
        String messageText = chatInput.getText().toString();
        chatInput.setText("");
        if (messageText.equals("")) return;

        Log.d("ChatFragment", "Send: " + messageText);
        Message message = new Message(model.getCurrentUser().getValue(), messageText, new Date());

        Gson gson = new Gson();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        JsonObject socketMessage = new JsonObject();
        socketMessage.add("method", gson.toJsonTree("SESSION"));
        socketMessage.add("action", gson.toJsonTree("message"));

        JsonObject body = new JsonObject();
        body.add("message", gson.toJsonTree(message.getMessageText()));
        body.add("timestamp", gson.toJsonTree(message.getTimestampString()));

        socketMessage.add("body", body);

        // Send the message via WebSocket
        if (isServiceBound && webSocketService != null) {
            webSocketService.sendMessage(socketMessage.toString());
        }

        model.addMessage(message, false);
    }

    // ChatGPT Usage: Partial
    private void initializeChat() {
        List<Message> chatMsg = model.getMessages().getValue();
        if (chatMsg == null) {
            chatMsg = new ArrayList<Message>();
            model.getMessages().setValue(chatMsg);
        }
        chatAdapter = new MessageAdapter(chatMsg,
                model.getCurrentUser().getValue(),
                getLayoutInflater(),
                requireContext(),
                new BackendClient(model.getCurrentUser().getValue().getUserId()));
        chatWindow.setAdapter(chatAdapter);
        chatWindow.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}
