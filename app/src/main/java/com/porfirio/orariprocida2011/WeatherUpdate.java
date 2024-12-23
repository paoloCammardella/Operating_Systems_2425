package com.porfirio.orariprocida2011;

import com.porfirio.orariprocida2011.entity.Osservazione;

import java.util.List;

public class WeatherUpdate {

    private final boolean isValid;
    private final List<Osservazione> forecasts;
    private final Exception error;

    public WeatherUpdate(boolean isValid, List<Osservazione> forecasts, Exception error) {
        this.isValid = isValid;
        this.forecasts = forecasts;
        this.error = error;
    }

    public boolean isValid() {
        return isValid;
    }

    public List<Osservazione> getForecasts() {
        return forecasts;
    }

    public Exception getError() {
        return error;
    }

    public static WeatherUpdate createSuccessUpdate(List<Osservazione> forecasts) {
        return new WeatherUpdate(true, forecasts, null);
    }

    public static WeatherUpdate createErrorUpdate(Exception e) {
        return createErrorUpdate(e, null);
    }

    public static WeatherUpdate createErrorUpdate(Exception e, List<Osservazione> backupData) {
        return new WeatherUpdate(false, backupData, e);
    }

}
