package com.porfirio.orariprocida2011.threads;

import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadAlertsHandler {
    private final OrariProcida2011Activity activity;
    private final ExecutorService executor;
    private final Handler handler;

    public ReadAlertsHandler(OrariProcida2011Activity orariProcida2011Activity) {
        this.activity = orariProcida2011Activity;
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void start() {
        trackEvent("App Event", "Leggi Segnalazioni Task");

        executor.execute(() -> {
            final String urlS = "http://unoprocidaresidente.altervista.org/segnalazioni.csv";
            HttpURLConnection connS = null;
            InputStream inS;
            try {
                connS = (HttpURLConnection) new URL(urlS).openConnection();
            } catch (IOException e) {
                Log.d("ReadAlertsHandler", "Error while trying to connect: ", e);
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

                    for (Mezzo transportItem : activity.transportList) {
                        if (activity.sameTransport(date, transport, transportItem, activity.c)) {
                            transportItem.addReason(reason);
                        }
                    }
                }
                reader.close();
                Log.d("ReadAlertsHandler", "Successfully read and processed alerts.");

                handler.post(() -> {
                    activity.aggiornaLista();
                    Log.d("ORARI", "Lista segnalazioni aggiornata su GUI.");
                });
            } catch (IOException e) {
                Log.e("ReadAlertsHandler", "Error reading alerts from the input stream", e);
            } catch (Exception e) {
                Log.e("ReadAlertsHandler", "An unexpected error occurred while processing alerts", e);
            }

            trackEvent("App Event", "Terminated Leggi Segnalazioni Task");
        });
    }

    private void trackEvent(String category, String action) {
        activity.mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
    }
}
