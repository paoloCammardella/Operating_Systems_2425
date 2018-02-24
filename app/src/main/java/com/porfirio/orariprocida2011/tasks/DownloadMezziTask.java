package com.porfirio.orariprocida2011.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by Porfirio on 16/02/2018.
 */

public class DownloadMezziTask extends AsyncTask<Void, Integer, Boolean> {
    private final OrariProcida2011Activity act;

    public DownloadMezziTask(OrariProcida2011Activity orariProcida2011Activity) {
        act = orariProcida2011Activity;
    }

    // Do the long-running work in here
    protected Boolean doInBackground(Void... params) {
        //act=activities[0];

        //Apre una connessione con gli orari
        URL u = null;
        try {
            u = new URL(act.urlOrari);
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
            act.aggiornamentoOrariWeb = (Calendar) act.aggiornamentoOrariIS.clone();
            act.aggiornamentoOrariWeb.set(Calendar.DAY_OF_MONTH, Integer.parseInt(st0.nextToken()));
            act.aggiornamentoOrariWeb.set(Calendar.MONTH, Integer.parseInt(st0.nextToken()));
            act.aggiornamentoOrariWeb.set(Calendar.YEAR, Integer.parseInt(st0.nextToken()));
            act.ultimaLetturaOrariDaWeb = Calendar.getInstance();
            int meseToast = act.aggiornamentoOrariIS.get(Calendar.MONTH);
            if (meseToast == 0) meseToast = 12;
            String str = act.getString(R.string.orariAggiornatiAl) + " " + act.aggiornamentoOrariWeb.get(Calendar.DAY_OF_MONTH) + "/" + meseToast + "/" + act.aggiornamentoOrariWeb.get(Calendar.YEAR);
            act.aboutDialog.setMessage("" + act.getString(R.string.disclaimer) + "\n" + act.getString(R.string.credits));
            Log.d("ORARI", str);
            //if (!primoAvvio)
            //Toast.makeText(act.getApplicationContext(), str, Toast.LENGTH_LONG).show();
            if (!(act.aggiornamentoOrariWeb.after(act.aggiornamentoOrariIS)))
                return false;
            else {
                Log.d("ORARI", "GLi orari dal Web sono piu' aggiornati");

                //legge riga novita da novita.csv
                HttpURLConnection conn2;
                InputStream in2;
                try {
                    conn2 = (HttpURLConnection) new URL(act.urlNovita).openConnection();
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
                act.listMezzi.clear();
                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    //esamino la riga e creo un mezzo
                    StringTokenizer st = new StringTokenizer(line, ",");
                    act.listMezzi.add(new Mezzo(act.getApplicationContext(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken()));
                    fos.write(line.getBytes());
                    fos.write("\n".getBytes());
                }
                fos.close();
                act.aggiornamentoOrariIS = act.aggiornamentoOrariWeb;


                // Aggiunto un messaggio che ricordi l'aggiornamento
                    /*
                    AlertDialog.Builder builder = new AlertDialog.Builder(act);
                    builder.setMessage(act.getString(R.string.nuovoAggiornamento) + " " + rigaAggiornamento.replace(',', '/') + " \n " + act.getString(R.string.novita) + " \n" + rigaNovita)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //dialog.cancel();
                                }
                            });
                    act.novitaDialog = builder.create();
                    act.novitaDialog.show();
                    */


            }
            r.close();
            Log.d("ORARI", "Orari web letti");
            //if (!primoAvvio)
            //	Toast.makeText(getApplicationContext(), ""+getString(R.string.aggiornamentoDaWeb), Toast.LENGTH_LONG).show();
            Log.d("ORARI", "Orari IS aggiornati");

        } catch (SocketTimeoutException e) {
            Toast.makeText(act.getApplicationContext(), act.getString(R.string.connessioneLenta), Toast.LENGTH_LONG).show();
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
        if (result){
            Log.d("ORARI", "Terminata lettura orari da web");
            act.aggiornaLista();
            Log.d("ORARI", "Terminato aggiornamento orari su GUI");
            //gli orari del web erano piu' aggiornati
            //bisogna aggiornare la GUI
        }
        //showNotification("Downloaded " + result + " bytes");
    }
}