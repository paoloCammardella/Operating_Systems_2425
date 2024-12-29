package com.porfirio.orariprocida2011.tasks;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Porfirio on 16/02/2018.
 */

public class ScriviSegnalazioneTask {

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    // Starts the execution of the task
    public void execute(String urlS) {
        new Thread(() -> {
            boolean result = doInBackground(urlS);

            // Update the UI with the result
            uiHandler.post(() -> onPostExecute(result));
        }).start();
    }

    // Do the long-running work here
    private boolean doInBackground(String urlS) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlS).openConnection();
            conn.setRequestMethod("GET");

            // read the response
            int responseCode = conn.getResponseCode();
            Log.d("ORARI", Integer.toString(responseCode));

            conn.disconnect();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // This is called when doInBackground() is finished
    private void onPostExecute(boolean result) {
        if (result) {
            Log.d("ORARI", "Scritta segnalazione su web");
            Log.d("ORARI", "Terminato aggiornamento orari su GUI");
        }
    }
}