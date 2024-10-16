package com.porfirio.orariprocida2011.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Osservazione;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * TODO Aggiornare la lettura asincrona aggiornando i metodi deprecati
 */

public class LeggiMeteoTask extends AsyncTask<Void, Integer, Boolean> {
    private final OrariProcida2011Activity act;

    public static Semaphore taskMeteo;
    public static Semaphore taskMeteoStart;

    public LeggiMeteoTask(OrariProcida2011Activity orariProcida2011Activity) {
        this.act = orariProcida2011Activity;
    }

    // Do the long-running work in here
    protected Boolean doInBackground(Void... param) {
        //act = activities[0];
        if (taskMeteoStart != null) {
            try {
                taskMeteoStart.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("TEST", "TASK: Inizia il task meteo");
            taskMeteoStart.release();
        }

        act.mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("Leggi Meteo Task")
                .build());

        /* Create a URL we want to load some xml-data from. */
        URL url;
        Double windKmhFromIS = 0.0;
        Integer windDirFromIS = 0;
        String windDirectionStringFromIS = act.getString(R.string.nord);


        if (act.isOnline()) //VALUTA SEMPRE UN NUOVO METEO
                try {
                    JSONObject jsonObject = null;
                    try {
                          InputStream is = new URL("http://api.openweathermap.org/data/2.5/forecast?id=3169807&APPID=dc8cfde44c4955e792406e26a562945e&units=metric").openStream();

                        try {
                            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                            String jsonText = OrariProcida2011Activity.readAll(rd);
                            jsonObject = new JSONObject(jsonText);
                        } finally {
                            is.close();
                        }
                        act.mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("App Event")
                                .setAction("Updated Meteo")
                                .build());
                    } catch (SocketTimeoutException e) {
                        //Toast.makeText(act.getApplicationContext(), act.getString(R.string.connessioneLenta), Toast.LENGTH_LONG).show();
                        return false;
                    } catch (IOException e) {
                        //
                        Log.d("ORARI", "dati meteo non caricati da web");
                        e.printStackTrace();
                        return false;
                    }
//Grazie alla formattazione ottenuta con http://jsonformatter.curiousconcept.com/
                    String controlCode = (String) jsonObject.get("cod");

                    if (controlCode.equals("200")) {
                        //200 = OK
                        // windDir = Double.parseDouble((String) jsonObject.getJSONObject("wind").get("deg"));
                        JSONArray list = jsonObject.getJSONArray("list");
                        for (int i = 0; i < 8; i++) { //8 osservazioni, una ogni tre ore

                            JSONObject cond = list.getJSONObject(i);
                            Osservazione o = new Osservazione();
                            Double windDir = cond.getJSONObject("wind").getDouble("deg");
                            // = Double.parseDouble(strWindDir);
                            Double windMs = cond.getJSONObject("wind").getDouble("speed");
                            Double windKmh = windMs * 3.6;
                            o.setWindKmh(windKmh);
                            o.setWindDirection((int) (45 * (Math.round(windDir / 45.0))) % 360);
                            o.setWindDirectionString(o.getWindDirection(), act);
                            o.setWindBeaufort(o.getWindKmh());
                            //LEGGI TEMPO
                            String tempo = cond.getString("dt_txt");
                            Timestamp timestamp = Timestamp.valueOf(tempo);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(timestamp.getTime());
                            o.setTempo(calendar);
                            act.meteo.getOsservazione().add(o);
                        }

                    }



                    Log.d("ORARI", "letto da json");
                    //scrivo l'aggiornamento su internal storage
                    FileOutputStream fos = null;
                    try {
                        fos = act.openFileOutput("aggiornamentoMeteo.csv", Context.MODE_PRIVATE);
                    } catch (FileNotFoundException e) {
                        //
                        e.printStackTrace();
                    }

                    act.aggiornamentoMeteo = Calendar.getInstance();


                } catch (JSONException e) {
                    //
                    e.printStackTrace();
                    Log.d("ORARI", "dati meteo non caricati da web");
                    if (act.isOnline()) Log.d("ORARI", "per mancanza di connessione");
                    else Log.d("ORARI", "perche' ho dati abbastanza aggiornati");

                }

        if (taskMeteo != null) {
            Log.d("TEST", "TASK: Il task meteo pronto a terminare");

            try {
                if (!taskMeteo.tryAcquire(20L, TimeUnit.SECONDS))
                    Log.d("TEST", "TASK: TIMEOUT task meteo ");
                //act.finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d("TEST", "TASK: Il task meteo termina");
            taskMeteo.release();
            Log.d("ORDER", "Meteo task");
        }
        act.mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("Task Meteo Terminated")
                .build());
        return true;
    }

    // This is called each time you call publishProgress()
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }


    // This is called when doInBackground() is finished
    protected void onPostExecute(Boolean result) {
        if (result) {

//            act.meteoDialog.setMessage(act.getString(R.string.condimeteo) + act.meteo.getOsservazione().get(0).getWindBeaufortString(act) + " (" + act.meteo.getOsservazione().get(0).getWindKmh().intValue() + " km/h) " + act.getString(R.string.da) + " " + act.meteo.getOsservazione().get(0).getWindDirectionString()
//                    + "\n" + act.getString(R.string.updated) + " " + act.aggiornamentoMeteo.get(Calendar.DAY_OF_MONTH) + "/" + (1 + act.aggiornamentoMeteo.get(Calendar.MONTH)) + "/" + act.aggiornamentoMeteo.get(Calendar.YEAR) + " " + act.getString(R.string.ore) + " " + act.aggiornamentoMeteo.get(Calendar.HOUR_OF_DAY) + ":" + act.aggiornamentoMeteo.get(Calendar.MINUTE));
            //TODO Altri dettagli sulle condizioni meteorologiche
            if (act.meteo.getOsservazione() != null) {
                if (!act.meteo.getOsservazione().isEmpty()) {
                    //Log.d("CONDIMETEO", "AVVIO" + act.getString(R.string.condimeteo) + act.meteo.getOsservazione().get(0).getWindBeaufortString(act) + " (" + act.meteo.getOsservazione().get(0).getWindKmh().intValue() + " km/h) " + act.getString(R.string.da) + " " + act.meteo.getOsservazione().get(0).getWindDirectionString() + "\n" + act.getString(R.string.updated) + " " + act.aggiornamentoMeteo.get(Calendar.DAY_OF_MONTH) + "/" + (1 + act.aggiornamentoMeteo.get(Calendar.MONTH)) + "/" + act.aggiornamentoMeteo.get(Calendar.YEAR) + " " + act.getString(R.string.ore) + " " + act.aggiornamentoMeteo.get(Calendar.HOUR_OF_DAY) + ":" + act.aggiornamentoMeteo.get(Calendar.MINUTE));
                    //String s = getString(R.string.condimeteo) + meteo.getOsservazione().get(0).getWindBeaufortString(act) + " (" + meteo.getOsservazione().get(0).getWindKmh().intValue() + " km/h) " + getString(R.string.da) + " " + meteo.getOsservazione().get(0).getWindDirectionString() + "\n" + getString(R.string.updated) + " " + aggiornamentoMeteo.get(Calendar.DAY_OF_MONTH) + "/" + (1 + aggiornamentoMeteo.get(Calendar.MONTH)) + "/" + aggiornamentoMeteo.get(Calendar.YEAR) + " " + getString(R.string.ore) + " " + aggiornamentoMeteo.get(Calendar.HOUR_OF_DAY) + ":" + aggiornamentoMeteo.get(Calendar.MINUTE);
                    String s = "";
                    s += act.getString(R.string.condimeteo) + act.meteo.getOsservazione().get(0).getWindBeaufortString(act) + " (" + act.meteo.getOsservazione().get(0).getWindKmh().intValue() + " km/h) " + act.getString(R.string.da) + " " + act.meteo.getOsservazione().get(0).getWindDirectionString() + "\n";
                    s += act.getString(R.string.updated) + " " + act.meteo.getOsservazione().get(0).getTempo().get(Calendar.DAY_OF_MONTH) + "/" + (1 + act.meteo.getOsservazione().get(0).getTempo().get(Calendar.MONTH)) + "/" + act.meteo.getOsservazione().get(0).getTempo().get(Calendar.YEAR) + " " + act.getString(R.string.ore) + " " + act.meteo.getOsservazione().get(0).getTempo().get(Calendar.HOUR_OF_DAY) + ":" + act.meteo.getOsservazione().get(0).getTempo().get(Calendar.MINUTE) + "\n";

                    s += act.getString(R.string.previsioni) + "\n";
                    for (int i = 1; i < 8; i++) {
                        //s+=act.meteo.getOsservazione().get(i).getWindBeaufortString(act) + " (" + act.meteo.getOsservazione().get(i).getWindKmh().intValue() + " km/h) " + act.getString(R.string.da) + " " + act.meteo.getOsservazione().get(i).getWindDirectionString();
                        //PIUU BREVE
                        s += act.meteo.getOsservazione().get(i).getWindKmh().intValue() + " km/h " + act.getString(R.string.da) + " " + act.meteo.getOsservazione().get(i).getWindDirectionString();
                        s += " " + act.getString(R.string.alleOre) + " " + act.meteo.getOsservazione().get(i).getTempo().get(Calendar.HOUR_OF_DAY) + "\n";
                    }
                    act.meteoDialog.setMessage(s);

                }
            }


            act.aggiornaLista();
            act.setMsgToast();
            Log.d("ORARI", "Terminato aggiornamento orari su GUI");
            //gli orari del web erano piu' aggiornati
            //bisogna aggiornare la GUI
        }
        Log.d("TEST", "Eseguita post execution dopo il task meteo");
        //showNotification("Downloaded " + result + " bytes");
    }
}
