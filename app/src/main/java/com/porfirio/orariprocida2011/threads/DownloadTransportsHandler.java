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

// TODO: should be renamed to something like OnRequestTransportsDAO
public class DownloadTransportsHandler implements TransportsDAO {

    private static final String TAG = DownloadTransportsHandler.class.getSimpleName();

    private final MutableLiveData<TransportsUpdate> update;
    private final DatabaseReference databaseReference;
    private final Analytics analytics;

    public DownloadTransportsHandler(Analytics analytics) {
        this.update = new MutableLiveData<>();
        this.analytics = Objects.requireNonNull(analytics);
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Transports");
    }

    public void requestUpdate() {
        analytics.send("App Event", "Download Mezzi Task");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Mezzo> transportList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Mezzo mezzo = parseMezzo(snapshot);
                    transportList.add(mezzo);
                }

                update.postValue(new TransportsUpdate(transportList, LocalDateTime.now()));
                analytics.send("App Event", "Download Terminated");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase exception: ", databaseError.toException());

                Bundle bundle = new Bundle();
                bundle.putString("error_message", databaseError.getMessage());

                update.postValue(new TransportsUpdate(databaseError.toException()));
            }

        });
    }

    @Override
    public LiveData<TransportsUpdate> getUpdates() {
        return update;
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
