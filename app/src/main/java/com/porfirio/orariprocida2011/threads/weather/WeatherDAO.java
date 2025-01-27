package com.porfirio.orariprocida2011.threads.weather;

import androidx.lifecycle.LiveData;

/**
 * Interface to receive weather forecasts.
 */
public interface WeatherDAO {

    /**
     * Returns the LiveData containing the latest forecasts update.
     * The implementation should send it in the main thread.
     *
     * @return LiveData with latest forecasts update
     */
    LiveData<WeatherUpdate> getUpdates();

}
