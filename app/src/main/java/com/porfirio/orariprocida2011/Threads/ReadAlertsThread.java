package com.porfirio.orariprocida2011.Threads;

import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Mezzo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Porfirio on 16/02/2018.
 */

public class ReadAlertsThread extends Thread {
    private final OrariProcida2011Activity act;

    public ReadAlertsThread(OrariProcida2011Activity orariProcida2011Activity) {
        act = orariProcida2011Activity;
    }

    @Override
    public void run() {
        trackEvent("App Event", "Leggi Segnalazioni Task");
        final String urlS = "http://unoprocidaresidente.altervista.org/segnalazioni.csv";
        HttpURLConnection connS = null;
        InputStream inS;
        try {
            connS = (HttpURLConnection) new URL(urlS).openConnection();
        } catch (IOException e) {
            Log.d("ReadAlertsThread", "Error while trying to connect: ", e);
        }
        try {
            assert connS != null;
            inS = connS.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inS));
            String date;
            String transport;
            String reason;
            String details;

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                date = line;
                transport = reader.readLine();
                reason = reader.readLine();
                details = reader.readLine();

                for (Mezzo transportItem : act.transportList) {
                    if (act.sameTransport(date, transport, transportItem, act.c)) {
                        transportItem.addReason(reason);
                    }
                }
            }
            reader.close();
            Log.d("ReadAlertsThread", "Successfully read and processed alerts.");
        } catch (IOException e) {
            Log.e("ReadAlertsThread", "Error reading alerts from the input stream", e);
        } catch (Exception e) {
            Log.e("ReadAlertsThread", "An unexpected error occurred while processing alerts", e);
        }

        trackEvent("App Event", "Terminated Leggi Segnalazioni Task");

        Log.d("ORARI", "Lette segnalazioni da web");
        act.aggiornaLista();
        Log.d("ORARI", "Terminato aggiornamento orari su GUI");
    }

    private void trackEvent(String category, String action) {
        act.mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
    }

}