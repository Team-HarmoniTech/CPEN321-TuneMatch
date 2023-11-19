package com.cpen321.tunematch;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ChatTest {

    private ActivityScenario<LoginActivity> loginActivityScenario;

    // ChatGPT Usage: Partial
    @Before
    public void setUp() {
        Intents.init();
        loginActivityScenario = ActivityScenario.launch(LoginActivity.class);

        UiTestHelper.addDelay(20000);

        Intents.intended(IntentMatchers.hasComponent(MainActivity.class.getName()));
    }

    // ChatGPT Usage: Partial
    @Test
    public void testChat() {
        testCreateSession();
        testChatView();

        List<String> messages = Arrays.asList("Hi", "I like this song");
        testSendChat(messages);
        testMsgPersist(messages);
    }

    // ChatGPT Usage: Partial
    @After
    public void destroy() {
        loginActivityScenario.close();
        Intents.release();
    }

    // ChatGPT Usage: No
    public void testCreateSession() {
        UiTestHelper.clickOnView(R.id.createListeningSessionBtn);

        // Wait till move to listening session
        UiTestHelper.addDelay(1000);

        List<Integer> idsToCheck = Arrays.asList(R.id.songSearchBar, R.id.queueBtn, R.id.chatBtn, R.id.exitBtn);
        UiTestHelper.checkViewListAreDisplayed(idsToCheck);
    }

    // ChatGPT Usage: No
    public void testChatView() {
        // Click chat button
        UiTestHelper.clickOnView(R.id.chatBtn);

        // Wait till change sub-frame to chat
        UiTestHelper.addDelay(1000);

        // Check all the view components are displayed
        List<Integer> idsToCheck = Arrays.asList(R.id.chatInput, R.id.sendChatButton, R.id.chatWindow);
        UiTestHelper.checkViewListAreDisplayed(idsToCheck);
    }

    // ChatGPT Usage: No
    public void testSendChat(List<String> messages) {
        for (String msg : messages) {
            // type message then send
            UiTestHelper.inputMessage(R.id.chatInput, msg, false);
            UiTestHelper.clickOnView(R.id.sendChatButton);

            // Wait 1 seconds to send message
            UiTestHelper.addDelay(1000);

            // Check message was displayed
            UiTestHelper.checkTextIsDisplayed(msg);
        }

        // Check order of messages displayed
        UiTestHelper.checkTextOrder(messages);
    }

    // ChatGPT Usage: No
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
