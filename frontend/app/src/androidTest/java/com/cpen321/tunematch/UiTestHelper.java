package com.cpen321.tunematch;

import static org.hamcrest.CoreMatchers.anything;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.assertion.PositionAssertions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;

import java.util.List;

public class UiTestHelper {

    public static void checkViewIsDisplayed(int viewId) {
        Espresso.onView(ViewMatchers.withId(viewId))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    public static void checkTextIsDisplayed(String text) {
        Espresso.onView(ViewMatchers.withText(text))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    public static void checkTextOrder(List<String> texts) {
        for (int i = 0; i < texts.size() - 1; i++) {
            String firstText = texts.get(i);
            String secondText = texts.get(i+1);

            Espresso.onView(ViewMatchers.withText(firstText))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                    .check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()));

            Espresso.onView(ViewMatchers.withText(secondText))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                    .check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))
                    .check(PositionAssertions.isCompletelyBelow(ViewMatchers.withText(firstText)));
        }
    }

    public static void clickOnView(int viewId) {
        Espresso.onView(ViewMatchers.withId(viewId))
                .perform(ViewActions.click());
    }

    public static void clickListItem(int listViewId, int pos) {
        Espresso.onData(anything())
                .inAdapterView(ViewMatchers.withId(listViewId))
                .atPosition(pos)
                .perform(ViewActions.click());
    }

    public static void checkViewListAreDisplayed(List<Integer> viewIds) {
        for (int id : viewIds) {
            checkViewIsDisplayed(id);
        }
    }

    public static void inputMessage(int viewId, String message) {
        Espresso.onView(ViewMatchers.withId(viewId))
                .perform(ViewActions.typeText(message), ViewActions.closeSoftKeyboard());
    }

    public static void addDelay(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}

