package com.porfirio.orariprocida2011.threads.transports;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.porfirio.orariprocida2011.entity.Mezzo;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class OnRequestTransportsDAO implements TransportsDAO {

    private final MutableLiveData<TransportsUpdate> update;
    private final DatabaseReference databaseReference;

    public OnRequestTransportsDAO() {
        this.update = new MutableLiveData<>();
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Transports");
    }

    public void requestUpdate() {
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
            snapshot.child("nomeNave").getValue(String.class),
            snapshot.child("oraPartenza").getValue(String.class),
            snapshot.child("oraArrivo").getValue(String.class),
            snapshot.child("inizioEsclusione").getValue(String.class),
            snapshot.child("fineEsclusione").getValue(String.class),
            snapshot.child("portoPartenza").getValue(String.class),
            snapshot.child("portoArrivo").getValue(String.class),
            snapshot.child("giorniSettimana").getValue(String.class)
        );
    }


}
