package com.porfirio.orariprocida2011.threads.weather;

import androidx.lifecycle.LiveData;

public interface WeatherDAO {

    LiveData<WeatherUpdate> getUpdates();

}
