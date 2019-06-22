package com.porfirio.orariprocida2011.activities;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.porfirio.orariprocida2011.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class NavigationTest {

    @Rule
    public ActivityTestRule<OrariProcida2011Activity> mActivityTestRule = new ActivityTestRule<>(OrariProcida2011Activity.class);

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Test
    public void navigationTest() {
        ViewInteraction spinner = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner.perform(click());

        DataInteraction checkedTextView = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(0);
        checkedTextView.perform(click());

        ViewInteraction spinner2 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner2.perform(click());

        DataInteraction checkedTextView2 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(0);
        checkedTextView2.perform(click());

        ViewInteraction spinner3 = onView(
                allOf(withId(R.id.spnNave),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        spinner3.perform(click());

        DataInteraction checkedTextView3 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(1);
        checkedTextView3.perform(click());

        ViewInteraction spinner4 = onView(
                allOf(withId(R.id.spnNave),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        spinner4.perform(click());

        DataInteraction checkedTextView4 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(2);
        checkedTextView4.perform(click());

        ViewInteraction spinner5 = onView(
                allOf(withId(R.id.spnNave),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        spinner5.perform(click());

        DataInteraction checkedTextView5 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(3);
        checkedTextView5.perform(click());

        DataInteraction textView = onData(anything())
                .inAdapterView(allOf(withId(R.id.listMezzi),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                4)))
                .atPosition(0);
        textView.perform(click());

        ViewInteraction button = onView(
                allOf(withId(R.id.btnBiglietterie), withText("Chiama la biglietteria"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                7),
                        isDisplayed()));
        button.perform(click());

        ViewInteraction button2 = onView(
                allOf(withId(R.id.btnBack), withText("Indietro"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        button2.perform(click());

        ViewInteraction button3 = onView(
                allOf(withId(R.id.btnTaxi), withText("Chiama un taxi"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                8),
                        isDisplayed()));
        button3.perform(click());

        ViewInteraction button4 = onView(
                allOf(withId(R.id.btnBackTaxi), withText("Indietro"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        button4.perform(click());

        ViewInteraction button5 = onView(
                allOf(withId(R.id.btnConfermaOSmentisci), withText("Conferma la regolarita' o segnala un problema per questa corsa"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                9),
                        isDisplayed()));
        button5.perform(click());

        ViewInteraction button6 = onView(
                allOf(withId(R.id.btnConferma), withText("Conferma"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                10),
                        isDisplayed()));
        button6.perform(click());

        ViewInteraction button7 = onView(
                allOf(withId(R.id.btnReturnToHome), withText("Esci"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                10),
                        isDisplayed()));
        button7.perform(click());

        ViewInteraction spinner6 = onView(
                allOf(withId(R.id.spnNave),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        spinner6.perform(click());

        DataInteraction checkedTextView6 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(4);
        checkedTextView6.perform(click());

        DataInteraction textView2 = onData(anything())
                .inAdapterView(allOf(withId(R.id.listMezzi),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                4)))
                .atPosition(0);
        textView2.perform(click());

        ViewInteraction button8 = onView(
                allOf(withId(R.id.btnReturnToHome), withText("Esci"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                10),
                        isDisplayed()));
        button8.perform(click());

        ViewInteraction spinner7 = onView(
                allOf(withId(R.id.spnNave),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        spinner7.perform(click());

        DataInteraction checkedTextView7 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(5);
        checkedTextView7.perform(click());

        DataInteraction textView3 = onData(anything())
                .inAdapterView(allOf(withId(R.id.listMezzi),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                4)))
                .atPosition(0);
        textView3.perform(click());

        ViewInteraction button9 = onView(
                allOf(withId(R.id.btnBiglietterie), withText("Chiama la biglietteria"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                7),
                        isDisplayed()));
        button9.perform(click());

        ViewInteraction button10 = onView(
                allOf(withId(R.id.btnBack), withText("Indietro"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        button10.perform(click());

        ViewInteraction button11 = onView(
                allOf(withId(R.id.btnReturnToHome), withText("Esci"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                10),
                        isDisplayed()));
        button11.perform(click());

        ViewInteraction spinner8 = onView(
                allOf(withId(R.id.spnNave),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        spinner8.perform(click());

        DataInteraction checkedTextView8 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(6);
        checkedTextView8.perform(click());

        DataInteraction textView4 = onData(anything())
                .inAdapterView(allOf(withId(R.id.listMezzi),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                4)))
                .atPosition(0);
        textView4.perform(click());

        ViewInteraction button12 = onView(
                allOf(withId(R.id.btnBiglietterie), withText("Chiama la biglietteria"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                7),
                        isDisplayed()));
        button12.perform(click());

        ViewInteraction button13 = onView(
                allOf(withId(R.id.btnBack), withText("Indietro"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        button13.perform(click());

        ViewInteraction button14 = onView(
                allOf(withId(R.id.btnTaxi), withText("Chiama un taxi"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                8),
                        isDisplayed()));
        button14.perform(click());

        ViewInteraction button15 = onView(
                allOf(withId(R.id.btnBackTaxi), withText("Indietro"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        button15.perform(click());

        ViewInteraction button16 = onView(
                allOf(withId(R.id.btnReturnToHome), withText("Esci"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                10),
                        isDisplayed()));
        button16.perform(click());

        ViewInteraction spinner9 = onView(
                allOf(withId(R.id.spnNave),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        spinner9.perform(click());

        DataInteraction checkedTextView9 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(7);
        checkedTextView9.perform(click());

        DataInteraction textView5 = onData(anything())
                .inAdapterView(allOf(withId(R.id.listMezzi),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                4)))
                .atPosition(0);
        textView5.perform(click());

        ViewInteraction button17 = onView(
                allOf(withId(R.id.btnBiglietterie), withText("Chiama la biglietteria"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                7),
                        isDisplayed()));
        button17.perform(click());

        ViewInteraction button18 = onView(
                allOf(withId(R.id.btnBack), withText("Indietro"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        button18.perform(click());

        ViewInteraction button19 = onView(
                allOf(withId(R.id.btnTaxi), withText("Chiama un taxi"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                8),
                        isDisplayed()));
        button19.perform(click());

        ViewInteraction button20 = onView(
                allOf(withId(R.id.btnBackTaxi), withText("Indietro"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        button20.perform(click());

        ViewInteraction button21 = onView(
                allOf(withId(R.id.btnReturnToHome), withText("Esci"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                10),
                        isDisplayed()));
        button21.perform(click());

        ViewInteraction spinner10 = onView(
                allOf(withId(R.id.spnNave),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        spinner10.perform(click());

        DataInteraction checkedTextView10 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(8);
        checkedTextView10.perform(click());

        ViewInteraction spinner11 = onView(
                allOf(withId(R.id.spnNave),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        spinner11.perform(click());

        DataInteraction checkedTextView11 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(9);
        checkedTextView11.perform(click());

        ViewInteraction spinner12 = onView(
                allOf(withId(R.id.spnNave),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        spinner12.perform(click());

        DataInteraction checkedTextView12 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(10);
        checkedTextView12.perform(click());

        ViewInteraction spinner13 = onView(
                allOf(withId(R.id.spnNave),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        spinner13.perform(click());

        DataInteraction checkedTextView13 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(0);
        checkedTextView13.perform(click());

        ViewInteraction spinner14 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner14.perform(click());

        DataInteraction checkedTextView14 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(1);
        checkedTextView14.perform(click());

        ViewInteraction spinner15 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner15.perform(click());

        DataInteraction checkedTextView15 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(2);
        checkedTextView15.perform(click());

        ViewInteraction spinner16 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner16.perform(click());

        DataInteraction checkedTextView16 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(3);
        checkedTextView16.perform(click());

        ViewInteraction spinner17 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner17.perform(click());

        DataInteraction checkedTextView17 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(4);
        checkedTextView17.perform(click());

        ViewInteraction spinner18 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner18.perform(click());

        DataInteraction checkedTextView18 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(5);
        checkedTextView18.perform(click());

        ViewInteraction spinner19 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner19.perform(click());

        DataInteraction checkedTextView19 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(6);
        checkedTextView19.perform(click());

        ViewInteraction spinner20 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner20.perform(click());

        DataInteraction checkedTextView20 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(7);
        checkedTextView20.perform(click());

        ViewInteraction spinner21 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner21.perform(click());

        DataInteraction checkedTextView21 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(8);
        checkedTextView21.perform(click());

        ViewInteraction spinner22 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner22.perform(click());

        DataInteraction checkedTextView22 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(9);
        checkedTextView22.perform(click());

        ViewInteraction spinner23 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner23.perform(click());

        DataInteraction checkedTextView23 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(10);
        checkedTextView23.perform(click());

        ViewInteraction spinner24 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner24.perform(click());

        DataInteraction checkedTextView24 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(11);
        checkedTextView24.perform(click());

        ViewInteraction spinner25 = onView(
                allOf(withId(R.id.spnPortoPartenza),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        spinner25.perform(click());

        DataInteraction checkedTextView25 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(12);
        checkedTextView25.perform(click());

        ViewInteraction spinner26 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner26.perform(click());

        DataInteraction checkedTextView26 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(0);
        checkedTextView26.perform(click());

        ViewInteraction spinner27 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner27.perform(click());

        DataInteraction checkedTextView27 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(1);
        checkedTextView27.perform(click());

        ViewInteraction spinner28 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner28.perform(click());

        DataInteraction checkedTextView28 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(3);
        checkedTextView28.perform(click());

        ViewInteraction spinner29 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner29.perform(click());

        DataInteraction checkedTextView29 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(4);
        checkedTextView29.perform(click());

        ViewInteraction spinner30 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner30.perform(click());

        DataInteraction checkedTextView30 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(5);
        checkedTextView30.perform(click());

        ViewInteraction spinner31 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner31.perform(click());

        DataInteraction checkedTextView31 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(6);
        checkedTextView31.perform(click());

        ViewInteraction spinner32 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner32.perform(click());

        DataInteraction checkedTextView32 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(7);
        checkedTextView32.perform(click());

        ViewInteraction spinner33 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner33.perform(click());

        DataInteraction checkedTextView33 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(8);
        checkedTextView33.perform(click());

        ViewInteraction spinner34 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner34.perform(click());

        DataInteraction checkedTextView34 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(9);
        checkedTextView34.perform(click());

        ViewInteraction spinner35 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner35.perform(click());

        DataInteraction checkedTextView35 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(10);
        checkedTextView35.perform(click());

        ViewInteraction spinner36 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner36.perform(click());

        DataInteraction checkedTextView36 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(11);
        checkedTextView36.perform(click());

        ViewInteraction spinner37 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner37.perform(click());

        DataInteraction checkedTextView37 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(12);
        checkedTextView37.perform(click());

        ViewInteraction spinner38 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner38.perform(click());

        DataInteraction checkedTextView38 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(1);
        checkedTextView38.perform(click());

        ViewInteraction spinner39 = onView(
                allOf(withId(R.id.spnPortoArrivo),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        spinner39.perform(click());

        DataInteraction checkedTextView39 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(0);
        checkedTextView39.perform(click());

        ViewInteraction button22 = onView(
                allOf(withId(R.id.btnConfermaOSmentisci), withText("<<"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout4),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                0),
                        isDisplayed()));
        button22.perform(click());

        ViewInteraction button23 = onView(
                allOf(withId(R.id.button2), withText("<"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout4),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                1),
                        isDisplayed()));
        button23.perform(click());

        ViewInteraction button24 = onView(
                allOf(withId(R.id.button3), withText(">"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout4),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                3),
                        isDisplayed()));
        button24.perform(click());

        ViewInteraction button25 = onView(
                allOf(withId(R.id.button4), withText(">>"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout4),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                4),
                        isDisplayed()));
        button25.perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction textView6 = onView(
                allOf(withId(android.R.id.title), withText("Aggiorna dati meteo"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.android.internal.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        textView6.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction button26 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                2),
                        isDisplayed()));
        button26.perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction textView7 = onView(
                allOf(withId(android.R.id.title), withText("Info"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.android.internal.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        textView7.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction button27 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                2),
                        isDisplayed()));
        button27.perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction textView8 = onView(
                allOf(withId(android.R.id.title), withText("Aggiorna orari da Web"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.android.internal.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        textView8.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction textView9 = onView(
                allOf(withId(android.R.id.title), withText("Esci"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.android.internal.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        textView9.perform(click());

    }
}
