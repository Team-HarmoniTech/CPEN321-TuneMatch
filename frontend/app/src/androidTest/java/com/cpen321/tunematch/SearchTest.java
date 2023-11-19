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

    @Before
    public void setUp() {
        Intents.init();
        loginActivityScenario = ActivityScenario.launch(LoginActivity.class);

        UiTestHelper.addDelay(15000);

        Intents.intended(IntentMatchers.hasComponent(MainActivity.class.getName()));
    }

    @Test
    public void testSearch() {
        // Move to search friends page
        UiTestHelper.clickOnView(R.id.navigation_search);

        // Add delay to move to search page
        UiTestHelper.addDelay(1000);

        // Check for UI components
        List<Integer> idsToCheck = Arrays.asList(R.id.searchFriend, R.id.recommendedList);
        UiTestHelper.checkViewListAreDisplayed(idsToCheck);

        // Choose the first user on the list
        UiTestHelper.clickListItem(R.id.recommendedList, 0);

        // Check if request dialog is displayed
        idsToCheck = Arrays.asList(R.id.addButton, R.id.nameText, R.id.profileImage);
        UiTestHelper.checkViewListAreDisplayed(idsToCheck);

        // Click add button to send request
        UiTestHelper.clickOnView(R.id.addButton);
    }

    @After
    public void destroy() {
        loginActivityScenario.close();
        Intents.release();
    }
}