package com.porfirio.orariprocida2011.threads.weather.experimental;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.porfirio.orariprocida2011.entity.Osservazione;
import com.porfirio.orariprocida2011.threads.weather.WeatherAPI;
import com.porfirio.orariprocida2011.threads.weather.WeatherDAO;
import com.porfirio.orariprocida2011.threads.weather.WeatherUpdate;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledWeatherDAO implements WeatherDAO, Closeable {

    private final MutableLiveData<WeatherUpdate> updates = new MutableLiveData<>();
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;
    private boolean started;
    private boolean closed;

    public void start(long delay, TimeUnit timeUnit) {
        if (started)
            return;

        if (closed)
            throw new IllegalStateException("DAO has already been closed");

        future = service.scheduleWithFixedDelay(() -> {
            try {
                List<Osservazione> forecasts = WeatherAPI.getForecasts();
                updates.postValue(new WeatherUpdate(forecasts));
            } catch (Exception e) {
                updates.postValue(new WeatherUpdate(e));
            }
        }, 0, delay, timeUnit);

        started = true;
    }

    @Override
    public void close() {
        future.cancel(false);
        service.shutdown();
        closed = true;
    }

    @Override
    public LiveData<WeatherUpdate> getUpdates() {
        return updates;
    }

}
