package com.porfirio.orariprocida2011.threads.companies;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.porfirio.orariprocida2011.entity.Compagnia;

import java.util.ArrayList;

public class OnRequestCompaniesDAO implements CompaniesDAO {

    private static final String DATABASE_TAG = "companies";

    private final MutableLiveData<CompaniesUpdate> update;
    private final DatabaseReference database;
    private boolean requested;

    public OnRequestCompaniesDAO() {
        this.update = new MutableLiveData<>();
        this.database = FirebaseDatabase.getInstance().getReference(DATABASE_TAG);
    }

    public synchronized void requestUpdate() {
        if (requested)
            return;

        requested = true;

        database.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    ArrayList<Compagnia> companies = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        companies.add(parse(snapshot));

                    update.setValue(new CompaniesUpdate(companies));
                } catch (Exception e) {
                    update.setValue(new CompaniesUpdate(e));
                }

                requested = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                update.setValue(new CompaniesUpdate(databaseError.toException()));
                requested = false;
            }

        });
    }

    @Override
    public LiveData<CompaniesUpdate> getUpdate() {
        return update;
    }

    private Compagnia parse(DataSnapshot snapshot) {
        Compagnia company = new Compagnia(snapshot.getKey(), snapshot.child("name").getValue(String.class));

        if (snapshot.hasChild("contacts")) {
            DataSnapshot contacts = snapshot.child("contacts");

            for (DataSnapshot contact : contacts.getChildren()) {
                String name = contact.getKey();

                for (DataSnapshot number : contact.getChildren())
                    company.addContact(name, number.getValue(String.class));
            }
        }

        return company;
    }

}
