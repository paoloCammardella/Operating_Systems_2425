package com.porfirio.orariprocida2011.threads.alerts;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class OnRequestAlertsDAO implements AlertsDAO {

    private static final String DATABASE_TAG = "alerts";

    private final MutableLiveData<AlertUpdate> update;
    private final DatabaseReference database;
    private boolean requested = false;

    public OnRequestAlertsDAO() {
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
                ArrayList<Alert> alerts = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Alert alert = parse(snapshot);
                    alerts.add(alert);
                }

                update.setValue(new AlertUpdate(alerts));
                requested = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                update.setValue(new AlertUpdate(databaseError.toException()));
                requested = false;
            }

        });
    }

    @Override
    public LiveData<AlertUpdate> getUpdates() {
        return update;
    }

    @Override
    public void send(Alert alert) {
        String key = database.push().getKey();

        if (key == null)
            throw new IllegalStateException();

        HashMap<String, Object> attributes = new HashMap<>();

        attributes.put("routeId", alert.getRouteId());
        attributes.put("arrivalLocation", alert.getArrivalLocation());
        attributes.put("arrivalTime", DateTimeFormatter.ISO_LOCAL_TIME.format(alert.getArrivalTime()));
        attributes.put("departureLocation", alert.getDepartureLocation());
        attributes.put("departureTime", DateTimeFormatter.ISO_LOCAL_TIME.format(alert.getDepartureTime()));
        attributes.put("transportDate", DateTimeFormatter.ISO_LOCAL_DATE.format(alert.getTransportDate()));
        attributes.put("transport", alert.getTransport());
        attributes.put("details", alert.getDetails());
        attributes.put("reason", alert.getReason());

        database.child(key).setValue(attributes);
    }

    private Alert parse(DataSnapshot snapshot) {
        return new Alert(
                snapshot.getKey(),
                snapshot.child("routeId").getValue(String.class),
                snapshot.child("transport").getValue(String.class),
                snapshot.child("reason").getValue(Integer.class),
                snapshot.child("details").getValue(String.class),
                snapshot.child("departureLocation").getValue(String.class),
                LocalTime.parse(snapshot.child("departureTime").getValue(String.class)),
                snapshot.child("arrivalLocation").getValue(String.class),
                LocalTime.parse(snapshot.child("arrivalTime").getValue(String.class)),
                LocalDate.parse(snapshot.child("transportDate").getValue(String.class))
        );
    }

}
