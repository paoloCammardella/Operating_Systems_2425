package com.porfirio.orariprocida2011.entity;

import android.app.Activity;

import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class Meteo {

    private Activity callingActivity;
    private List<Osservazione> osservazione;

    public Meteo(OrariProcida2011Activity orariProcida2011Activity) {
        callingActivity = orariProcida2011Activity;
        osservazione = new ArrayList<>();
    }

    public double getForecast(Mezzo route) {
        long delta = (route.oraPartenza.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
        delta = (long) Math.floor((double) delta / (1000 * 60 * 60));
        //if (mezzo.oraPartenza.get(Calendar.DAY_OF_YEAR)>Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
        if (delta < 0)
            delta += 24;
        delta /= 3;
        int previsione = (int) delta;

        double limitBeaufort = 0.0;
        double actualBeaufort = 0.0;

        if (!osservazione.isEmpty() && osservazione.size() > previsione)
            actualBeaufort = osservazione.get(previsione).getWindBeaufort();

        // Penalizzazione per le brezze estive
        if (isSummer())
            limitBeaufort += 2;

        // Aggiunto Aladino
        if (route.nave.equals("Procida Lines") || route.nave.equals("Gestur") || route.nave.contains("Ippocampo") || route.nave.contains("Aladino"))
            limitBeaufort -= 1; //penalizzazione per mezzi piccoli
        else if (route.nave.equals(callingActivity.getString(R.string.aliscafo) + " SNAV"))
            limitBeaufort -= 0.5; //penalizzazione per compagnia privata
        if (route.oraPartenza.get(Calendar.HOUR_OF_DAY) == 7 && route.oraPartenza.get(Calendar.MINUTE) == 40)
            limitBeaufort += 1; // incremento per corsa fondamentale
        else if (route.oraPartenza.get(Calendar.HOUR_OF_DAY) == 19 && route.oraPartenza.get(Calendar.MINUTE) == 25)
            limitBeaufort += 1; // incremento per corsa fondamentale
        else if (route.oraPartenza.get(Calendar.HOUR_OF_DAY) == 6 && route.oraPartenza.get(Calendar.MINUTE) == 25)
            limitBeaufort += 1; // incremento per corsa fondamentale
        else if (route.oraPartenza.get(Calendar.HOUR_OF_DAY) == 20 && route.oraPartenza.get(Calendar.MINUTE) == 0)
            limitBeaufort += 1; // incremento per corsa fondamentale
        //Non metto aggiustamenti per l'orario perche' ho dati solo su base giornaliera
        //Non metto aggiustamenti in base ai porti perche' ho dati per tutto il golfo

        Osservazione.Direction windDirection = osservazione.get(previsione).getWindDirection();

        if ((windDirection == Osservazione.Direction.N || windDirection == Osservazione.Direction.NW) && (route.portoArrivo.contains("Ischia") || route.portoPartenza.contains("Ischia") || route.portoArrivo.contains("Casamicciola") || route.portoPartenza.contains("Casamicciola")))
            limitBeaufort += 4;
        else if ((windDirection == Osservazione.Direction.N || windDirection == Osservazione.Direction.NW) && (route.portoArrivo.contains("Napoli") || route.portoPartenza.contains("Napoli") || route.portoArrivo.contentEquals("Pozzuoli") || route.portoPartenza.contentEquals("Pozzuoli")))
            limitBeaufort += 5;
        else if ((windDirection == Osservazione.Direction.NE || windDirection == Osservazione.Direction.E))
            limitBeaufort += 4;
        else if (windDirection == Osservazione.Direction.SE || windDirection == Osservazione.Direction.S || windDirection == Osservazione.Direction.SW) {
            if (route.nave.contains("Aliscafo"))
                limitBeaufort += 3;
            else
                limitBeaufort += 4;
        } else if ((windDirection == Osservazione.Direction.W))
            limitBeaufort += 3;
        else if (route.portoPartenza.contentEquals("Monte di Procida") || route.portoArrivo.contentEquals("Monte di Procida"))
            limitBeaufort += 4; //TODO Metto valore standard per il porto di Monte di Procida

        return actualBeaufort - limitBeaufort;
    }

    public List<Osservazione> getOsservazione() {
        return osservazione;
    }

    public void setOsservazione(List<Osservazione> osservazione) {
        this.osservazione = osservazione;
    }

    private boolean isSummer() {
        int month = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MONTH);
        return month >= 5 && month <= 8;
    }

}
