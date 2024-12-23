package com.porfirio.orariprocida2011;

import androidx.lifecycle.LiveData;

public interface WeatherDAO {

    LiveData<WeatherUpdate> getUpdates();

}
