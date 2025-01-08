package com.porfirio.orariprocida2011.threads.weather;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.porfirio.orariprocida2011.entity.Osservazione;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class OnRequestWeatherDAO implements WeatherDAO {

    private static final String TAG = "OnRequestWeatherDAO";
    private static final int UPDATE_MIN_DELAY = 30; // What should the minimum delay be?

    private final MutableLiveData<WeatherUpdate> updates = new MutableLiveData<>();
    private ExecutorService executorService;
    private long lastUpdateTime = -1;

    public OnRequestWeatherDAO(ExecutorService executorService) {
        setExecutorService(executorService);
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = Objects.requireNonNull(executorService);
    }

    public void requestUpdate() {
        long now = System.currentTimeMillis() / 1000;
        long elapsedTime = now - lastUpdateTime;

        if (elapsedTime < UPDATE_MIN_DELAY) {
            Log.w(TAG, "Requested new update too soon, not calling.");
            return;
        }

        executorService.submit(() -> {
            try {
                List<Osservazione> forecasts = WeatherAPI.getForecasts();
                updates.postValue(new WeatherUpdate(forecasts));
                lastUpdateTime = System.currentTimeMillis();
            } catch (Exception e) {
                updates.postValue(new WeatherUpdate(e));
            }
        });
    }

    @Override
    public LiveData<WeatherUpdate> getUpdates() {
        return updates;
    }

}
