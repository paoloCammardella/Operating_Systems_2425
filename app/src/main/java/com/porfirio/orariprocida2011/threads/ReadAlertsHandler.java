package com.porfirio.orariprocida2011.threads;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.utils.Analytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadAlertsHandler {

    private static final String URL = "http://unoprocidaresidente.altervista.org/segnalazioni.csv";

    private final OrariProcida2011Activity activity;
    private final ExecutorService executor;
    private final Handler handler;

    private Analytics analytics;

    public ReadAlertsHandler(OrariProcida2011Activity orariProcida2011Activity) {
        this.activity = orariProcida2011Activity;
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void setAnalytics(Analytics analytics) {
        this.analytics = analytics;
    }

    public void start() {
        analytics.send("App Event", "Leggi Segnalazioni Task");

        executor.execute(() -> {
            HttpURLConnection connS;

            try {
                connS = (HttpURLConnection) new URL(URL).openConnection();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connS.getInputStream()))) {
                    for (String date = reader.readLine(); date != null; date = reader.readLine()) {
                        String transport = reader.readLine();
                        String reason = reader.readLine();
                        String details = reader.readLine();

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
            } catch (IOException e) {
                Log.d("ReadAlertsHandler", "Error while trying to connect: ", e);
            }

            analytics.send("App Event", "Terminated Leggi Segnalazioni Task");
        });
    }

}
