package com.porfirio.orariprocida2011.entity;

import android.content.Context;

import com.porfirio.orariprocida2011.R;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Meteo {

    private List<Osservazione> forecasts = new ArrayList<>();

    public double getForecast(Context context, Mezzo route) {
        if (forecasts.isEmpty())
            return 0;

        LocalDateTime departureTime = Instant.ofEpochMilli(route.oraPartenza.getTimeInMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        // NOTE: workaround because departure time doesn't save the actual day so it may be checking the next-day route
        if (departureTime.isBefore(now))
            departureTime = departureTime.plusDays(1);

        int hoursUntilDeparture = (int) Duration.between(now, departureTime).getSeconds() / (60 * 60);
        int forecastIndex = hoursUntilDeparture / 3;

        double limitBeaufort = 0.0;
        double actualBeaufort = 0.0;

        if (forecastIndex < forecasts.size())
            actualBeaufort = forecasts.get(forecastIndex).getWindBeaufort();

        // summer breeze penalty
        if (isSummer(now))
            limitBeaufort += 2;

        // TODO: replace string comparisons?

        if (route.nave.equals("Procida Lines") || route.nave.equals("Gestur") || route.nave.contains("Ippocampo") || route.nave.contains("Aladino"))
            limitBeaufort -= 1; // small vehicles penalty
        else if (route.nave.equals(context.getString(R.string.aliscafo) + " SNAV"))
            limitBeaufort -= 0.5; // private company penalty
        if (departureTime.getHour() == 7 && departureTime.getMinute() == 40)
            limitBeaufort += 1; // critical route bonus
        else if (departureTime.getHour() == 19 && departureTime.getMinute() == 25)
            limitBeaufort += 1; // critical route bonus
        else if (departureTime.getHour() == 6 && departureTime.getMinute() == 25)
            limitBeaufort += 1; // critical route bonus
        else if (departureTime.getHour() == 20 && departureTime.getMinute() == 0)
            limitBeaufort += 1; // critical route bonus

        // Non metto aggiustamenti per l'orario perchè ho dati solo su base giornaliera
        // Non metto aggiustamenti in base ai porti perchè ho dati per tutto il golfo

        Osservazione.Direction windDirection = forecasts.get(forecastIndex).getWindDirection();

        if ((windDirection == Osservazione.Direction.N || windDirection == Osservazione.Direction.NW)) {
            if (route.portoArrivo.contains("Ischia") || route.portoPartenza.contains("Ischia") || route.portoArrivo.contains("Casamicciola") || route.portoPartenza.contains("Casamicciola"))
                limitBeaufort += 4;
            else if (route.portoArrivo.contains("Napoli") || route.portoPartenza.contains("Napoli") || route.portoArrivo.contentEquals("Pozzuoli") || route.portoPartenza.contentEquals("Pozzuoli"))
                limitBeaufort += 5;
        } else if ((windDirection == Osservazione.Direction.NE || windDirection == Osservazione.Direction.E))
            limitBeaufort += 4;
        else if (windDirection == Osservazione.Direction.SE || windDirection == Osservazione.Direction.S || windDirection == Osservazione.Direction.SW) {
            if (route.nave.contains("Aliscafo"))
                limitBeaufort += 3;
            else
                limitBeaufort += 4;
        } else if ((windDirection == Osservazione.Direction.W))
            limitBeaufort += 3;
        else if (route.portoPartenza.contentEquals("Monte di Procida") || route.portoArrivo.contentEquals("Monte di Procida"))
            limitBeaufort += 4;

        return actualBeaufort - limitBeaufort;
    }

    public List<Osservazione> getForecasts() {
        return forecasts;
    }

    public void setForecasts(List<Osservazione> forecasts) {
        this.forecasts = forecasts;
    }

    private boolean isSummer(LocalDateTime time) {
        int month = time.getMonthValue();
        return month >= 5 && month <= 8;
    }

}
