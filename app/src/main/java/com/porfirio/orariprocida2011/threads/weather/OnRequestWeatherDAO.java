package com.porfirio.orariprocida2011.threads.weather;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.porfirio.orariprocida2011.entity.Osservazione;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class OnRequestWeatherDAO implements WeatherDAO {

    private final MutableLiveData<WeatherUpdate> updates = new MutableLiveData<>();
    private ExecutorService executorService;

    public OnRequestWeatherDAO(ExecutorService executorService) {
        setExecutorService(executorService);
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = Objects.requireNonNull(executorService);
    }

    public void requestUpdate() {
        executorService.submit(() -> {
            try {
                List<Osservazione> forecasts = WeatherAPI.getForecasts();
                updates.postValue(new WeatherUpdate(forecasts));
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
