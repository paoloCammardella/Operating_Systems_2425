package com.porfirio.orariprocida2011.threads.weather;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.porfirio.orariprocida2011.entity.Osservazione;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnRequestWeatherDAO implements WeatherDAO, Closeable {

    private final MutableLiveData<WeatherUpdate> updates = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean requested;

    public synchronized void requestUpdate() {
        if (requested)
            return;

        requested = true;

        executorService.submit(() -> {
            try {
                List<Osservazione> forecasts = WeatherAPI.getForecasts();
                updates.postValue(new WeatherUpdate(forecasts));
            } catch (Exception e) {
                updates.postValue(new WeatherUpdate(e));
            }

            requested = false;
        });
    }

    @Override
    public LiveData<WeatherUpdate> getUpdates() {
        return updates;
    }

    @Override
    public synchronized void close() {
        executorService.shutdown();
    }

}
