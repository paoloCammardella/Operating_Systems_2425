package com.porfirio.orariprocida2011.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Mezzo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Porfirio on 16/02/2018.
 */

public class LeggiSegnalazioniTask extends AsyncTask<OrariProcida2011Activity, Integer, Boolean> {
    private OrariProcida2011Activity act;

    // Do the long-running work in here
    protected Boolean doInBackground(OrariProcida2011Activity... activities) {
        act = activities[0];

        String urlS = "http://unoprocidaresidente.altervista.org/segnalazioni.csv";
        HttpURLConnection connS = null;
        InputStream inS;
        try {
            connS = (HttpURLConnection) new URL(urlS).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert connS != null;
            inS = connS.getInputStream();
            BufferedReader rS = new BufferedReader(new InputStreamReader(inS));
            String rigaData;
            String rigaMezzo;
            String rigaMotivo;
            String rigaDettagli;
            for (String line = rS.readLine(); line != null; line = rS.readLine()) {
                rigaData = line;
                rigaMezzo = rS.readLine();
                rigaMotivo = rS.readLine();
                rigaDettagli = rS.readLine();

                if (act.isGiornoVisualizzato(rigaData, act.c)) {
                    //devo trovare il mezzo in list mezzi (con un equals da implementare)
                    //una volta trovato incremento il contatore delle segnalazioni (da aggiungere come attributo)
                    //infine nella visualizzazione dei mezzi devo metterci il valore del contatore e anche nei dettagli
                    for (Mezzo m : act.listMezzi) {
                        if (act.stessoMezzo(rigaData, rigaMezzo, m, act.c)) {
                            m.addMotivo(rigaMotivo);
                        }
                    }

                }


            }
            rS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }


    // This is called each time you call publishProgress()
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }


    // This is called when doInBackground() is finished
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.d("ORARI", "Lette segnalazioni da web");
            act.aggiornaLista();
            Log.d("ORARI", "Terminato aggiornamento orari su GUI");
            //gli orari del web erano piu' aggiornati
            //bisogna aggiornare la GUI
        }
        //showNotification("Downloaded " + result + " bytes");
    }
}