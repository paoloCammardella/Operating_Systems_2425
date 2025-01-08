package com.porfirio.orariprocida2011.threads.weather;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
                updates.postValue(new WeatherUpdate(forecasts));
            } catch (Exception e) {
                updates.postValue(new WeatherUpdate(e));
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
