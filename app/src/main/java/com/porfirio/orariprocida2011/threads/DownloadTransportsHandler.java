package com.porfirio.orariprocida2011.threads;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.utils.Analytics;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;

// TODO: should be renamed to something like OnRequestTransportsDAO
public class DownloadTransportsHandler implements TransportsDAO {

    private final OrariProcida2011Activity act; // FIXME: references to activity should be removed

    private final MutableLiveData<TransportsUpdate> update;
    private ExecutorService executorService;

    // Firebase references
    private final DatabaseReference databaseReference;
    private final FirebaseAnalytics analytics;

    private Analytics internalAnalytics;

    public DownloadTransportsHandler(OrariProcida2011Activity orariProcida2011Activity, Analytics analytics, ExecutorService executorService) {
        act = orariProcida2011Activity;

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        databaseReference = FirebaseDatabase.getInstance().getReference("Transports");
        this.analytics = FirebaseAnalytics.getInstance(act.getApplicationContext()); // FIXME: should we need analytics?

        setAnalytics(analytics);
        setExecutorService(executorService);

        update = new MutableLiveData<>();
    }

    public void setAnalytics(Analytics analytics) {
        this.internalAnalytics = Objects.requireNonNull(analytics);
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = Objects.requireNonNull(executorService);
    }

    // TODO: context parameter should be removed, it's there for legacy code that should be removed in the future
    public void requestUpdate(Context context) {
        executorService.submit(() -> {
            // Log evento Firebase Analytics: Inizio download
            analytics.logEvent("fetch_transports_started", null);
            internalAnalytics.send("App Event", "Download Mezzi Task");

            fetchTransportDataFromRealtimeDatabase(context);

            // Log evento Firebase Analytics: Download terminato
            analytics.logEvent("fetch_transports_ended", null);
            internalAnalytics.send("App Event", "Download Terminated");
        });
    }

    @Override
    public LiveData<TransportsUpdate> getUpdates() {
        return update;
    }

    //TODO cambiare effettivamente i valori nel Firebase Realtime DB con quelli reali
    //TODO creare un backend per automatizzarre il riempimento del DB Firebase
    private void fetchTransportDataFromRealtimeDatabase(Context context) {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Mezzo> transportList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Mezzo mezzo = parseMezzo(snapshot);
                    transportList.add(mezzo);
                }

                update.postValue(new TransportsUpdate(transportList, LocalDateTime.now()));

                analytics.logEvent("transports_updated", null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DownloadHandler", "Errore Realtime Database: " + databaseError.getMessage(), databaseError.toException());

                Bundle bundle = new Bundle();
                bundle.putString("error_message", databaseError.getMessage());
                analytics.logEvent("fetch_transports_failed", bundle);

                postOfflineData(context);
            }

        });

    }

    private Mezzo parseMezzo(DataSnapshot snapshot) {
        return new Mezzo(
                snapshot.child("attribute1").getValue(String.class),  // Nome nave
                snapshot.child("attribute2").getValue(String.class),  // Ora partenza ora
                snapshot.child("attribute3").getValue(String.class),  // Ora partenza minuti
                snapshot.child("attribute4").getValue(String.class),  // Ora arrivo ora
                snapshot.child("attribute5").getValue(String.class),  // Ora arrivo minuti
                snapshot.child("attribute6").getValue(String.class),  // Porto partenza
                snapshot.child("attribute7").getValue(String.class),  // Porto arrivo
                snapshot.child("attribute8").getValue(String.class),  // Giorni inizio esclusione
                snapshot.child("attribute9").getValue(String.class),  // Mese inizio esclusione
                snapshot.child("attribute10").getValue(String.class), // Anno inizio esclusione
                snapshot.child("attribute11").getValue(String.class), // Giorni fine esclusione
                snapshot.child("attribute12").getValue(String.class), // Mese fine esclusione
                snapshot.child("attribute13").getValue(String.class), // Anno fine esclusione
                snapshot.child("attribute14").getValue(String.class)  // Giorni settimana
        );
    }

    private void postOfflineData(Context context) {

        // FIXME: this is legacy code that should be removed

        try {
            FileInputStream stream = new FileInputStream(context.getFilesDir().getPath() + "/orari.csv");
            postInternalStorageData(stream);
        } catch (FileNotFoundException e) {
            internalAnalytics.send("App Event", "Riempi Lista da Codice");

            ArrayList<Mezzo> transportList = new ArrayList<>();

            transportList.add(new Mezzo("Aliscafo Caremar", 8, 10, 8, 25, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 9, 10, 9, 45, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 12, 10, 12, 40, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 18, 35, 19, 0, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 10, 20, 10, 55, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 13, 50, 14, 20, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 19, 15, 19, 45, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 8, 55, 9, 15, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 7, 30, 8, 10, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 8, 50, 9, 30, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 11, 45, 12, 25, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 13, 10, 13, 50, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 15, 10, 15, 50, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 17, 30, 18, 10, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 18, 15, 18, 55, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            //listMezzi.add(new Mezzo(getApplicationContext(),"Traghetto Caremar",0,15,1,15,"Napoli Porta di Massa","Procida",0,0,0,0,0,0,"1234567",this));
            transportList.add(new Mezzo("Traghetto Caremar", 6, 25, 7, 25, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 9, 10, 10, 10, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 10, 45, 11, 45, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 15, 15, 16, 15, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 17, 45, 18, 45, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 19, 30, 20, 30, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 22, 15, 23, 15, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            //listMezzi.add(new Mezzo(getApplicationContext(),"Traghetto Caremar",2,20,3,20,"Procida","Napoli Porta di Massa",0,0,0,0,0,0,"1234567",this));
            transportList.add(new Mezzo("Traghetto Caremar", 7, 40, 8, 40, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 13, 35, 14, 35, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 14, 35, 15, 35, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 16, 15, 17, 15, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 18, 5, 19, 0, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 20, 30, 21, 30, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            //listMezzi.add(new Mezzo(getApplicationContext(),"Traghetto Caremar",22,55,23,55,"Procida","Napoli Porta di Massa",0,0,0,0,0,0,"1234567",this));
            transportList.add(new Mezzo("Aliscafo Caremar", 6, 35, 7, 15, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 7, 55, 8, 35, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 9, 25, 10, 5, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 10, 35, 11, 15, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 13, 30, 14, 10, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 14, 55, 15, 35, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 16, 55, 17, 35, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Gestur", 6, 50, 7, 30, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 9, 40, 10, 20, "Procida", "Pozzuoli", 19, 10, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Gestur", 11, 30, 12, 10, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 14, 5, 14, 45, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 17, 5, 17, 45, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 8, 25, 9, 5, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 10, 40, 11, 20, "Pozzuoli", "Procida", 19, 10, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Gestur", 13, 0, 13, 40, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 15, 30, 16, 10, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 17, 55, 18, 35, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 8, 25, 9, 0, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 12, 20, 12, 55, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 16, 20, 16, 55, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 19, 0, 19, 35, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 7, 30, 8, 5, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 10, 10, 10, 45, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 14, 15, 14, 50, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 18, 5, 18, 40, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Medmar", 4, 10, 4, 50, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "23456"));
            transportList.add(new Mezzo("Medmar", 20, 30, 21, 10, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Medmar", 3, 10, 3, 50, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "23456"));
            transportList.add(new Mezzo("Medmar", 19, 40, 20, 20, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Medmar", 5, 0, 5, 20, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "23456"));
            transportList.add(new Mezzo("Medmar", 21, 20, 21, 40, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Medmar", 2, 30, 2, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "23456"));
            transportList.add(new Mezzo("Medmar", 6, 25, 6, 45, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Medmar", 10, 35, 10, 55, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));

            transportList.add(new Mezzo("Traghetto Caremar", 7, 35, 7, 55, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 10, 20, 10, 40, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 11, 5, 11, 25, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 11, 55, 12, 15, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 14, 30, 14, 50, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 16, 25, 18, 45, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 18, 55, 19, 15, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 19, 50, 20, 10, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 20, 35, 20, 55, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 23, 20, 23, 40, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));

            transportList.add(new Mezzo("Traghetto Caremar", 7, 0, 7, 20, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 8, 30, 8, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 11, 30, 11, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 12, 55, 13, 15, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 13, 55, 14, 15, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 15, 30, 15, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 17, 25, 17, 45, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 18, 0, 18, 20, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 19, 55, 20, 15, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));

            transportList.add(new Mezzo("Aliscafo Caremar", 9, 35, 9, 50, "Procida", "Ischia Porto", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 12, 30, 12, 45, "Procida", "Ischia Porto", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 13, 55, 14, 10, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 15, 55, 16, 10, "Procida", "Ischia Porto", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 19, 0, 19, 15, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));

            transportList.add(new Mezzo("Aliscafo Caremar", 7, 30, 7, 45, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 10, 10, 10, 25, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 13, 5, 13, 20, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 14, 30, 14, 45, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 16, 30, 16, 45, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));

            transportList.add(new Mezzo("Aliscafo SNAV", 7, 10, 7, 25, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 9, 45, 10, 0, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 13, 50, 14, 10, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 17, 40, 17, 55, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));

            transportList.add(new Mezzo("Aliscafo SNAV", 9, 0, 9, 15, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 13, 15, 13, 30, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 17, 5, 17, 20, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 19, 45, 10, 0, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));

            update.postValue(new TransportsUpdate(transportList, LocalDateTime.of(2011, 11, 1, 0, 0)));
        }
    }

    private void postInternalStorageData(FileInputStream fstream) {
        try {
            internalAnalytics.send("App Event", "Riempi Mezzi da Internal Storage");

            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String rigaAggiornamento = br.readLine();
            StringTokenizer st0 = new StringTokenizer(rigaAggiornamento, ",");

            int day = Integer.parseInt(st0.nextToken());
            int month = Integer.parseInt(st0.nextToken());
            int year = Integer.parseInt(st0.nextToken());

            ArrayList<Mezzo> transportList = new ArrayList<>();

            for (String line = br.readLine(); line != null; line = br.readLine()) {
                //esamino la riga e creo un mezzo
                StringTokenizer st = new StringTokenizer(line, ",");
                transportList.add(new Mezzo(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken()));
            }

            update.postValue(new TransportsUpdate(transportList, LocalDateTime.of(year, month, day, 0, 0)));

            //Close the input stream
            in.close();


        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

}
