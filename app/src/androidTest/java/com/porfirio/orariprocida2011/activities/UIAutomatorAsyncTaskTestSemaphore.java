package com.porfirio.orariprocida2011.activities;


import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
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
        // SEQUENZA UI->TASK1->TASK2
        // 0 - Avvio l'app (che carica la UI e tenta di iniziare i task meteo e download, che vengono subito bloccati)
        // 1- Avvio il task meteo dopo 2 sec
        // 2 - Termino il task meteo dopo altri 1 sec
        // 3- Avvio il task download dopo 2 altro sec
        // 4 - Termino il task download dopo altri 1 sec
        //ESITO NEGATIVO: CONCURRENCY BUG
        // Il problema è causato dalla UI che non immagina che i task siano pending al momento di visualizzare
        // un toast che ne evidenzia lo stato
        // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.util.Calendar.get(int)' on a null object reference
        // BUG: causa del bug è l'errata migrazione da sincrono verso asincrono, nella quale il toast è eseguito
        // dalla UI senza sincronizzarsi sulla terminazione dei task
        // COMMENTI: Il comportamento di questo test è analogo a PrimoTest_2

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
    public void PrimoTest_2() throws InterruptedException {
        // SEQUENZA UI -> TASK METEO
        // 0 - Avvio l'app (che carica la UI e tenta di iniziare i task meteo e download, ma il solo task meteo viene bloccato)
        // 1- Avvio il task meteo dopo 2 sec
        // 2 - Termino il task meteo dopo altri 1 sec
        //ESITO NEGATIVO: CONCURRENCY BUG
        // Il problema è causato dalla UI che non immagina che il task meteo possa essere pending al momento di visualizzare
        // un toast che ne evidenzia lo stato
        // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.util.Calendar.get(int)' on a null object reference
        // BUG: causa del bug è l'errata migrazione da sincrono verso asincrono, nella quale il toast è eseguito
        // dalla UI senza sincronizzarsi sulla terminazione dei task


        // Definisco i semafori, uno per ogni task, eventualmente settando il numero di task possibili
        DownloadMezziTask.taskDownload = new Semaphore(1);
        DownloadMezziTask.taskDownloadStart = new Semaphore(1);
        LeggiMeteoTask.taskMeteo = new Semaphore(1);
        LeggiMeteoTask.taskMeteoStart = new Semaphore(1);


        //Il test mette rosso i semafori, in modo da poterne determinare autonomamente lo sblocco
        Log.d("TEST", "Il test prova ad acquisire i semafori");
        //DownloadMezziTask.taskDownload.acquire();
        LeggiMeteoTask.taskMeteo.acquire();
        //DownloadMezziTask.taskDownloadStart.acquire();
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

    }

    @Test
    public void PrimoTest_3() throws InterruptedException {
        // SEQUENZA UI -> TASK DOWNLOAD
        // 0 - Avvio l'app (che carica la UI e tenta di iniziare i task meteo e download, ma il solo task download viene bloccati)
        // 1- Avvio il task download dopo 2 altro sec
        // 2 - Termino il task download dopo altri 1 sec
        //ESITO NEGATIVO: CONCURRENCY BUG
        // Il problema è causato dalla UI che non immagina che i task siano pending al momento di visualizzare
        // un toast che ne evidenzia lo stato
        // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.util.Calendar.get(int)' on a null object reference
        // BUG: causa del bug è l'errata migrazione da sincrono verso asincrono, nella quale il toast è eseguito
        // dalla UI senza sincronizzarsi sulla terminazione dei task
        // COMMENTI: il test analogo riguardante il task download non causa crash semplicemente perchè gli orari
        // su disco o cablati nel codice rappresentano un valore di default sempre disponibile. E' possibile, però
        // che il toast visualizzai un orario che poi verrà aggiornato

        // Definisco i semafori, uno per ogni task, eventualmente settando il numero di task possibili
        DownloadMezziTask.taskDownload = new Semaphore(1);
        DownloadMezziTask.taskDownloadStart = new Semaphore(1);
        LeggiMeteoTask.taskMeteo = new Semaphore(1);
        LeggiMeteoTask.taskMeteoStart = new Semaphore(1);


        //Il test mette rosso i semafori, in modo da poterne determinare autonomamente lo sblocco
        Log.d("TEST", "Il test prova ad acquisire i semafori");
        DownloadMezziTask.taskDownload.acquire();
        //LeggiMeteoTask.taskMeteo.acquire();
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
        // SEQUENZA (UI | METEO START) ->TERMINAZIONE METEO -> DOWNLOAD
        // 0 - Avvio l'app (che carica la UI e tenta di iniziare i task meteo e download, bloccando solo download e la terminazione di meteo)
        // 1- Avvio il task meteo subito
        // 2 - Termino il task meteo dopo altri 1 sec
        // 3- Avvio il task download dopo 2 altro sec
        // 4 - Termino il task download dopo altri 10 sec
        //ESITO POSITIVO
        //COMMENTI: Come nel test precedente, l'avvio ritardato di download non causa crash, ma causa
        //comunque un'anomalia (non riscontrabile) nel messaggio Toast, che fa riferimento al vecchio aggiornamento degli orari

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
        // SEQUENZA (UI | START METEO) -> TERMINA METEO -> AVVIO DONLOAD -> TERMINA DOWNLOAD -> UI -> DOWNLOAD
        // 0 - Avvio l'app (che carica la UI e tenta di iniziare i task meteo e download, bloccando solo download e la terminazione di meteo)
        // 1- Avvio il task meteo subito
        // 2 - Termino il task meteo dopo altri 1 sec
        // 3- Avvio il task download dopo 2 altro sec
        // 4 - Termino il task download dopo altri 1 sec
        // 5 - Clicco su aggiorna orari web
        // 6- Avvio il task download dopo 2 altro sec
        // 7 - Termino il task download dopo altri 10 sec
        //ESITO POSITIVO
        //COMMENTI: L'evento sulla UI avviene a task terminati, quindi in uno stato sostanzialmente coerente

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

        Thread.sleep(10000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download");
        DownloadMezziTask.taskDownload.release();


    }

    @Test
    public void QuartoTest() throws InterruptedException {
        // SEQUENZA (UI | METEO START) -> TERMINA METEO -> START DOWNLOAD -> UI -> START DOWNLOAD 2 -> FINE DOWNLOAD -> FINE DOWNLOAD 2
        // 0 - Avvio l'app (che carica la UI e tenta di iniziare i task meteo e download, bloccando solo download e la terminazione di meteo)
        // 1- Avvio il task meteo subito
        // 2 - Termino il task meteo dopo altri 1 sec
        // 3- Avvio il task download dopo 2 altro sec
        // 4 - Clicco su aggiorna orari web  QUI IMPAZZISCE ESPRESSO?
        // 5 - Avvio l'altro task download
        // 5 - Termino il task download dopo altri 10 sec
        // PROBLEMA: ESPRESSO previene l'esecuzione di un evento UI in presenza di task pendenti
        // Come possibile effetto collaterale viene rilevato un TIMEOUT nel task
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
    public void QuartoTest_2() throws InterruptedException, UiObjectNotFoundException, RemoteException {
        // SEQUENZA (UI | METEO START) -> TERMINA METEO -> START DOWNLOAD -> UI -> START DOWNLOAD 2 -> FINE DOWNLOAD -> FINE DOWNLOAD 2
        // 0 - Avvio l'app (che carica la UI e tenta di iniziare i task meteo e download, bloccando solo download e la terminazione di meteo)
        // 1- Avvio il task meteo subito
        // 2 - Termino il task meteo dopo altri 1 sec
        // 3- Avvio il task download dopo 2 altro sec
        // 4 - Clicco su aggiorna orari web  ORA UTILIZZO UIAUTOMATOR, CHE FUNZIONA ANCHE IN QUESTE CONDIZIONI
        // 5 - Avvio l'altro task download
        // 6 - Termino il task download dopo altri 3 sec
        // 7 - Termino il task download dopo altri 3 sec
        // PROBLEMA: l'impressione è che l'app si chiuda senza attendere il completamento del task (per esaurimento del test?)

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
                LAUNCH_TIMEOUT * 1000);
        Log.d("TEST", "TEST: Fine before");
        //La app è stata avviata


        Thread.sleep(1000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task meteo");
        LeggiMeteoTask.taskMeteo.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo meteo");

        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download 1");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");
        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());

        Thread.sleep(1000);
        Log.d("TEST", "Click su aggiorna orari da web");
        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());

        mDevice.pressMenu();

        UiObject ui = mDevice.findObject(new UiSelector().text("Aggiorna orari da Web"));
        ui.click();

        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());


        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download 2");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");

        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());
        Thread.sleep(3000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download 1");
        DownloadMezziTask.taskDownload.release();

        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());
        Thread.sleep(3000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download 2");
        DownloadMezziTask.taskDownload.release();

        //LeggiMeteoTask.taskMeteoStart.acquire();
        Log.d("TEST", "TEST: Fine del test");
    }

    @Test
    public void QuintoTest() throws InterruptedException, UiObjectNotFoundException, RemoteException {
        // SEQUENZA (UI | METEO START) -> TERMINA METEO -> START DOWNLOAD -> UI -> START DOWNLOAD 2 -> FINE DOWNLOAD -> FINE DOWNLOAD 2
        // 0 - Avvio l'app (che carica la UI e tenta di iniziare i task meteo e download, bloccando solo download e la terminazione di meteo)
        // 1- Avvio il task meteo subito
        // 2 - Termino il task meteo dopo altri 1 sec
        // 3- Avvio il task download dopo 2 altro sec
        // 4 - Clicco su aggiorna orari web  ORA UTILIZZO UIAUTOMATOR, CHE FUNZIONA ANCHE IN QUESTE CONDIZIONI
        // 5 - Avvio l'altro task download
        // 6 - Termino il task download dopo altri 10 sec
        // 7 - Termino il task download dopo altri 10 sec
        // PROBLEMA: l'impressione è che l'app si chiuda senza attendere il completamento del task (per esaurimento del test?)

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
                LAUNCH_TIMEOUT * 1000);
        Log.d("TEST", "TEST: Fine before");
        //La app è stata avviata


        Thread.sleep(1000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task meteo");
        LeggiMeteoTask.taskMeteo.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo meteo");

        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download 1");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");
        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());

        Thread.sleep(1000);
        Log.d("TEST", "Click su aggiorna orari da web");
        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());

        mDevice.pressHome();
        Thread.sleep(2000);
        mDevice.pressRecentApps();
        Thread.sleep(1000);
        mDevice.pressRecentApps();


        mDevice.pressMenu();

        UiObject ui = mDevice.findObject(new UiSelector().text("Aggiorna orari da Web"));
        ui.click();

        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());


        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download (1)");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");

        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());
        Thread.sleep(3000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download (2)");
        DownloadMezziTask.taskDownload.release();

        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());
        Thread.sleep(3000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download (3)");
        DownloadMezziTask.taskDownload.release();

        //LeggiMeteoTask.taskMeteoStart.acquire();
        Log.d("TEST", "TEST: Fine del test");
    }

    @Test
    public void SestoTest() throws InterruptedException, UiObjectNotFoundException, RemoteException {
        // SEQUENZA (UI | START DOWNLOAD) -> UI -> START DOWNLOAD2 -> UI -> START DOWNLOAD 3 -> FINE DOWNLOAD 1 -> FINE DOWNLOAD 2 -> FINE DOWNLOAD 3
        // 0 - Avvio l'app (che carica la UI e tenta di iniziare i task meteo e download, bloccando solo download e la terminazione di meteo)
        // 3- Avvio il task download subito
        // 4 - Clicco su aggiorna orari web  ORA UTILIZZO UIAUTOMATOR, CHE FUNZIONA ANCHE IN QUESTE CONDIZIONI
        // 5 - Avvio l'altro task download
        // 4 - Clicco su aggiorna orari web  ORA UTILIZZO UIAUTOMATOR, CHE FUNZIONA ANCHE IN QUESTE CONDIZIONI
        // 5 - Avvio l'altro task download
        // 6 - Termino il task download dopo altri 10 sec
        // 7 - Termino il task download dopo altri 10 sec
        // 7 - Termino il task download dopo altri 10 sec
        // ESITO: nessun problema
        // COMMENTI: l'esecuzione di oggetti task diversi corrispondenti alla stessa classe task è stata parallelizzata
        // Non sono stati riscontrati problemi relativi a corse critiche anche se potenzialmente potevano esistere
        // PROBLEMA : Con il sistema del singolo semaforo non posso decidere l'ordine di sblocco dei task
        // Se l'applicazione fosse stata programmata in maniera peggiore utilizzando più volte lo stesso oggetto task
        // ci sarebbero stati problemi (ma sarebbero stati rivelati anche da semplici test funzionali?)


        // Definisco i semafori, uno per ogni task, eventualmente settando il numero di task possibili
        DownloadMezziTask.taskDownload = new Semaphore(1);
        DownloadMezziTask.taskDownloadStart = new Semaphore(1);
        LeggiMeteoTask.taskMeteo = new Semaphore(1);
        LeggiMeteoTask.taskMeteoStart = new Semaphore(1);


        //Il test mette rosso i semafori, in modo da poterne determinare autonomamente lo sblocco
        Log.d("TEST", "Il test prova ad acquisire i semafori");
        DownloadMezziTask.taskDownload.acquire();
        //LeggiMeteoTask.taskMeteo.acquire();
        //DownloadMezziTask.taskDownloadStart.acquire();
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
                LAUNCH_TIMEOUT * 1000);
        Log.d("TEST", "TEST: Fine before");
        //La app è stata avviata

        Thread.sleep(1000);
        Log.d("TEST", "Click su aggiorna orari da web");
        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());

        mDevice.pressMenu();
        mDevice.findObject(new UiSelector().text("Aggiorna orari da Web")).click();

        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());
        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download 2");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");

        Thread.sleep(1000);
        Log.d("TEST", "Click su aggiorna orari da web");
        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());

        mDevice.pressMenu();
        mDevice.findObject(new UiSelector().text("Aggiorna orari da Web")).click();

        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());
        Thread.sleep(2000);
        Log.d("TEST", "TEST: Il test sblocca l'avvio del task download 2");
        DownloadMezziTask.taskDownloadStart.release();
        //Log.d("TEST", "TEST: Rilasciato il semaforo download");

        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());
        Thread.sleep(3000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download 1");
        DownloadMezziTask.taskDownload.release();

        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());
        Thread.sleep(3000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download 2");
        DownloadMezziTask.taskDownload.release();

        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());
        Thread.sleep(3000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download 2");
        DownloadMezziTask.taskDownload.release();

        Log.d("TEST", "TEST: Fine del test");
    }

    @Test
    public void SettimoTest() throws InterruptedException, UiObjectNotFoundException, RemoteException {
        // SEQUENZA (UI | START DOWNLOAD ) -> PAUSE -> RESUME -> FINE DOWNLOAD
        // 0 - Avvio l'app (che carica la UI e tenta di iniziare i task meteo e download, bloccando solo download e la terminazione di meteo)
        // pause
        // resume
        // 6 - Termino il task download dopo altri 10 sec
        // 7 - Termino il task download dopo altri 10 sec
        // PROBLEMA: l'impressione è che l'app si chiuda senza attendere il completamento del task (per esaurimento del test?)

        // Definisco i semafori, uno per ogni task, eventualmente settando il numero di task possibili
        DownloadMezziTask.taskDownload = new Semaphore(1);
        DownloadMezziTask.taskDownloadStart = new Semaphore(1);
        LeggiMeteoTask.taskMeteo = new Semaphore(1);
        LeggiMeteoTask.taskMeteoStart = new Semaphore(1);


        //Il test mette rosso i semafori, in modo da poterne determinare autonomamente lo sblocco
        Log.d("TEST", "Il test prova ad acquisire i semafori");
        DownloadMezziTask.taskDownload.acquire();
        //LeggiMeteoTask.taskMeteo.acquire();
        //DownloadMezziTask.taskDownloadStart.acquire();
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
                LAUNCH_TIMEOUT * 1000);
        Log.d("TEST", "TEST: Fine before");
        //La app è stata avviata

        // PERCHE' IL TEST FINISCE QUI?
        Thread.sleep(10000);


        mDevice.pressHome();
        Log.d("TEST", "TEST: Pause app");
        Thread.sleep(2000);
        mDevice.pressRecentApps();
        Thread.sleep(1000);
        mDevice.pressRecentApps();
        Log.d("TEST", "TEST: Resume app");


        Log.d("TEST", "TEST: valori dei semafori: StartDownload=" + DownloadMezziTask.taskDownloadStart.availablePermits() + " download=" + DownloadMezziTask.taskDownload.availablePermits());
        Thread.sleep(3000);
        Log.d("TEST", "TEST: Il test sblocca la terminazione del task download ");
        DownloadMezziTask.taskDownload.release();


        //LeggiMeteoTask.taskMeteoStart.acquire();
        Log.d("TEST", "TEST: Fine del test");
    }


}
