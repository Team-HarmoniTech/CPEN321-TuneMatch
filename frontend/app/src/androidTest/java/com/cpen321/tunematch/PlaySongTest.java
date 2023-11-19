package com.cpen321.tunematch;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PlaySongTest {
    private ActivityScenario<LoginActivity> loginActivityScenario;

    // ChatGPT Usage: Partial
    @Before
    public void setUp() {
        Intents.init();
        loginActivityScenario = ActivityScenario.launch(LoginActivity.class);

        UiTestHelper.addDelay(45000);

        Intents.intended(IntentMatchers.hasComponent(MainActivity.class.getName()));
    }

    // ChatGPT Usage: Partial
    @Test
    public void testPlaySong() {
        testJoinSession();
        testSearchSong();
//        testQueue();
    }

    // ChatGPT Usage: Partial
    @After
    public void destroy() {
        loginActivityScenario.close();
        Intents.release();
    }

    // ChatGPT Usage: Partial
    public void testJoinSession() {
        // Assume that there exist at least one listening session created by another user
        // Clicks join button
        UiTestHelper.clickListChildItem(R.id.listeningSessionList, 0, R.id.joinBtn);

        // Checks if in the session
        List<Integer> idsToCheck = Arrays.asList(R.id.songSearchBar, R.id.queueBtn, R.id.chatBtn,
                                                    R.id.exitBtn, R.id.recycler_view);
        UiTestHelper.checkViewListAreDisplayed(idsToCheck);
    }

    // ChatGPT Usage: Partial
    public void testSearchSong() {
        //TODO: Typing text into search bar is not working

        // testInvalid input if specified
        UiTestHelper.inputMessage(R.id.songSearchBar, "?@}#{$%!", true);
        UiTestHelper.checkViewIsNotDisplayed(R.id.suggestionListView);

        // Test valid input
        UiTestHelper.inputMessage(R.id.songSearchBar, "snowman", true);
        UiTestHelper.checkViewIsDisplayed(R.id.suggestionListView);
        UiTestHelper.clickListItem(R.id.suggestionListView, 0);

        UiTestHelper.inputMessage(R.id.songSearchBar, "Last christmas", true);
        UiTestHelper.checkViewIsDisplayed(R.id.suggestionListView);
        UiTestHelper.clickListItem(R.id.suggestionListView, 0);
    }

    // ChatGPT Usage: No
    public void testQueue() {
        // Check Queue is displayed
        UiTestHelper.checkViewIsDisplayed(R.id.recycler_view);

        // Swipe second song on the list to top
        UiTestHelper.swipeListItem(R.id.recycler_view, 1, true);

        // Swipe first song on the list to bottom
        UiTestHelper.swipeListItem(R.id.recycler_view, 0, false);

        // TODO: Need a way to check that changing position worked correctly

    }

    public void testMediaPlayer() {
        // Click previous button to move song to start
        UiTestHelper.clickOnView(R.id.previous_button);
        UiTestHelper.checkTextIsDisplayed("00:00");

        // Click start button to start song
        UiTestHelper.checkBtnBackground(R.id.play_button, R.drawable.play_btn);
        UiTestHelper.clickOnView(R.id.play_button);
        UiTestHelper.checkBtnBackground(R.id.play_button, R.drawable.pause_btn);

        // Click pause button to pause song
        UiTestHelper.clickOnView(R.id.play_button);
        UiTestHelper.checkBtnBackground(R.id.play_button, R.drawable.play_btn);

        // Click next button to play next song in the queue

    }


}
