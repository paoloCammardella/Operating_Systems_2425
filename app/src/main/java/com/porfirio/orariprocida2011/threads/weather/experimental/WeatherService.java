package com.porfirio.orariprocida2011.threads.weather.experimental;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.lifecycle.LiveData;

import com.porfirio.orariprocida2011.threads.weather.WeatherDAO;
import com.porfirio.orariprocida2011.threads.weather.WeatherUpdate;

import java.util.concurrent.TimeUnit;

public class WeatherService extends Service implements WeatherDAO {


    public final class LocalBinder extends Binder {

        public WeatherService getService() {
            return WeatherService.this;
        }

    }

    private final IBinder binder = new LocalBinder();
    private final ScheduledWeatherDAO weatherDAO = new ScheduledWeatherDAO();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        weatherDAO.start(1, TimeUnit.HOURS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        weatherDAO.close();
    }

    @Override
    public LiveData<WeatherUpdate> getUpdates() {
        return weatherDAO.getUpdates();
    }

}