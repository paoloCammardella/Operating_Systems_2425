package com.porfirio.orariprocida2011;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.lifecycle.LifecycleOwner;

import com.porfirio.orariprocida2011.entity.Osservazione;

import java.util.List;
import java.util.function.Consumer;

public final class WeatherDAO {

    private WeatherService weatherService;
    private boolean bound = false;

    private Consumer<List<Osservazione>> onDataReceived;
    private Consumer<Exception> onError;

    private final Context context;
    private final LifecycleOwner owner;

    public WeatherDAO(Context context, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.owner = lifecycleOwner;
    }

    public void start() {
        Intent intent = new Intent(context, WeatherService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        context.unbindService(connection);
        bound = false;
    }

    public void setOnDataReceived(Consumer<List<Osservazione>> onDataReceived) {
        this.onDataReceived = onDataReceived;
    }

    public void setOnError(Consumer<Exception> onError) {
        this.onError = onError;
    }

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            WeatherService.LocalBinder binder = (WeatherService.LocalBinder) service;
            weatherService = binder.getService();
            bound = true;

            weatherService.getForecastsLiveData().observe(owner, forecasts -> {
                if (onDataReceived != null)
                    onDataReceived.accept(forecasts);
            });

            weatherService.getExceptionsLiveData().observe(owner, error -> {
                if (onError != null)
                    onError.accept(error);
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            weatherService.getForecastsLiveData().removeObservers(owner);
        }

    };

}
