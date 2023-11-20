package com.cpen321.tunematch;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.widget.SearchView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.Root;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.assertion.PositionAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;
import java.util.Map;

public class UiTestHelper {

    // ChatGPT Usage: No
    public static void checkViewIsDisplayed(int viewId) {
        onView(withId(viewId)).check(matches(isDisplayed()));
    }

    // ChatGPT Usage: Yes
    public static void checkViewHasText(int viewId, String text) {
        onView(withId(viewId))
                .check(matches(withText((Matchers.containsString(text)))));
    }

    // ChatGPT Usage: No
    public static void checkTextIsDisplayed(String text) {
        onView(withText(text)).check(matches(isDisplayed()));
    }

    public static void checkListOrder(int recyclerViewId, List<String> textInOrder) {
        for (int i = 0; i < textInOrder.size() - 1; i++) {
            String firstText = textInOrder.get(i);
            String secondText = textInOrder.get(i + 1);

            onView(withId(recyclerViewId))
                    .perform(RecyclerViewActions.scrollToPosition(i))
                    .check(matches(isDisplayed()))
                    .check(matches(isCompletelyDisplayed()))
                    .check(matches(hasDescendant(withText(firstText))));

            onView(withId(recyclerViewId))
                    .perform(RecyclerViewActions.scrollToPosition(i + 1))
                    .check(matches(isDisplayed()))
                    .check(matches(isCompletelyDisplayed()))
                    .check(matches(hasDescendant(withText(secondText))));
        }
    }

    // ChatGPT Usage: No
    public static void clickOnView(int viewId) {
        onView(withId(viewId))
                .perform(click());
    }

    // ChatGPT Usage: No
    public static void clickListItem(int listViewId, int pos) {
        onData(anything())
                .inAdapterView(withId(listViewId))
                .atPosition(pos)
                .perform(click());
    }

    // ChatGPT Usage: No
    public static void checkViewListDisplay(List<Integer> viewIds, boolean isDisplay) {
        if (isDisplay) {
            for (int id : viewIds) {
                checkViewIsDisplayed(id);
            }
        } else {
            for (int id : viewIds) {
                checkViewIsNotDisplayed(id);
            }
        }
    }

    // ChatGPT Usage: No
    public static void inputMessage(int viewId, String message, Boolean isSearch) {
        if (!isSearch) {
            onView(withId(viewId))
                    .perform(ViewActions.typeText(message), ViewActions.closeSoftKeyboard());
        } else {
            onView(withId(viewId))
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
        onData(anything())
                .inAdapterView(withId(listViewId))
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
        onView(withId(viewId))
                .check(matches(not(isDisplayed())));
    }

    // ChatGPT Usage: Partial
    public static ViewAction typeSearchViewText(final String text){
        return new ViewAction(){
            @Override
            public Matcher<View> getConstraints() {
                //Ensure that only apply if it is a SearchView and if it is visible.
                return allOf(isDisplayed(), isAssignableFrom(SearchView.class));
            }

            @Override
            public String getDescription() {
                return "Change search view text";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((SearchView) view).setQuery(text,false);

                KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
                ((SearchView) view).dispatchKeyEvent(keyEvent);
            }
        };
    }

    // ChatGPT Usage: No
    public static void swipeListItem(int listViewId, int pos, Boolean isUp) {
        if (isUp) {
            onView(withId(listViewId))
                    .perform(actionOnItemAtPosition(pos, ViewActions.longClick()))
                    .perform(actionOnItemAtPosition(pos, ViewActions.swipeUp()));
        } else {
            onView(withId(listViewId))
                    .perform(actionOnItemAtPosition(pos, ViewActions.longClick()))
                    .perform(actionOnItemAtPosition(pos, ViewActions.swipeDown()));
        }
    }

    // ChatGPT Usage: No
    public static void checkBtnBackground(int buttonViewId, int backgroundResId) {
        onView(withId(buttonViewId))
                .check(matches(ViewMatchers.hasBackground(backgroundResId)));
    }

    // ChatGPT Usage: No
    public static void checkListSize(int listViewId, int size) {
        onView(withId(listViewId))
                .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                .check(matches(ViewMatchers.hasChildCount(size)));
    }

    // ChatGPT Usage: Yes
    public static void checkListIsEmpty(int listViewId, boolean isEmpty) {
        if (!isEmpty) {
            onView(withId(listViewId))
                    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .check(matches(Matchers.not(ViewMatchers.hasChildCount(0))))
                    .check(matches(isDisplayed()));
        } else {
            onView(withId(listViewId))
                    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .check(matches(ViewMatchers.hasChildCount(0)));
        }
    }

    public static void checkListItemContainStr(int listViewId, String substr) {
        onView(withId(listViewId))
                .check(matches(hasDescendant(withText(Matchers.containsString(substr)))));
    }

    // ChatGPT Usage: No
    public static void checkToastMessage(String msg) {
        onView(withText(msg))
                .inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    // ChatGPT Usage: No
    public static class ToastMatcher extends TypeSafeMatcher<Root> {
        @Override
        public void describeTo(Description description) {
            description.appendText("is toast");
        }

        @Override
        public boolean matchesSafely(Root root) {
            int type = root.getWindowLayoutParams().get().type;
            if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
                IBinder windowToken = root.getDecorView().getWindowToken();
                IBinder appToken = root.getDecorView().getApplicationWindowToken();
                if (windowToken == appToken) {
                    return true;
                }
            }
            return false;
        }
    }

    // ChatGPT Usage: Partial
    public static void checkListInDescendingOrder(int listViewId) {
        // Replace with the actual ListView ID
        ViewInteraction listView = Espresso.onView(withId(listViewId));

        // Assuming the list item format is "name (number)"
        // Retrieve list items dynamically
        listView.check(matches(matchesListInDescendingOrder()));
    }

    // ChatGPT Usage: Partial
    private static Matcher<Object> matchesListInDescendingOrder() {
        return new BoundedMatcher<Object, String>(String.class) {
            @Override
            public boolean matchesSafely(String listItem) {
                // Extract the numeric value from the list item
                int number = extractNumber(listItem);
                // Compare with previous item (if available)
                // You can adapt this logic based on your specific requirements
                // For simplicity, I'm assuming the first item is always in descending order
                return number >= previousNumber;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("List items in descending order");
            }
        };
    }

    private static int previousNumber = Integer.MAX_VALUE; // Initialize with a large value

    // ChatGPT Usage: Partial
    private static int extractNumber(String listItem) {
        // Extract the numeric value from the list item
        String numberString = listItem.substring(listItem.lastIndexOf("(") + 1, listItem.lastIndexOf("%"));
        int number = Integer.parseInt(numberString.trim());

        // Update previousNumber for comparison with the next item
        previousNumber = number;
        return number;
    }

    // ChatGPT Usage: No
    public static void setSeekBarPosition(int pos) {
        onView(withClassName(Matchers.equalTo(SeekBar.class.getName()))).perform(setProgress(pos));

    }

    // ChatGPT Usage: No
    public static ViewAction setProgress(final int progress) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                SeekBar seekBar = (SeekBar) view;
                seekBar.setProgress(progress);
            }
            @Override
            public String getDescription() {
                return "Set a progress on a SeekBar";
            }
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(SeekBar.class);
            }
        };
    }

    // ChatGPT Usage: No
    public static void checkSeekBarPosition(int position) {
        onView(withId(R.id.seekBar))
                .check(matches(withProgress(position)));
    }

    // ChatGPT Usage: No
    public static Matcher<View> withProgress(final int expectedProgress) {
        return new BoundedMatcher<View, SeekBar>(SeekBar.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("Seek bar with progress: " + expectedProgress);
            }

            @Override
            protected boolean matchesSafely(SeekBar seekBar) {
                return seekBar.getProgress() == expectedProgress;
            }
        };
    }
}

