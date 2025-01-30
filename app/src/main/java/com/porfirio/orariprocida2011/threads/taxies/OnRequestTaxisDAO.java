package com.porfirio.orariprocida2011.threads.taxies;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.entity.Taxi;
import com.porfirio.orariprocida2011.threads.companies.CompaniesUpdate;

import java.util.ArrayList;

public class OnRequestTaxisDAO implements TaxisDAO {

    private static final String DATABASE_TAG = "taxis";

    private final MutableLiveData<TaxisUpdate> update;
    private final DatabaseReference database;
    private boolean requested;

    public OnRequestTaxisDAO() {
        this.update = new MutableLiveData<>();
        this.database = FirebaseDatabase.getInstance().getReference(DATABASE_TAG);
    }

    /**
     * Requests an update.
     */
    public synchronized void requestUpdate() {
        if (requested)
            return;

        requested = true;

        database.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    ArrayList<Taxi> taxis = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        taxis.add(parse(snapshot));

                    update.setValue(new TaxisUpdate(taxis));
                } catch (Exception e) {
                    update.setValue(new TaxisUpdate(e));
                }

                requested = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                update.setValue(new TaxisUpdate(databaseError.toException()));
                requested = false;
            }

        });
    }

    @Override
    public LiveData<TaxisUpdate> getUpdate() {
        return update;
    }

    private Taxi parse(DataSnapshot snapshot) {
        String location = snapshot.child("location").getValue(String.class);
        String name = snapshot.child("name").getValue(String.class);
        String number = snapshot.child("number").getValue(String.class);

        return new Taxi(location, name, number);
    }

}
