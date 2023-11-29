package com.cpen321.tunematch;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChatTest {

    private ActivityScenario<LoginActivity> loginActivityScenario;

    // ChatGPT Usage: Partial
    @Before
    public void setUp() {
        Intents.init();
        loginActivityScenario = ActivityScenario.launch(LoginActivity.class);
        UiTestHelper.clickOnView(R.id.spotify_login_button);

        UiTestHelper.addDelay(10000);

        Intents.intended(IntentMatchers.hasComponent(MainActivity.class.getName()));
    }

    // ChatGPT Usage: Partial
    @After
    public void destroy() {
        loginActivityScenario.close();
        Intents.release();
    }

    // ChatGPT Usage: No
    @Test
    public void A_testCreateSession() {
        UiTestHelper.clickOnView(R.id.createListeningSessionBtn);

        // Wait till move to listening session
        UiTestHelper.addDelay(1000);

        List<Integer> idsToCheck = Arrays.asList(R.id.songSearchBar, R.id.queueBtn, R.id.chatBtn, R.id.exitBtn);
        UiTestHelper.checkViewListDisplay(idsToCheck, true);
    }

    // ChatGPT Usage: No
    @Test
    public void B_testChatView() {
        A_testCreateSession();

        // Click chat button
        UiTestHelper.clickOnView(R.id.chatBtn);

        // Wait till change sub-frame to chat
        UiTestHelper.addDelay(1000);

        // Check all the view components are displayed
        List<Integer> idsToCheck = Arrays.asList(R.id.chatInput, R.id.sendChatButton, R.id.chatWindow);
        UiTestHelper.checkViewListDisplay(idsToCheck, true);
    }

    // ChatGPT Usage: No
    @Test
    public void C_testSendChat() {
        List<String> messages = Arrays.asList("Hi", "I like this song", "let's share playlist");
        sendChat(messages);
    }

    // ChatGPT Usage: No
    @Test
    public void D_testMsgPersist() {
        B_testChatView();

        List<String> messages = Arrays.asList("Hi", "I like this song", "let's share playlist");

        // Click Queue button to move out from chat
        UiTestHelper.clickOnView(R.id.queueBtn);

        // Check Queue views are displayed instead
        UiTestHelper.checkViewIsDisplayed(R.id.recycler_view);

        // Click Chat button to move back to chat
        UiTestHelper.clickOnView(R.id.chatBtn);

        // Check all the view components are displayed
        List<Integer> chatPageIds = Arrays.asList(R.id.chatInput, R.id.sendChatButton, R.id.chatWindow);
        UiTestHelper.checkViewListDisplay(chatPageIds, true);

        // Check messages still on the view with the desired order
        UiTestHelper.checkListOrder(R.id.chatWindow, messages);

        // Click profile page to move out from listening session
        UiTestHelper.clickOnView(R.id.navigation_profile);

        // Check profile page are displayed
        UiTestHelper.addDelay(1000);
        List<Integer> profilePageIds = Arrays.asList(R.id.friendsListBtn, R.id.topArtistsBtn,
                R.id.topGenresBtn, R.id.pfpImageView, R.id.searchIdText);
        UiTestHelper.checkViewListDisplay(profilePageIds, true);

        // Click Chat button to move back to chat
        UiTestHelper.clickOnView(R.id.navigation_room);
        UiTestHelper.addDelay(500);
        UiTestHelper.clickOnView(R.id.chatBtn);

        // Check all the view components are displayed
        UiTestHelper.checkViewListDisplay(chatPageIds, true);

        // Check messages still on the view with the desired order
        UiTestHelper.checkListOrder(R.id.chatWindow, messages);
    }

    // ChatGPT Usage: No
    @Test
    public void E_testSend30Msgs() {
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            messages.add("message " + i);
        }

        sendChat(messages);
    }

    // ChatGPT Usage: No
    @Test
    public void F_testExit() {
        B_testChatView();

        UiTestHelper.clickOnView(R.id.exitBtn);

        List<Integer> idsToCheck = Arrays.asList(R.id.listeningSessionTitle, R.id.listeningSessionList,
                R.id.friendsList, R.id.friendsListTitle);
        UiTestHelper.checkViewListDisplay(idsToCheck, true);
    }

    // ChatGPT Usage: No
    public void sendChat(List<String> messages) {
        B_testChatView();

        for (String msg : messages) {
            // type message then send
            UiTestHelper.inputMessage(R.id.chatInput, msg, false);
            UiTestHelper.clickOnView(R.id.sendChatButton);

            // Wait 1 seconds to send message
            UiTestHelper.addDelay(1000);

            // Check message was displayed
            UiTestHelper.checkTextIsDisplayed(msg);
        }
    }
}
