package com.porfirio.orariprocida2011;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.porfirio.orariprocida2011.entity.Osservazione;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnRequestWeatherDAO implements WeatherDAO {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<WeatherUpdate> updates = new MutableLiveData<>();

    public void requestUpdate() {
        executorService.submit(() -> {
            try {
                List<Osservazione> forecasts = WeatherAPI.getForecasts();
                updates.postValue(WeatherUpdate.createSuccessUpdate(forecasts));
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
