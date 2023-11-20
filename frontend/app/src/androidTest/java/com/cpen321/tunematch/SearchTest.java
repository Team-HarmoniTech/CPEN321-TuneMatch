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

    // ChatGPT Usage: No
    @Test
    public void A_testOpenSearchPage() {
        // Move to search friends page
        UiTestHelper.clickOnView(R.id.navigation_search);

        // Add delay to move to search page
        UiTestHelper.addDelay(1000);

        // Check for UI components
        List<Integer> idsToCheck = Arrays.asList(R.id.searchFriend, R.id.recommendedList);
        UiTestHelper.checkViewListDisplay(idsToCheck, true);
    }

    // ChatGPT Usage: No
    @Test
    public void B_testGetRecommendation() {
        A_testOpenSearchPage();

        // Check the list is not empty
        UiTestHelper.checkListIsEmpty(R.id.recommendedList, false);
    }

    // ChatGPT Usage: No
    @Test
    public void C_testSendRequestToMatch() {
        A_testOpenSearchPage();

        // Choose the first user on the list
        UiTestHelper.clickListItem(R.id.recommendedList, 0);

        // Check if request dialog is displayed
        UiTestHelper.addDelay(1000);
        List<Integer> idsToCheck = Arrays.asList(R.id.addButton, R.id.nameText, R.id.profileImage);
        UiTestHelper.checkViewListDisplay(idsToCheck, true);

        // Click add button to send request
        UiTestHelper.clickOnView(R.id.addButton);
    }

    // ChatGPT Usage: No
    @Test
    public void D_testSearchFriend() {
        A_testOpenSearchPage();

        // Test invalid string
        UiTestHelper.inputMessage(R.id.searchFriend, "!@#$%", true);
        UiTestHelper.addDelay(1000);
        UiTestHelper.checkListIsEmpty(R.id.recommendedList, true);
        UiTestHelper.checkToastMessage("User with username !@#$% does not exist.");
        UiTestHelper.addDelay(3000);

        // Test invalid username
        UiTestHelper.inputMessage(R.id.searchFriend, "testUser", true);
        UiTestHelper.addDelay(1000);
        UiTestHelper.checkListIsEmpty(R.id.recommendedList, true);
        UiTestHelper.checkToastMessage("User with username testUser does not exist.");

        // Test valid username
        UiTestHelper.inputMessage(R.id.searchFriend, VALID_USERNAME, true);
        UiTestHelper.addDelay(1000);
        UiTestHelper.checkListIsEmpty(R.id.recommendedList, false);
    }

    // ChatGPT Usage: No
    @Test
    public void E_testSendRequestToFriend() {
        A_testOpenSearchPage();

        // Test valid username
        UiTestHelper.inputMessage(R.id.searchFriend, VALID_USERNAME, true);
        UiTestHelper.addDelay(1000);
        UiTestHelper.checkListIsEmpty(R.id.recommendedList, false);

        // Choose the first user on the list
        UiTestHelper.clickListItem(R.id.recommendedList, 0);

        // Check if request dialog is displayed
        UiTestHelper.addDelay(3000);
        List<Integer> idsToCheck = Arrays.asList(R.id.addButton, R.id.nameText, R.id.profileImage);
        UiTestHelper.checkViewListDisplay(idsToCheck, true);

        // Check if dialog has the username we searched
        UiTestHelper.checkViewHasText(R.id.nameText, VALID_USERNAME);

        // Click add button to send request
        UiTestHelper.clickOnView(R.id.addButton);
    }

    // ChatGPT Usage: Partial
    @After
    public void destroy() {
        loginActivityScenario.close();
        Intents.release();
    }
}