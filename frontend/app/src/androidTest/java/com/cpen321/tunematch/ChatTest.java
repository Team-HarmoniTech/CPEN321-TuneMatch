package com.cpen321.tunematch;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatTest {
//    @Rule
//    public ActivityScenarioRule<LoginActivity> activityRule
//            = new ActivityScenarioRule<>(LoginActivity.class);

    private ActivityScenario<LoginActivity> loginActivityScenario;

    @Before
    public void setUp() {
        Intents.init();
        loginActivityScenario = ActivityScenario.launch(LoginActivity.class);

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intents.intended(IntentMatchers.hasComponent(MainActivity.class.getName()));
    }

    @Test
    public void testChat() {
        testCreateSession();
        testChatView();

        List<String> messages = Arrays.asList("Hi", "I like this song");
        testSendChat(messages);
        testMsgPersist(messages);
    }

    @After
    public void destroy() {
        loginActivityScenario.close();
        Intents.release();
    }

    public void testCreateSession() {
        UiTestHelper.clickOnView(R.id.createListeningSessionBtn);

        // Wait till move to listening session
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Integer> idsToCheck = Arrays.asList(R.id.songSearchBar, R.id.queueBtn, R.id.chatBtn, R.id.exitBtn);
        UiTestHelper.checkViewListAreDisplayed(idsToCheck);
    }

    public void testChatView() {
        // Click chat button
        UiTestHelper.clickOnView(R.id.chatBtn);

        // Wait till change sub-frame to chat
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check all the view components are displayed
        List<Integer> idsToCheck = Arrays.asList(R.id.chatInput, R.id.sendChatButton, R.id.chatWindow);
        UiTestHelper.checkViewListAreDisplayed(idsToCheck);
    }

    public void testSendChat(List<String> messages) {
        for (String msg : messages) {
            // type message then send
            UiTestHelper.inputMessage(R.id.chatInput, msg);
            UiTestHelper.clickOnView(R.id.sendChatButton);

            // Wait 1 seconds to send message
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Check message was displayed
            UiTestHelper.checkTextIsDisplayed(msg);
        }

        // Check order of messages displayed
        UiTestHelper.checkTextOrder(messages);
    }

    public void testMsgPersist(List<String> messages) {
        // Click Queue button to move out from chat
        UiTestHelper.clickOnView(R.id.queueBtn);

        // Check Queue views are displayed instead
        UiTestHelper.checkViewIsDisplayed(R.id.recycler_view);

        // Click Chat button to move back to chat
        testChatView();

        // Check messages still on the view with the desired order
        UiTestHelper.checkTextOrder(messages);
    }
}
