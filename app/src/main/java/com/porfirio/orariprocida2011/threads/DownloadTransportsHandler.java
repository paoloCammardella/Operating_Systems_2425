package com.porfirio.orariprocida2011.threads;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.utils.Analytics;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

// TODO: should be renamed to something like OnRequestTransportsDAO
public class DownloadTransportsHandler implements TransportsDAO {

    private final MutableLiveData<TransportsUpdate> update;
    private ExecutorService executorService;

    // Firebase references
    private final DatabaseReference databaseReference;

    private Analytics internalAnalytics;

    public DownloadTransportsHandler(Analytics analytics, ExecutorService executorService) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Transports");

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

    public void requestUpdate() {
        executorService.submit(() -> {
            internalAnalytics.send("App Event", "Download Mezzi Task");
            fetchTransportDataFromRealtimeDatabase();
            internalAnalytics.send("App Event", "Download Terminated");
        });
    }

    @Override
    public LiveData<TransportsUpdate> getUpdates() {
        return update;
    }

    //TODO cambiare effettivamente i valori nel Firebase Realtime DB con quelli reali
    //TODO creare un backend per automatizzarre il riempimento del DB Firebase
    private void fetchTransportDataFromRealtimeDatabase() {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Mezzo> transportList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Mezzo mezzo = parseMezzo(snapshot);
                    transportList.add(mezzo);
                }

                update.postValue(new TransportsUpdate(transportList, LocalDateTime.now()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DownloadHandler", "Errore Realtime Database: " + databaseError.getMessage(), databaseError.toException());

                Bundle bundle = new Bundle();
                bundle.putString("error_message", databaseError.getMessage());

                update.postValue(new TransportsUpdate(databaseError.toException()));
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

}
