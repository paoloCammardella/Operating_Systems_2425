package com.porfirio.orariprocida2011.dao;

import androidx.lifecycle.LiveData;

import com.porfirio.orariprocida2011.utils.WeatherUpdate;

public interface WeatherDAO {

    LiveData<WeatherUpdate> getUpdates();

}
