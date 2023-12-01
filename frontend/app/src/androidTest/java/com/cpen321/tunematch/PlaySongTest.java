package com.cpen321.tunematch;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PlaySongTest {
    private ActivityScenario<LoginActivity> loginActivityScenario;

    // ChatGPT Usage: Partial
    @Before
    public void setUp() {
        Intents.init();
        loginActivityScenario = ActivityScenario.launch(LoginActivity.class);
        UiTestHelper.addDelay(1000);
        UiTestHelper.clickOnView(R.id.spotify_login_button);

        UiTestHelper.addDelay(15000);

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

        List<Integer> idsToCheck = Arrays.asList(R.id.songSearchBar, R.id.queueBtn, R.id.chatBtn,
                R.id.exitBtn, R.id.recycler_view);
        UiTestHelper.checkViewListDisplay(idsToCheck, true);
    }

    // ChatGPT Usage: Partial
    // Note: This require for another account to create listening session before the test
//    @Test
    public void B_testJoinSession() {
        // Clicks join button
        UiTestHelper.clickListChildItem(R.id.listeningSessionList, 0, R.id.joinBtn);

        // Wait till move to listening session
        UiTestHelper.addDelay(1000);

        // Checks if in the session
        List<Integer> idsToCheck = Arrays.asList(R.id.songSearchBar, R.id.queueBtn, R.id.chatBtn,
                R.id.exitBtn, R.id.recycler_view);
        UiTestHelper.checkViewListDisplay(idsToCheck, true);
    }

    // ChatGPT Usage: No
    @Test
    public void C_testEmptyQueue() {
        A_testCreateSession();

        // Make sure that Queue is empty
        UiTestHelper.checkListIsEmpty(R.id.recycler_view, true);

        // Test next button with empty queue
        UiTestHelper.clickOnView(R.id.next_button);
        UiTestHelper.checkToastMessage("Queue is empty");
        UiTestHelper.addDelay(2000);
    }

    // ChatGPT Usage: No
    @Test
    public void D_testNextButton() {
        A_testCreateSession();

        // Add known number of songs to the queue
        List<String> songList = Arrays.asList("Santa", "Snowman", "Last", "All");
        for (String title : songList) {
            searchSong(title);
        }

        // Check when next button is clicked, have pause button and queue length decrease
        for (int size = songList.size()-1; size >= 0; size--) {
            UiTestHelper.clickOnView(R.id.next_button);

            UiTestHelper.checkListSize(R.id.recycler_view, size);
            UiTestHelper.checkSeekBarPosition(0);
        }
    }

    // ChatGPT Usage: No
    @Test
    public void E_testPlayButton() {
        A_testCreateSession();

        for (int i = 0; i < 9; i++) {
            UiTestHelper.clickOnView(R.id.play_button);
            UiTestHelper.addDelay(1000);
        }
    }

    // ChatGPT Usage: Partial
    @Test
    public void F_testRestartButton() {
        A_testCreateSession();

        for (int i = 0; i < 5; i++) {
            UiTestHelper.addDelay(i * 1000);
            UiTestHelper.clickOnView(R.id.restart_button);
            UiTestHelper.checkSeekBarPosition(0);
        }
    }

    // ChatGPT Usage: Partial
    @Test
    public void G_testSearchSong() {
        A_testCreateSession();

        // testInvalid input if specified
        UiTestHelper.inputMessage(R.id.songSearchBar, "?@}#{$%!", true);
        UiTestHelper.checkViewIsNotDisplayed(R.id.suggestionListView);

        // Test valid input
        // Have snowman twice since first click would play the song
        List<String> songTitle = Arrays.asList("Santa", "Snowman", "Last", "All");
        for (String song : songTitle) {
            searchSong(song);

            UiTestHelper.checkListItemContainStr(R.id.recycler_view, song);
        }

        // Check if queue all the songs in right order
        UiTestHelper.checkListOrder(R.id.recycler_view, songTitle);

    }

    // ChatGPT Usage: No
    @Test
    public void H_testQueue() {
        A_testCreateSession();

        // Check Queue is displayed
        UiTestHelper.checkViewIsDisplayed(R.id.recycler_view);
        UiTestHelper.swipeListItem(R.id.recycler_view, 1, true);
        UiTestHelper.addDelay(1000);
        UiTestHelper.swipeListItem(R.id.recycler_view, 0, false);

        // Check if order is changed correctly
        List<String> expectedOrder = Arrays.asList("Santa", "Snowman", "Last", "All");
        UiTestHelper.checkListOrder(R.id.recycler_view, expectedOrder);
    }

    // ChatGPT Usage: No
    @Test
    public void I_testSeekBar() {
        A_testCreateSession();

        for (int i = 0; i < 10; i++) {
            UiTestHelper.setSeekBarPosition(i * 10);
            UiTestHelper.checkSeekBarPosition(i * 10);
        }

    }

    // ChatGPT Usage: No
    @Test
    public void J_testExit() {
        A_testCreateSession();

        UiTestHelper.clickOnView(R.id.exitBtn);

        List<Integer> idsToCheck = Arrays.asList(R.id.listeningSessionTitle, R.id.listeningSessionList,
                R.id.friendsList, R.id.friendsListTitle);
        UiTestHelper.checkViewListDisplay(idsToCheck, true);
    }

    public void searchSong(String title) {
        // Search song
        UiTestHelper.inputMessage(R.id.songSearchBar, title, true);
        UiTestHelper.addDelay(3000);

        // Add to queue
        UiTestHelper.checkViewIsDisplayed(R.id.suggestionListView);
        UiTestHelper.clickListItem(R.id.suggestionListView, 0);
        UiTestHelper.addDelay(1000);
    }


}
