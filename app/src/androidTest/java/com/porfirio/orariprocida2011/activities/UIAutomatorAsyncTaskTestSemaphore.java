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


        //QUI SETTO IL NUMERO DEL TEST
        //TestSuiteAS.testNumber++;

    }

    @Test
    public void PrimoTest() throws InterruptedException {
        // 1- Avvio il task meteo dopo 2 sec
        // 2 - Termino il task meteo dopo altri 1 sec
        // 3- Avvio il task download dopo 2 altro sec
        // 4 - Termino il task download dopo altri 1 sec
        //ESITO NEGATIVO: CONCURRENCY BUGS

        // Definisco i semafori, uno per ogni task, eventualmente settando il numero di task possibili
        DownloadMezziTask.taskDownload = new Semaphore(1);
        DownloadMezziTask.taskDownloadStart = new Semaphore(1);
        LeggiMeteoTask.taskMeteo = new Semaphore(1);
        LeggiMeteoTask.taskMeteoStart = new Semaphore(1);


        //Il test mette rosso i semafori, in modo da poterne determinare autonomamente lo sblocco
        Log.d("TEST", "Il test prova ad acquisire i semafori");
        DownloadMezziTask.taskDownload.acquire();
        LeggiMeteoTask.taskMeteo.acquire();
        DownloadMezziTask.taskDownloadStart.acquire();
        LeggiMeteoTask.taskMeteoStart.acquire();

        Log.d("TEST", "Inizia il test, valori dei semafori: Download=" + DownloadMezziTask.taskDownload.availablePermits() + " meteo=" + LeggiMeteoTask.taskMeteo.availablePermits());

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

        Log.d("TEST", "TEST: Avvio la activity");
        //Log.d("TEST","TEST: valori dei semafori: Download="+DownloadMezziTask.taskDownload.availablePermits()+" meteo="+LeggiMeteoTask.taskMeteo.availablePermits());
        //Thread.sleep(5000);
        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT * 100);
        Log.d("TEST", "TEST: Fine before");
        //La app è stata avviata


        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task meteo");
        LeggiMeteoTask.taskMeteoStart.release();
        Log.d("TEST", "TEST: Rilasciato il semaforo meteo");

        Thread.sleep(1000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task meteo");
        LeggiMeteoTask.taskMeteo.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo meteo");

        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");

        Thread.sleep(1000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download");
        DownloadMezziTask.taskDownload.release();

    }

    @Test
    public void SecondoTest() throws InterruptedException {
        // 1- Avvio il task meteo subito
        // 2 - Termino il task meteo dopo altri 1 sec
        // 3- Avvio il task download dopo 2 altro sec
        // 4 - Termino il task download dopo altri 10 sec
        //ESITO POSITIVO

        // Definisco i semafori, uno per ogni task, eventualmente settando il numero di task possibili
        DownloadMezziTask.taskDownload = new Semaphore(1);
        DownloadMezziTask.taskDownloadStart = new Semaphore(1);
        LeggiMeteoTask.taskMeteo = new Semaphore(1);
        LeggiMeteoTask.taskMeteoStart = new Semaphore(1);


        //Il test mette rosso i semafori, in modo da poterne determinare autonomamente lo sblocco
        Log.d("TEST", "Il test prova ad acquisire i semafori");
        DownloadMezziTask.taskDownload.acquire();
        LeggiMeteoTask.taskMeteo.acquire();
        DownloadMezziTask.taskDownloadStart.acquire();
        //LeggiMeteoTask.taskMeteoStart.acquire();

        Log.d("TEST", "Inizia il test, valori dei semafori: Download=" + DownloadMezziTask.taskDownload.availablePermits() + " meteo=" + LeggiMeteoTask.taskMeteo.availablePermits());


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

        Log.d("TEST", "TEST: Avvio la activity");
        //Log.d("TEST","TEST: valori dei semafori: Download="+DownloadMezziTask.taskDownload.availablePermits()+" meteo="+LeggiMeteoTask.taskMeteo.availablePermits());
        //Thread.sleep(5000);
        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT * 100);
        Log.d("TEST", "TEST: Fine before");
        //La app è stata avviata



        Thread.sleep(1000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task meteo");
        LeggiMeteoTask.taskMeteo.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo meteo");

        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");

        Thread.sleep(10000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download");
        DownloadMezziTask.taskDownload.release();

    }

    @Test
    public void TerzoTest() throws InterruptedException {
        // 1- Avvio il task meteo subito
        // 2 - Termino il task meteo dopo altri 1 sec
        // 3- Avvio il task download dopo 2 altro sec
        // 4 - Termino il task download dopo altri 10 sec
        // 5 - Clicco su aggiorna orari web
        //ESITO POSITIVO

        // Definisco i semafori, uno per ogni task, eventualmente settando il numero di task possibili
        DownloadMezziTask.taskDownload = new Semaphore(1);
        DownloadMezziTask.taskDownloadStart = new Semaphore(1);
        LeggiMeteoTask.taskMeteo = new Semaphore(1);
        LeggiMeteoTask.taskMeteoStart = new Semaphore(1);


        //Il test mette rosso i semafori, in modo da poterne determinare autonomamente lo sblocco
        Log.d("TEST", "Il test prova ad acquisire i semafori");
        DownloadMezziTask.taskDownload.acquire();
        LeggiMeteoTask.taskMeteo.acquire();
        DownloadMezziTask.taskDownloadStart.acquire();
        //LeggiMeteoTask.taskMeteoStart.acquire();

        Log.d("TEST", "Inizia il test, valori dei semafori: Download=" + DownloadMezziTask.taskDownload.availablePermits() + " meteo=" + LeggiMeteoTask.taskMeteo.availablePermits());


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

        Log.d("TEST", "TEST: Avvio la activity");
        //Log.d("TEST","TEST: valori dei semafori: Download="+DownloadMezziTask.taskDownload.availablePermits()+" meteo="+LeggiMeteoTask.taskMeteo.availablePermits());
        //Thread.sleep(5000);
        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT * 100);
        Log.d("TEST", "TEST: Fine before");
        //La app è stata avviata


        Thread.sleep(1000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task meteo");
        LeggiMeteoTask.taskMeteo.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo meteo");

        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");

        Thread.sleep(10000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download");
        DownloadMezziTask.taskDownload.release();

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

        Log.d("TEST", "Click su aggiorna orari da web");

        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");

        Thread.sleep(10000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download");
        DownloadMezziTask.taskDownload.release();


    }

    @Test
    public void QuartoTest() throws InterruptedException {
        // 1- Avvio il task meteo subito
        // 2 - Termino il task meteo dopo altri 1 sec
        // 3- Avvio il task download dopo 2 altro sec
        // 4 - Clicco su aggiorna orari web  QUI IMPAZZISCE ESPRESSO?
        // 5 - Avvio l'altro task download
        // 5 - Termino il task download dopo altri 10 sec
        // 6 - Termino il task download dopo altri 10 sec
        //ESITO TIMEOUT DEL TASK DOWNLOAD
        //SEMBRA CHE L'AVVIO DI DUE TASK DOWNLOAD CAUSI UN PROBLEMA: VERIFICARE SE E' REALE O SOLO DEL TEST
        //ATTENZIONE: onView di Android Espresso NON può essere eseguita se ne non sono terminati i task
        // https://developer.android.com/training/testing/espresso/

        // Definisco i semafori, uno per ogni task, eventualmente settando il numero di task possibili
        DownloadMezziTask.taskDownload = new Semaphore(2);
        DownloadMezziTask.taskDownloadStart = new Semaphore(2);
        LeggiMeteoTask.taskMeteo = new Semaphore(1);
        LeggiMeteoTask.taskMeteoStart = new Semaphore(1);


        //Il test mette rosso i semafori, in modo da poterne determinare autonomamente lo sblocco
        Log.d("TEST", "Il test prova ad acquisire i semafori");
        DownloadMezziTask.taskDownload.acquire(2);
        LeggiMeteoTask.taskMeteo.acquire();
        DownloadMezziTask.taskDownloadStart.acquire(2);
        //LeggiMeteoTask.taskMeteoStart.acquire();

        Log.d("TEST", "Inizia il test, valori dei semafori: Download=" + DownloadMezziTask.taskDownload.availablePermits() + " meteo=" + LeggiMeteoTask.taskMeteo.availablePermits());


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

        Log.d("TEST", "TEST: Avvio la activity");
        //Log.d("TEST","TEST: valori dei semafori: Download="+DownloadMezziTask.taskDownload.availablePermits()+" meteo="+LeggiMeteoTask.taskMeteo.availablePermits());
        //Thread.sleep(5000);
        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT * 100);
        Log.d("TEST", "TEST: Fine before");
        //La app è stata avviata


        Thread.sleep(1000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task meteo");
        LeggiMeteoTask.taskMeteo.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo meteo");

        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");
        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());

        Thread.sleep(1000);
        Log.d("TEST", "Click su aggiorna orari da web");
        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());

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
        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());


        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");

        Thread.sleep(3000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download");
        DownloadMezziTask.taskDownload.release();

        Thread.sleep(3000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download");
        DownloadMezziTask.taskDownload.release();


    }

    @Test
    public void QuintoTest() throws InterruptedException {
        // 1- Avvio il task meteo subito
        // 2 - Termino il task meteo dopo altri 1 sec
        // 3- Avvio il task download dopo 2 altro sec
        // 4 - Termino il task download dopo altri 10 sec
        // 5 - Clicco su aggiorna orari web
        //ESITO POSITIVO

        // Definisco i semafori, uno per ogni task, eventualmente settando il numero di task possibili
        DownloadMezziTask.taskDownload = new Semaphore(1);
        DownloadMezziTask.taskDownloadStart = new Semaphore(1);
        LeggiMeteoTask.taskMeteo = new Semaphore(1);
        LeggiMeteoTask.taskMeteoStart = new Semaphore(1);


        //Il test mette rosso i semafori, in modo da poterne determinare autonomamente lo sblocco
        Log.d("TEST", "Il test prova ad acquisire i semafori");
        DownloadMezziTask.taskDownload.acquire();
        LeggiMeteoTask.taskMeteo.acquire();
        DownloadMezziTask.taskDownloadStart.acquire();
        //LeggiMeteoTask.taskMeteoStart.acquire();


        Log.d("TEST", "Inizia il test, valori dei semafori: Download=" + DownloadMezziTask.taskDownload.availablePermits() + " meteo=" + LeggiMeteoTask.taskMeteo.availablePermits());

        // Il task meteo si avvia subito
        //LeggiMeteoTask.taskMeteoStart.release();

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

        Log.d("TEST", "TEST: Avvio la activity");
        //Log.d("TEST","TEST: valori dei semafori: Download="+DownloadMezziTask.taskDownload.availablePermits()+" meteo="+LeggiMeteoTask.taskMeteo.availablePermits());
        //Thread.sleep(5000);
        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT * 100);
        Log.d("TEST", "TEST: Fine before");
        //La app è stata avviata


        Thread.sleep(1000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task meteo");
        LeggiMeteoTask.taskMeteo.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo meteo");

        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");

        Thread.sleep(1000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download");
        DownloadMezziTask.taskDownload.release();

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

        Log.d("TEST", "Click su aggiorna orari da web");

        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");

        Thread.sleep(1000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download");
        DownloadMezziTask.taskDownload.release();

        //InstrumentationRegistry.getInstrumentation().callActivityOnPause(context.);


    }


    @Test
    public void STerzoTest() throws InterruptedException {
        // 1 - Termina il task meteo dopo 2 sec
        // 2 - Click su Aggiorna Orari da Web
        // 3- Termina il task download dopo 10 sec
        // 4 - Termina il task download dopo 2 sec
        //ESITO: TIMEOUT IN TASK DOWNLOAD

        //L'avvio dell'app ha comportato anche l'avvio dei tre task
        //dopo un tempo di attesa termina il task download

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

        Log.d("TEST", "Click su aggiorna orari da web");

        Thread.sleep(2000);
        Log.d("TEST", "Il test considera finito il task e rilascia il semaforo");
        taskDownload.release();
        Log.d("TEST", "Rilasciato il semaforo download");

        Thread.sleep(5000);
        Log.d("TEST", "Il test considera finito il task e rilascia il semaforo download");
        DownloadMezziTask.taskDownload.release();
        Log.d("TEST", "Rilasciato il semaforo download");

    }
}
