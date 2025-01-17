package com.porfirio.orariprocida2011.threads.weather;

import com.porfirio.orariprocida2011.entity.Osservazione;
import com.porfirio.orariprocida2011.threads.DataUpdate;

import java.util.List;

public class WeatherUpdate extends DataUpdate<List<Osservazione>> {

    public WeatherUpdate(List<Osservazione> data) {
        super(data);
    }

    public WeatherUpdate(Exception error) {
        super(error);
    }

}
