package com.porfirio.orariprocida2011.threads.weather;

import com.porfirio.orariprocida2011.entity.Osservazione;

import java.util.List;

public class WeatherUpdate {

    private final boolean isValid;
    private final List<Osservazione> data;
    private final Exception error;

    public WeatherUpdate(List<Osservazione> data) {
        this.isValid = true;
        this.data = data;
        this.error = null;
    }

    public WeatherUpdate(Exception error) {
        this.isValid = false;
        this.data = null;
        this.error = error;
    }

    public boolean isValid() {
        return isValid;
    }

    public List<Osservazione> getData() {
        return data;
    }

    public Exception getError() {
        return error;
    }

}
