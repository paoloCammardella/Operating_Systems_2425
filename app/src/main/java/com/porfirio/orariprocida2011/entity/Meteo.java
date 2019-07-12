package com.porfirio.orariprocida2011.entity;

import android.app.Activity;

import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class Meteo {

	private Activity callingActivity;
    private ArrayList<Osservazione> osservazione = null;
	
	public Meteo(OrariProcida2011Activity orariProcida2011Activity) {
		callingActivity=orariProcida2011Activity;
        osservazione = new ArrayList<Osservazione>();
    }



    public String condimeteoString(Mezzo mezzo) {
        String result;
        Double actualBeaufort = osservazione.get(0).getWindBeaufort();
		Double limitBeaufort=0.0;
		
		//Penalizzazione per le brezze estive
		if ((Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MONTH)>=5)&&(Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MONTH)<=8))
			limitBeaufort+=2;
		//Aggiunto Aladino
		if (mezzo.nave.equals("Procida Lines") || mezzo.nave.equals("Gestur")|| mezzo.nave.contains("Ippocampo")||mezzo.nave.contains("Aladino")) 
			limitBeaufort-=1; //penalizzazione per mezzi piccoli
		else if (mezzo.nave.equals(callingActivity.getString(R.string.aliscafo)+" SNAV"))
			limitBeaufort-=0.5; //penalizzazione per compagnia privata
		if (mezzo.oraPartenza.get(Calendar.HOUR_OF_DAY)==7 && mezzo.oraPartenza.get(Calendar.MINUTE)==40)
			limitBeaufort+=1; // incremento per corsa fondamentale
		else if (mezzo.oraPartenza.get(Calendar.HOUR_OF_DAY)==19 && mezzo.oraPartenza.get(Calendar.MINUTE)==25)
			limitBeaufort+=1; // incremento per corsa fondamentale
		else if (mezzo.oraPartenza.get(Calendar.HOUR_OF_DAY)==6 && mezzo.oraPartenza.get(Calendar.MINUTE)==25)
			limitBeaufort+=1; // incremento per corsa fondamentale
		else if (mezzo.oraPartenza.get(Calendar.HOUR_OF_DAY)==20 && mezzo.oraPartenza.get(Calendar.MINUTE)==0)
			limitBeaufort+=1; // incremento per corsa fondamentale
		//Non metto aggiustamenti per l'orario perche' ho dati solo su base giornaliera
		//Non metto aggiustamenti in base ai porti perche' ho dati per tutto il golfo

        if ((osservazione.get(0).getWindDirection() == 0 || osservazione.get(0).getWindDirection() == 315) && (mezzo.portoArrivo.contains("Ischia") || mezzo.portoPartenza.contains("Ischia") || mezzo.portoArrivo.contains("Casamicciola") || mezzo.portoPartenza.contains("Casamicciola")))
			limitBeaufort+=4;
        else if ((osservazione.get(0).getWindDirection() == 0 || osservazione.get(0).getWindDirection() == 315) && (mezzo.portoArrivo.contains("Napoli") || mezzo.portoPartenza.contains("Napoli") || mezzo.portoArrivo.contentEquals("Pozzuoli") || mezzo.portoPartenza.contentEquals("Pozzuoli")))
			limitBeaufort+=5;
        else if ((osservazione.get(0).getWindDirection() == 45 || osservazione.get(0).getWindDirection() == 90))
			limitBeaufort+=4;
        else if ((osservazione.get(0).getWindDirection() == 135 || osservazione.get(0).getWindDirection() == 180 || osservazione.get(0).getWindDirection() == 225) && (!(mezzo.nave.contains("Aliscafo"))))
			limitBeaufort+=4;
        else if ((osservazione.get(0).getWindDirection() == 135 || osservazione.get(0).getWindDirection() == 180 || osservazione.get(0).getWindDirection() == 225) && (mezzo.nave.contains("Aliscafo")))
			limitBeaufort+=3;
        else if ((osservazione.get(0).getWindDirection() == 270))
			limitBeaufort+=3;
		else if (mezzo.portoPartenza.contentEquals("Monte di Procida")||mezzo.portoArrivo.contentEquals("Monte di Procida"))
			limitBeaufort+=4; //TODO Metto valore standard per il porto di Monte di Procida
		
		double extraWind=actualBeaufort-limitBeaufort;
		if (extraWind<=0)
			result="";
		else if (extraWind<=1)
			result=" - "+callingActivity.getString(R.string.pocoProbabile);
		else if (extraWind<=2)
			result=" - "+callingActivity.getString(R.string.aRischio);
		else if (extraWind<=3)
			result=" - "+callingActivity.getString(R.string.corsaQuasi);
		else
			result=" - "+callingActivity.getString(R.string.corsaImpossibile);
		return result;
	
	}


    public ArrayList<Osservazione> getOsservazione() {
        return osservazione;
    }

    public void setOsservazione(ArrayList<Osservazione> osservazione) {
        this.osservazione = osservazione;
    }
}
