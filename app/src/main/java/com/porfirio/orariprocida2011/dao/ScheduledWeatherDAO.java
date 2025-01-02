package com.porfirio.orariprocida2011.dao;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.porfirio.orariprocida2011.utils.WeatherAPI;
import com.porfirio.orariprocida2011.utils.WeatherUpdate;
import com.porfirio.orariprocida2011.entity.Osservazione;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledWeatherDAO implements WeatherDAO, Closeable {

    private final MutableLiveData<WeatherUpdate> updates = new MutableLiveData<>();
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public void start(long delay, TimeUnit timeUnit) {
        service.schedule(() -> {
            try {
                List<Osservazione> forecasts = WeatherAPI.getForecasts();
                updates.postValue(WeatherUpdate.createSuccessUpdate(forecasts));
            } catch (Exception e) {
                updates.postValue(WeatherUpdate.createErrorUpdate(e));
            }
        }, delay, timeUnit);
    }

    @Override
    public void close() {
        service.shutdown();
    }

    @Override
    public LiveData<WeatherUpdate> getUpdates() {
        return updates;
    }

}
