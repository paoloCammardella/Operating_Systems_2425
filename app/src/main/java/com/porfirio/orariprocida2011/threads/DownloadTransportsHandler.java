
package com.porfirio.orariprocida2011.threads;

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
 * TODO Migrare da lettura da file a lettura da risorsa Firebase
 * TODO Mantenere possibilmente la lettura da file locale per la modalità offline
 * TODO Aggiornare la lettura asincrona aggiornando i metodi deprecati
 */

public class DownloadTransportsHandler extends AsyncTask<Void, Integer, Boolean> {
    final OrariProcida2011Activity act;

    //dichiarazione del semaforo
    public static Semaphore taskDownload;
    public static Semaphore taskDownloadStart;
    private Calendar UpdateWebTimes;
    private ArrayList<Mezzo> trasportList;
    private Calendar updateTimesIS = null;

    public DownloadTransportsHandler(OrariProcida2011Activity orariProcida2011Activity) {
        act = orariProcida2011Activity;
        trasportList = new ArrayList<Mezzo>();
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
            String updateLine = r.readLine();
            String newsLine = "";
            StringTokenizer st0 = new StringTokenizer(updateLine, ",");
            UpdateWebTimes = (Calendar) act.updateTimesIS.clone();
            UpdateWebTimes.set(Calendar.DAY_OF_MONTH, Integer.parseInt(st0.nextToken()));
            UpdateWebTimes.set(Calendar.MONTH, Integer.parseInt(st0.nextToken()));
            UpdateWebTimes.set(Calendar.YEAR, Integer.parseInt(st0.nextToken()));

            if (!(UpdateWebTimes.after(act.updateTimesIS))) {
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

                HttpURLConnection conn2;
                InputStream in2;
                try {
                    conn2 = (HttpURLConnection) new URL(act.getApplicationContext().getString(R.string.urlNews)).openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    in2 = conn2.getInputStream();
                    BufferedReader r2 = new BufferedReader(new InputStreamReader(in2));
                    newsLine = r2.readLine();
                    if (!(Locale.getDefault().getLanguage().contentEquals("it"))) //se non ? italiano legge la seconda riga delle novita
                        newsLine = r2.readLine();
                    r2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Scrive gli orari web sul file interno
                FileOutputStream fos = act.openFileOutput("orari.csv", Context.MODE_PRIVATE);
                fos.write(updateLine.getBytes());
                fos.write("\n".getBytes());
                trasportList.clear();
                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    Log.d("RIGA", line);
                    StringTokenizer st = new StringTokenizer(line, ",");
                    trasportList.add(new Mezzo(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken()));
                    fos.write(line.getBytes());
                    fos.write("\n".getBytes());
                }
                act.mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("App Event")
                        .setAction("Updated Timetable")
                        .build());
                fos.close();
                updateTimesIS = act.updateWebTimes;


            }
            r.close();
            Log.d("ORARI", "Orari web letti");
            Log.d("ORARI", "Orari IS aggiornati");


        } catch (SocketTimeoutException e) {
            e.printStackTrace();
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
            if (UpdateWebTimes != null)
                act.updateWebTimes = (Calendar) UpdateWebTimes.clone();
            else {
                act.updateWebTimes = (Calendar.getInstance());
                act.updateWebTimes.set(2001, 1, 1);
            }
            act.ultimaLetturaOrariDaWeb = Calendar.getInstance();
            int meseToast = act.updateTimesIS.get(Calendar.MONTH);
            if (meseToast == 0) meseToast = 12;
            String str = act.getString(R.string.orariAggiornatiAl) + " " + act.updateWebTimes.get(Calendar.DAY_OF_MONTH) + "/" + meseToast + "/" + act.updateWebTimes.get(Calendar.YEAR);
            act.aboutDialog.setMessage("" + act.getString(R.string.disclaimer) + "\n" + act.getString(R.string.credits));
            Log.d("ORARI", str);
            if (updateTimesIS != null)
                act.updateTimesIS = (Calendar) updateTimesIS.clone();
            //TODO: Ho aggiornato la listMezzi locale; ora devo propagare a quella globale e scatenare il refresh

            act.transportList.clear();
            act.transportList.addAll(trasportList);
            act.aggiornaLista();
            act.setMsgToast();


            Log.d("ORARI", "Terminata lettura orari da web");
            Log.d("ORARI", "Terminato aggiornamento orari su GUI");
            //gli orari del web erano piu' aggiornati
            //bisogna aggiornare la GUI
        }
        Log.d("TEST", "Eseguita post execution dopo il task download");
    }
}

