package com.porfirio.orariprocida2011.activities;


import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.porfirio.orariprocida2011.tasks.DownloadMezziTask;
import com.porfirio.orariprocida2011.tasks.LeggiMeteoTask;
import com.porfirio.orariprocida2011.tasks.LeggiSegnalazioniTask;
import com.porfirio.orariprocida2011.test.TestSuiteAS;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class UIAutomatorAsyncTaskTest {

    private static final String BASIC_SAMPLE_PACKAGE
            = "com.porfirio.orariprocida2011";
    private static final int LAUNCH_TIMEOUT = 5000;
    private static final String STRING_TO_BE_TYPED = "UiAutomator";
    private UiDevice mDevice;

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

    @BeforeClass
    public static void setTestValues() {
        //TODO Leggere i valori dei delay da file
        TestSuiteAS.addTest(0);
        TestSuiteAS.setDelay(DownloadMezziTask.class.toString(), 10000);
        TestSuiteAS.setDelay(LeggiSegnalazioniTask.class.toString(), 2000);
        TestSuiteAS.setDelay(LeggiMeteoTask.class.toString(), 3000);
        TestSuiteAS.addTest(1);
        TestSuiteAS.setDelay(DownloadMezziTask.class.toString(), 10000);
        TestSuiteAS.setDelay(LeggiSegnalazioniTask.class.toString(), 2000);
        TestSuiteAS.setDelay(LeggiMeteoTask.class.toString(), 1000);
        TestSuiteAS.testNumber = -1;
        return;
    }


    @Before
    public void startMainActivityFromHomeScreen() {
/*        TestSuiteAS.addTest(0);
        TestSuiteAS.setDelay(DownloadMezziTask.class.toString(),1000);
        TestSuiteAS.setDelay(LeggiSegnalazioniTask.class.toString(),2000);
        TestSuiteAS.setDelay(LeggiMeteoTask.class.toString(),3000);
        TestSuiteAS.testNumber=-1;*/
        //QUI SETTO IL NUMERO DEL TEST
        TestSuiteAS.testNumber++;

        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();

        // Wait for launcher
        final String launcherPackage = mDevice.getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)),
                LAUNCH_TIMEOUT);

        // Launch the app
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
        // Clear out any previous instances
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT);

    }

    @Test
    public void IllegalStateExceptionTest() {

        //TODO: Dovrei prima di tutto assicurarmi di stare eseguendo proprio OrariProcida2011Activity
        //TODO: Poi dovrei trovare il modo di iniettare gli opportuni rallentamenti negli AsyncTask
        //TODO: Infine mi potrebbero andare bene le attuali asserzioni


        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
/*        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction textView = onView(
                allOf(withId(android.R.id.title), withText("Aggiorna orari da Web"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.android.internal.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        textView.perform(click());

    }
}
