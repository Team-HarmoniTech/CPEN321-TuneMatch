package com.cpen321.tunematch;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.widget.SearchView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.assertion.PositionAssertions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.util.HumanReadables;

import org.hamcrest.Matcher;

import java.util.List;
import java.util.concurrent.TimeoutException;

public class UiTestHelper {

    // ChatGPT Usage: No
    public static void checkViewIsDisplayed(int viewId) {
        Espresso.onView(ViewMatchers.withId(viewId))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    // ChatGPT Usage: No
    public static void checkTextIsDisplayed(String text) {
        Espresso.onView(ViewMatchers.withText(text))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    // ChatGPT Usage: Partial
    public static void checkTextOrder(List<String> textInOrder) {
        for (int i = 0; i < textInOrder.size() - 1; i++) {
            String firstText = textInOrder.get(i);
            String secondText = textInOrder.get(i+1);

            Espresso.onView(ViewMatchers.withText(firstText))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                    .check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()));

            Espresso.onView(ViewMatchers.withText(secondText))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                    .check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))
                    .check(PositionAssertions.isCompletelyBelow(ViewMatchers.withText(firstText)));
        }
    }

    // ChatGPT Usage: No
    public static void clickOnView(int viewId) {
        Espresso.onView(ViewMatchers.withId(viewId))
                .perform(ViewActions.click());
    }

    // ChatGPT Usage: No
    public static void clickListItem(int listViewId, int pos) {
        Espresso.onData(anything())
                .inAdapterView(ViewMatchers.withId(listViewId))
                .atPosition(pos)
                .perform(ViewActions.click());
    }

    // ChatGPT Usage: No
    public static void checkViewListAreDisplayed(List<Integer> viewIds) {
        for (int id : viewIds) {
            checkViewIsDisplayed(id);
        }
    }

    // ChatGPT Usage: No
    public static void inputMessage(int viewId, String message, Boolean isSearch) {
        if (!isSearch) {
            Espresso.onView(ViewMatchers.withId(viewId))
                    .perform(ViewActions.typeText(message), ViewActions.closeSoftKeyboard());
        } else {
            Espresso.onView(ViewMatchers.withId(viewId))
                    .perform(typeSearchViewText(message));
        }
    }


    // ChatGPT Usage: No
    public static void addDelay(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ChatGPT Usage: Yes
    public static void clickListChildItem(int listViewId, int pos, int childViewId) {
        Espresso.onData(anything())
                .inAdapterView(ViewMatchers.withId(listViewId))
                .atPosition(pos)
                .perform(clickChildViewWithId(childViewId));
    }

    // ChatGPT Usage: Yes
    private static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified ID";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }

    // ChatGPT Usage: No
    public static void checkViewIsNotDisplayed(int viewId) {
        Espresso.onView(ViewMatchers.withId(viewId))
                .check(ViewAssertions.matches(not(ViewMatchers.isDisplayed())));
    }

    public static ViewAction typeSearchViewText(final String text){
        return new ViewAction(){
            @Override
            public Matcher<View> getConstraints() {
                //Ensure that only apply if it is a SearchView and if it is visible.
                return allOf(ViewMatchers.isDisplayed(), isAssignableFrom(SearchView.class));
            }

            @Override
            public String getDescription() {
                return "Change view text";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((SearchView) view).setQuery(text,false);
            }
        };
    }

    // ChatGPT Usage: No
    public static void swipeListItem(int listViewId, int pos, Boolean isUp) {
        if (isUp) {
            Espresso.onView(ViewMatchers.withId(listViewId))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(pos, ViewActions.swipeUp()));
        } else {
            Espresso.onView(ViewMatchers.withId(listViewId))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(pos, ViewActions.swipeDown()));
        }
    }

    public static void checkBtnBackground(int buttonViewId, int backgroundResId) {
        Espresso.onView(ViewMatchers.withId(buttonViewId))
                .check(ViewAssertions.matches(ViewMatchers.hasBackground(backgroundResId)));
    }

}

