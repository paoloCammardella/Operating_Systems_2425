package com.porfirio.orariprocida2011;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.porfirio.orariprocida2011.entity.Osservazione;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnRequestWeatherDAO implements WeatherDAO {

    private static final String TAG = "OnRequestWeatherDAO";
    private static final int UPDATE_MIN_DELAY = 30; // What should the minimum delay be?

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<WeatherUpdate> updates = new MutableLiveData<>();

    private long lastUpdateTime = -1;

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
                updates.postValue(WeatherUpdate.createSuccessUpdate(forecasts));
                lastUpdateTime = System.currentTimeMillis();
            } catch (Exception e) {
                updates.postValue(WeatherUpdate.createErrorUpdate(e));
            }
        });
    }

    @Override
    public LiveData<WeatherUpdate> getUpdates() {
        return updates;
    }

}
