package com.porfirio.orariprocida2011.threads;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.utils.Analytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DownloadTransportsHandler extends Handler {

    private final OrariProcida2011Activity act;
    private final ArrayList<Mezzo> transportList;

    // Firebase references
    private final DatabaseReference databaseReference;
    private final FirebaseAnalytics analytics;

    // Semaphore declarations
    public static Semaphore taskDownload;
    public static Semaphore taskDownloadStart;

    private Analytics internalAnalytics;

    public DownloadTransportsHandler(OrariProcida2011Activity orariProcida2011Activity) {
        act = orariProcida2011Activity;
        transportList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Transports");
        analytics = FirebaseAnalytics.getInstance(act.getApplicationContext());
    }

    public void setAnalytics(Analytics analytics) {
        this.internalAnalytics = analytics;
    }

    public void fetchTransports() {
        new Thread(() -> {
            handleSemaphore(taskDownloadStart, true);

            // Log evento Firebase Analytics: Inizio download
            analytics.logEvent("fetch_transports_started", null);
            internalAnalytics.send("App Event", "Download Mezzi Task");

            try {
                // Recupera dati da Realtime Database
                fetchTransportDataFromRealtimeDatabase();
            } finally {
                handleSemaphore(taskDownload, false);
            }

            // Log evento Firebase Analytics: Download terminato
            analytics.logEvent("fetch_transports_ended", null);
            internalAnalytics.send("App Event", "Download Terminated");
        }).start();
    }

    //TODO cambiare effettivamente i valori nel Firebase Realtime DB con quelli reali
    //TODO creare un backend per automatizzarre il riempimento del DB Firebase
    private void fetchTransportDataFromRealtimeDatabase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transportList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Mezzo mezzo = parseMezzo(snapshot);
                    transportList.add(mezzo);
                }

                updateGui();

                analytics.logEvent("transports_updated", null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DownloadHandler", "Errore Realtime Database: " + databaseError.getMessage(), databaseError.toException());

                Bundle bundle = new Bundle();
                bundle.putString("error_message", databaseError.getMessage());
                analytics.logEvent("fetch_transports_failed", bundle);
            }
        });
    }

    private Mezzo parseMezzo(DataSnapshot snapshot) {
        return new Mezzo(
                snapshot.child("nomeNave").getValue(String.class),         // Nome nave
                snapshot.child("oraPartenza").getValue(String.class),      // Ora partenza (ora)
                snapshot.child("minutiPartenza").getValue(String.class),   // Ora partenza (minuti)
                snapshot.child("oraArrivo").getValue(String.class),        // Ora arrivo (ora)
                snapshot.child("minutiArrivo").getValue(String.class),     // Ora arrivo (minuti)
                snapshot.child("portoPartenza").getValue(String.class),    // Porto di partenza
                snapshot.child("portoArrivo").getValue(String.class),      // Porto di arrivo
                snapshot.child("giorniInizioEsclusione").getValue(String.class), // Giorni inizio esclusione
                snapshot.child("meseInizioEsclusione").getValue(String.class),   // Mese inizio esclusione
                snapshot.child("annoInizioEsclusione").getValue(String.class),   // Anno inizio esclusione
                snapshot.child("giorniFineEsclusione").getValue(String.class),   // Giorni fine esclusione
                snapshot.child("meseFineEsclusione").getValue(String.class),     // Mese fine esclusione
                snapshot.child("annoFineEsclusione").getValue(String.class),     // Anno fine esclusione
                snapshot.child("giorniSettimana").getValue(String.class)         // Giorni della settimana
        );
    }

    private void updateGui() {
        act.updateWebTimes = Calendar.getInstance();
        act.ultimaLetturaOrariDaWeb = Calendar.getInstance();

        String str = String.format(Locale.getDefault(), "%s %d/%d/%d",
                act.getString(R.string.orariAggiornatiAl),
                act.updateWebTimes.get(Calendar.DAY_OF_MONTH),
                act.updateWebTimes.get(Calendar.MONTH) + 1,
                act.updateWebTimes.get(Calendar.YEAR)
        );

        act.transportList.clear();
        act.transportList.addAll(transportList);
        act.aggiornaLista();
        act.setMsgToast();

        Log.d("DownloadHandler", str);
        Log.d("DownloadHandler", "Transport data updated in the GUI.");
    }

    private void handleSemaphore(Semaphore semaphore, boolean acquire) {
        if (semaphore != null) {
            try {
                if (acquire) {
                    semaphore.acquire();
                } else if (!semaphore.tryAcquire(15L, TimeUnit.SECONDS)) {
                    Log.e("DownloadHandler", "Semaphore timeout.");
                    act.finish();
                }
            } catch (InterruptedException e) {
                Log.e("DownloadHandler", "Semaphore operation interrupted: " + e.getMessage(), e);
            } finally {
                semaphore.release();
            }
        }
    }
}
