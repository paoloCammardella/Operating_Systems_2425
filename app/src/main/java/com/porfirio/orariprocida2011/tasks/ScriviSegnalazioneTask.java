package com.porfirio.orariprocida2011.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Porfirio on 16/02/2018.
 */

public class ScriviSegnalazioneTask extends AsyncTask<String, Integer, Boolean> {

    // Do the long-running work in here
    protected Boolean doInBackground(String... url) {
        String urlS = url[0];

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlS).openConnection();
            conn.setRequestMethod("GET");

// read the response
            int responseCode = conn.getResponseCode();
            Log.d("ORARI", Integer.toString(responseCode));

            conn.disconnect();
        } catch (Exception ex) {
            return false;
        }


        return true;
    }


    // This is called each time you call publishProgress()
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }


    // This is called when doInBackground() is finished
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.d("ORARI", "Scritta segnalazione su web");
            //act.aggiornaLista();
            Log.d("ORARI", "Terminato aggiornamento orari su GUI");
            //gli orari del web erano piu' aggiornati
            //bisogna aggiornare la GUI
        }
        //showNotification("Downloaded " + result + " bytes");
    }
}