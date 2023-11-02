package com.cpen321.tunematch;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ChatFragment extends Fragment {
    private View view;
    private RecyclerView chatWindow;
    private MessageAdapter chatAdapter;
    private TextInputEditText chatInput;
    private FloatingActionButton sendChat;
    private List<Message> messages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_chat, container, false);
        chatWindow = view.findViewById(R.id.chatWindow);
        chatInput = view.findViewById(R.id.chatInput);
        sendChat = view.findViewById(R.id.sendChatButton);

        initializeChat();
        sendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSendMessage();
            }
        });
        return view;
    }

    private void onSendMessage() {
        String messageText = chatInput.getText().toString();
        if (messageText == "") return;

        Message message = new Message(currentUser, messageText, new Date());
        // TODO: SEND MESSAGE IN THE WEBSOCKET
        addMessage(message);
    }

    private void addMessage(Message message) {
        messages.add(message);
        Collections.sort(messages);
        chatAdapter.notifyDataSetChanged();
    }

    private void initializeChat() {
        messages = new ArrayList<>();
        SessionUser currentUser = null;
        chatAdapter = new MessageAdapter(messages, currentUser);
        chatWindow.setAdapter(chatAdapter);
        chatWindow.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}
