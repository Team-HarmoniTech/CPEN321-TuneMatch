package com.cpen321.tunematch;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SearchTest {

    private ActivityScenario<LoginActivity> loginActivityScenario;
    private String VALID_USERNAME = "sojupapi";

    // ChatGPT Usage: Partial
    @Before
    public void setUp() {
        Intents.init();
        loginActivityScenario = ActivityScenario.launch(LoginActivity.class);

        UiTestHelper.addDelay(15000);

        Intents.intended(IntentMatchers.hasComponent(MainActivity.class.getName()));
    }

    @Test
    public void testOpenSearchPage() {
        // Move to search friends page
        UiTestHelper.clickOnView(R.id.navigation_search);

        // Add delay to move to search page
        UiTestHelper.addDelay(1000);

        // Check for UI components
        List<Integer> idsToCheck = Arrays.asList(R.id.searchFriend, R.id.recommendedList);
        UiTestHelper.checkViewListAreDisplayed(idsToCheck);
    }

    // ChatGPT Usage: No
    @Test
    public void testGetRecommendation() {
        testOpenSearchPage();

        // Check the list is not empty
        UiTestHelper.checkListIsEmpty(R.id.recommendedList, false);

        // TODO: check if the list is in descending order
        UiTestHelper.checkListInDescendingOrder(R.id.recommendedList);
    }

    // ChatGPT Usage: No
    @Test
    public void testSendRequestToMatch() {
        testOpenSearchPage();

        // Choose the first user on the list
        UiTestHelper.clickListItem(R.id.recommendedList, 0);

        // Check if request dialog is displayed
        List<Integer> idsToCheck = Arrays.asList(R.id.addButton, R.id.nameText, R.id.profileImage);
        UiTestHelper.checkViewListAreDisplayed(idsToCheck);

        // Click add button to send request
        UiTestHelper.clickOnView(R.id.addButton);
    }

    @Test
    public void testSearchFriend() {
        testOpenSearchPage();

        // Test invalid string
        UiTestHelper.inputMessage(R.id.searchFriend, "!@#$%\n", true);
        UiTestHelper.addDelay(1000);
        UiTestHelper.checkListIsEmpty(R.id.recommendedList, true);
        UiTestHelper.checkToastMessage("User with username !@#$% does not exist.");

        // Test invalid username
        UiTestHelper.inputMessage(R.id.searchFriend, "testUser\n", true);
        UiTestHelper.addDelay(1000);
        UiTestHelper.checkListIsEmpty(R.id.recommendedList, true);
        UiTestHelper.checkToastMessage("User with username testUser does not exist.");

        // Test valid username
        UiTestHelper.inputMessage(R.id.searchFriend, VALID_USERNAME+"\n", true);
        UiTestHelper.addDelay(1000);
        UiTestHelper.checkListIsEmpty(R.id.recommendedList, false);
    }

    // ChatGPT Usage: No
    @Test
    public void testSendRequestToFriend() {
        testSearchFriend();

        // Choose the first user on the list
        UiTestHelper.clickListItem(R.id.recommendedList, 0);

        // Check if request dialog is displayed
        List<Integer> idsToCheck = Arrays.asList(R.id.addButton, R.id.nameText, R.id.profileImage);
        UiTestHelper.checkViewListAreDisplayed(idsToCheck);

        // Check if dialog has the username we searched
        UiTestHelper.checkViewHasText(R.id.nameText, VALID_USERNAME);

        // Click add button to send request
        UiTestHelper.clickOnView(R.id.addButton);

        // Check if dialog disappeared
        for (int id : idsToCheck) {
            UiTestHelper.checkViewIsNotDisplayed(id);
        }
    }

    // ChatGPT Usage: Partial
    @After
    public void destroy() {
        loginActivityScenario.close();
        Intents.release();
    }
}