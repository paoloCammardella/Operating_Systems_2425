package com.porfirio.orariprocida2011.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Mezzo;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Porfirio on 16/02/2018.
 */

public class DownloadMezziTask extends AsyncTask<Void, Integer, Boolean> {
    final OrariProcida2011Activity act;
    //int delay = 0;

    //dichiarazione del semaforo
    public static Semaphore taskDownload;
    public static Semaphore taskDownloadStart;
    private Calendar aggiornamentoOrariWeb;
    private ArrayList<Mezzo> listMezzi;
    private Calendar aggiornamentoOrariIS = null;

    public DownloadMezziTask(OrariProcida2011Activity orariProcida2011Activity) {
        act = orariProcida2011Activity;
        listMezzi = new ArrayList<Mezzo>();
        //delay = TestSuiteAS.getDelay(DownloadMezziTask.class.toString());


    }


    // Do the long-running work in here
    protected Boolean doInBackground(Void... params) {
        if (taskDownloadStart != null) {
            try {
                taskDownloadStart.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("TEST", "TASK: Inizia il task download");
            taskDownloadStart.release();
        }
/*
        if (delay > 0 || true) {
            Log.d("TEST", "Started Delay a " + System.currentTimeMillis() % 100000);
            for (long i = 0; i < (long) 500000 * 10000; i++) ;
            Log.d("TEST", "FInished Delay a " + System.currentTimeMillis() % 100000);
        }
*/

        //act=activities[0];
        act.mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("Download Mezzi Task")
                .build());
        //Apre una connessione con gli orari
        URL u = null;
        try {
            u = new URL(act.getApplicationContext().getString(R.string.urlOrari));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection conn;
        InputStream in;
        try {
            //conn = Connection.connect(new URL(url));
            assert u != null;
            conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(5000);
        } catch (SocketTimeoutException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            in = conn.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String rigaAggiornamento = r.readLine();
            String rigaNovita = "";
            StringTokenizer st0 = new StringTokenizer(rigaAggiornamento, ",");
            aggiornamentoOrariWeb = (Calendar) act.aggiornamentoOrariIS.clone();
            aggiornamentoOrariWeb.set(Calendar.DAY_OF_MONTH, Integer.parseInt(st0.nextToken()));
            aggiornamentoOrariWeb.set(Calendar.MONTH, Integer.parseInt(st0.nextToken()));
            aggiornamentoOrariWeb.set(Calendar.YEAR, Integer.parseInt(st0.nextToken()));
            //if (!primoAvvio)
            //Toast.makeText(act.getApplicationContext(), str, Toast.LENGTH_LONG).show();
            if (!(aggiornamentoOrariWeb.after(act.aggiornamentoOrariIS))) {
                //Wait prima della terminazione del task
                Log.d("TEST", "TASK: Il task download pronto a terminare");

                if (taskDownload != null) {
                    try {
                        if (!taskDownload.tryAcquire(15L, TimeUnit.SECONDS)) {
                            Log.d("TEST", "TASK: TIMEOUT task download");
                            act.finish();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Log.d("TEST", "TASK: Finisce il task download");
                    taskDownload.release();
                    Log.d("ORDER", "Download task");
                }
                return false;
            }
            else {
                Log.d("ORARI", "GLi orari dal Web sono piu' aggiornati");

                //legge riga novita da novita.csv
                HttpURLConnection conn2;
                InputStream in2;
                try {
                    conn2 = (HttpURLConnection) new URL(act.getApplicationContext().getString(R.string.urlNovita)).openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    in2 = conn2.getInputStream();
                    BufferedReader r2 = new BufferedReader(new InputStreamReader(in2));
                    rigaNovita = r2.readLine();
                    if (!(Locale.getDefault().getLanguage().contentEquals("it"))) //se non ? italiano legge la seconda riga delle novita
                        rigaNovita = r2.readLine();
                    r2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Scrive gli orari web sul file interno
                FileOutputStream fos = act.openFileOutput("orari.csv", Context.MODE_PRIVATE);
                fos.write(rigaAggiornamento.getBytes());
                fos.write("\n".getBytes());
                listMezzi.clear();
                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    //esamino la riga e creo un mezzo
                    Log.d("RIGA", line);
                    StringTokenizer st = new StringTokenizer(line, ",");
                    listMezzi.add(new Mezzo(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken()));
                    fos.write(line.getBytes());
                    fos.write("\n".getBytes());
                }
                act.mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("App Event")
                        .setAction("Updated Timetable")
                        .build());
                fos.close();
                aggiornamentoOrariIS = act.aggiornamentoOrariWeb;


            }
            r.close();
            Log.d("ORARI", "Orari web letti");
            //if (!primoAvvio)
            //	Toast.makeText(getApplicationContext(), ""+getString(R.string.aggiornamentoDaWeb), Toast.LENGTH_LONG).show();
            Log.d("ORARI", "Orari IS aggiornati");


        } catch (SocketTimeoutException e) {
            //Toast.makeText(act.getApplicationContext(), act.getString(R.string.connessioneLenta), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (taskDownload != null) {
            //Wait prima della terminazione del task
            Log.d("TEST", "TASK: Il task download è pronto a terminare");

            try {
                if (!taskDownload.tryAcquire(15L, TimeUnit.SECONDS)) {
                    Log.d("TEST", "TASK: TIMEOUT task download");
                    act.finish();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d("TEST", "TASK: Finisce il task download");
            taskDownload.release();
            Log.d("ORDER", "Download task");
        }
        act.mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("Download Terminated")
                .build());
        return true;
    }


    // This is called each time you call publishProgress()
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }


    // This is called when doInBackground() is finished
    protected void onPostExecute(Boolean result) {
        if (result){
            if (aggiornamentoOrariWeb != null)
                act.aggiornamentoOrariWeb = (Calendar) aggiornamentoOrariWeb.clone();
            else {
                act.aggiornamentoOrariWeb = (Calendar.getInstance());
                act.aggiornamentoOrariWeb.set(2001, 1, 1);
            }
            act.ultimaLetturaOrariDaWeb = Calendar.getInstance();
            int meseToast = act.aggiornamentoOrariIS.get(Calendar.MONTH);
            if (meseToast == 0) meseToast = 12;
            String str = act.getString(R.string.orariAggiornatiAl) + " " + act.aggiornamentoOrariWeb.get(Calendar.DAY_OF_MONTH) + "/" + meseToast + "/" + act.aggiornamentoOrariWeb.get(Calendar.YEAR);
            act.aboutDialog.setMessage("" + act.getString(R.string.disclaimer) + "\n" + act.getString(R.string.credits));
            Log.d("ORARI", str);
            if (aggiornamentoOrariIS != null)
                act.aggiornamentoOrariIS = (Calendar) aggiornamentoOrariIS.clone();
            //TODO: Ho aggiornato la listMezzi locale; ora devo propagare a quella globale e scatenare il refresh

            act.listMezzi.clear();
            act.listMezzi.addAll(listMezzi);
            act.aggiornaLista();
            act.setMsgToast();


            Log.d("ORARI", "Terminata lettura orari da web");
            Log.d("ORARI", "Terminato aggiornamento orari su GUI");
            //gli orari del web erano piu' aggiornati
            //bisogna aggiornare la GUI
        }
        //showNotification("Downloaded " + result + " bytes");
        Log.d("TEST", "Eseguita post execution dopo il task download");
    }


}