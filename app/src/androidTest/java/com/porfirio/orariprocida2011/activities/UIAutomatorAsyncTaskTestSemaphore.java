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
import android.util.Log;
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

import java.util.concurrent.Semaphore;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.porfirio.orariprocida2011.tasks.DownloadMezziTask.taskDownload;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class UIAutomatorAsyncTaskTestSemaphore {

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
    public void startMainActivityFromHomeScreen() throws InterruptedException {

        // Definisco i semafori, uno per ogni task, eventualmente settando il numero di task possibili
        DownloadMezziTask.taskDownload = new Semaphore(1, true);
        LeggiMeteoTask.taskMeteo = new Semaphore(1, true);


        //Il test mette rosso i semafori, in modo da poterne determinare autonomamente lo sblocco
        Log.d("TEST", "Il test prova ad acquisire i semafori");
        DownloadMezziTask.taskDownload.acquire();
        LeggiMeteoTask.taskMeteo.acquire();
        Log.d("TEST", "Inizia il test, acquisisce i semafori");

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

        Log.d("TEST", "Avvio la activity");
        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT * 100);

        //La app è stata avviata
    }

    @Test
    public void IllegalStateExceptionTest() throws InterruptedException {

        //L'avvio dell'app ha comportato anche l'avvio dei tre task

        //TODO: Il test:
        //TODO: 1)	 avvia l’operazione sul thread principale
        //TODO: 2)	scatena la terminazione dei task a tempi prefissati con release dopo Timer


        //dopo un tempo di attesa termina il task download

        Thread.sleep(10000);
        Log.d("TEST", "Il test considera finito il task e rilascia il semaforo download");
        DownloadMezziTask.taskDownload.release();
        Log.d("TEST", "Rilasciato il semaforo download");

        Thread.sleep(2000);
        Log.d("TEST", "Il test considera finito il task e rilascia il semaforo meteo");
        LeggiMeteoTask.taskMeteo.release();
        Log.d("TEST", "Rilasciato il semaforo meteo");


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

        Thread.sleep(2000);
        Log.d("TEST", "Il test considera finito il task e rilascia il semaforo");
        taskDownload.release();
        Log.d("TEST", "Rilasciato il semaforo");

        /*Thread.sleep(1000);
        taskMeteo.release();
        Log.d("TEST","Terminato meteo");

        Thread.sleep(5000);
        taskSegnalazioni.release();
        Log.d("TEST","Terminato segnalazioni");
*/
//        OrariProcida2011Activity currentActivity = (OrariProcida2011Activity) getInstrumentation().waitForMonitorWithTimeout(getInstrumentation().addMonitor("com.porfirio.OrariProcida2011Activity", null, false), 3000);
//        currentActivity.downloadMezziTask=null;

    }
}
