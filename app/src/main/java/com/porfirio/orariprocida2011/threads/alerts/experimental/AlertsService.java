package com.porfirio.orariprocida2011.threads.alerts.experimental;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.porfirio.orariprocida2011.threads.alerts.Alert;
import com.porfirio.orariprocida2011.threads.alerts.AlertUpdate;
import com.porfirio.orariprocida2011.threads.alerts.AlertsDAO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class AlertsService extends Service implements AlertsDAO {

    public final class LocalBinder extends Binder {

        public AlertsService getService() {
            return AlertsService.this;
        }

    }

    private static final String DATABASE_TAG = "alerts";

    private final IBinder binder = new AlertsService.LocalBinder();

    private DatabaseReference database;
    private ValueEventListener eventListener;
    private MutableLiveData<AlertUpdate> update;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.update = new MutableLiveData<>();
        this.database = FirebaseDatabase.getInstance().getReference(DATABASE_TAG);
        this.eventListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Alert> alerts = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Alert alert = parse(snapshot);
                    alerts.add(alert);
                }

                update.postValue(new AlertUpdate(alerts));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                update.postValue(new AlertUpdate(databaseError.toException()));
            }

        };
        this.database.addValueEventListener(this.eventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.database.removeEventListener(eventListener);
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
