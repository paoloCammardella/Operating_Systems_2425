package com.porfirio.orariprocida2011;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.porfirio.orariprocida2011.entity.Osservazione;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherService extends Service {

    public class LocalBinder extends Binder {

        public WeatherService getService() {
            return WeatherService.this;
        }

    }

    private static final String TAG = "WeatherService";
    private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/forecast?id=3169807&APPID=dc8cfde44c4955e792406e26a562945e&units=metric";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IBinder binder = new LocalBinder();
    private final MutableLiveData<List<Osservazione>> forecastsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Exception> exceptionMutableLiveData = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        executorService.submit(this::fetchWeather);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void fetchWeather() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(WEATHER_URL).openStream(), StandardCharsets.UTF_8))) {
            String jsonText = readAll(reader);
            JSONObject jsonObject = new JSONObject(jsonText);

            String controlCode = (String) jsonObject.get("cod");

            if (controlCode.equals("200")) {
                ArrayList<Osservazione> forecasts = new ArrayList<>();

                JSONArray list = jsonObject.getJSONArray("list");

                // 8 osservazioni, una ogni tre ore

                for (int i = 0; i < 8; i++) {
                    JSONObject cond = list.getJSONObject(i);

                    double windDir = cond.getJSONObject("wind").getDouble("deg");
                    double windSpeed = cond.getJSONObject("wind").getDouble("speed") * 3.6;
                    String timestamp = cond.getString("dt_txt");

                    LocalDateTime time = LocalDateTime.parse(timestamp, TIME_FORMATTER);

                    forecasts.add(new Osservazione(100, windDir, time));
                }

                forecastsLiveData.postValue(forecasts);
            }

            // TODO: save update to internal storage
            // TODO: save update timestamp to internal storage

//            lastUpdateTimestamp = System.currentTimeMillis() / 1000L;
        } catch (Exception e) {
            exceptionMutableLiveData.postValue(e);
            Log.e(TAG, "fetchWeather: ", e);
        }
    }

    public LiveData<List<Osservazione>> getForecastsLiveData() {
        return forecastsLiveData;
    }

    public LiveData<Exception> getExceptionsLiveData() {
        return exceptionMutableLiveData;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;

        while ((cp = rd.read()) != -1)
            sb.append((char) cp);

        return sb.toString();
    }

}