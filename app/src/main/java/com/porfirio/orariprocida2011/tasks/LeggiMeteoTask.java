package com.porfirio.orariprocida2011.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;


/**
 * Created by Porfirio on 17/02/2018.
 */

public class LeggiMeteoTask extends AsyncTask<OrariProcida2011Activity, Integer, Boolean> {
    private OrariProcida2011Activity act;

    // Do the long-running work in here
    protected Boolean doInBackground(OrariProcida2011Activity... activities) {
        act = activities[0];

    /* Create a URL we want to load some xml-data from. */
        URL url;
        Double windKmhFromIS = 0.0;
        Integer windDirFromIS = 0;
        String windDirectionStringFromIS = act.getString(R.string.nord);
        boolean scriviSuIS = false;
        //Prova a leggere da Internal Storage il valore di aggiornamentoMeteoIS
        FileInputStream fstream = null;
        try {
            Log.i("k", act.getApplicationContext().getFilesDir().getPath());
            fstream = new FileInputStream(act.getApplicationContext().getFilesDir().getPath() + "/aggiornamentoMeteo.csv");
        } catch (FileNotFoundException e1) {
            scriviSuIS = true;
            //metto fittiziamente aggiornamento al 2001
            act.aggiornamentoMeteo = Calendar.getInstance(TimeZone.getDefault());
            act.aggiornamentoMeteo.set(Calendar.YEAR, 2001);
        }

        if (!(fstream == null)) {
            Log.d("ORARI", "legge da IS il valore di aggiornamentoMeteoIS");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String rigaAggiornamento = null;
            try {
                rigaAggiornamento = br.readLine();
            } catch (IOException e) {
                //
                e.printStackTrace();
            }
            Log.d("ORARI", "aggiornamento al " + rigaAggiornamento);
            StringTokenizer st0 = new StringTokenizer(rigaAggiornamento, ",");
            act.aggiornamentoMeteo = Calendar.getInstance(TimeZone.getDefault());
            act.aggiornamentoMeteo.set(Calendar.DAY_OF_MONTH, Integer.parseInt(st0.nextToken()));
            act.aggiornamentoMeteo.set(Calendar.MONTH, Integer.parseInt(st0.nextToken()));
            act.aggiornamentoMeteo.set(Calendar.YEAR, Integer.parseInt(st0.nextToken()));
            act.aggiornamentoMeteo.set(Calendar.HOUR_OF_DAY, Integer.parseInt(st0.nextToken()));
            act.aggiornamentoMeteo.set(Calendar.MINUTE, Integer.parseInt(st0.nextToken()));
            try {
                String s = br.readLine();
                windKmhFromIS = Double.parseDouble(s);
                act.meteo.setWindKmh(windKmhFromIS);
                s = br.readLine();
                windDirFromIS = Integer.parseInt(s);
                act.meteo.setWindDirection(windDirFromIS);
                s = br.readLine();
                windDirectionStringFromIS = s;
                act.meteo.setWindDirectionString(windDirectionStringFromIS);

            } catch (NumberFormatException | IOException e1) {
                //
                e1.printStackTrace();
                return false;
            }
            try {
                in.close();
            } catch (IOException e) {
                //
                e.printStackTrace();
                return false;
            }
        }


        Long differenza = Calendar.getInstance().getTimeInMillis() - act.aggiornamentoMeteo.getTimeInMillis();
        Log.d("ORARI", "vecchiaia dell'aggiornamento in millisec " + differenza.toString());
        if (act.isOnline() && (differenza > 10000000 || act.aggiorna)) //Valuta un nuovo meteo ogni 10000 secondi (quasi tre ore)
            //if (isOnline() && differenza>10000) //Valuta un nuovo meteo ogni 10000 secondi (quasi tre ore)
//            if (true) {
                try {
                    JSONObject jsonObject = null;
                    try {
                        //jsonObject = readJsonFromUrl("http://api.wunderground.com/api/7a2bedc35ab44ecb/geolookup/conditions/q/IA/Procida.json");
                        //jsonObject = readJsonFromUrl("http://api.wunderground.com/api/7a2bedc35ab44ecb/geolookup/conditions/q/IA/Pozzuoli.json");

                        InputStream is = new URL("http://api.wunderground.com/api/7a2bedc35ab44ecb/geolookup/conditions/q/IA/Pozzuoli.json").openStream();
                        try {
                            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                            String jsonText = OrariProcida2011Activity.readAll(rd);
                            jsonObject = new JSONObject(jsonText);
                        } finally {
                            is.close();
                        }

                    } catch (SocketTimeoutException e) {
                        //Toast.makeText(act.getApplicationContext(), act.getString(R.string.connessioneLenta), Toast.LENGTH_LONG).show();
                        return false;
                    } catch (IOException e) {
                        //
                        Log.d("ORARI", "dati meteo non caricati da web");
                        return false;
                    }
//Grazie alla formattazione ottenuta con http://jsonformatter.curiousconcept.com/
                    if (!(jsonObject == null)) {

                        Integer windDir = (Integer) jsonObject.getJSONObject("current_observation").get("wind_degrees");
                        act.meteo.setWindKmh(Double.parseDouble(jsonObject.getJSONObject("current_observation").get("wind_kph").toString()));
                        act.meteo.setWindDirection((int) (45 * (Math.round(windDir / 45.0))) % 360);
                        act.meteo.setWindDirectionString(act.meteo.getWindDirection());
                        act.meteo.setWindBeaufort(act.meteo.getWindKmh());
                        Log.d("ORARI", "letto da json");
                        //scrivo l'aggiornamento su internal storage
                        FileOutputStream fos = null;
                        try {
                            fos = act.openFileOutput("aggiornamentoMeteo.csv", Context.MODE_PRIVATE);
                        } catch (FileNotFoundException e) {
                            //
                            e.printStackTrace();
                        }

                        //TODO: se il valore letto da weather underground ? troppo piccolo (oppure ? intero anzich? string) rivolgiti a openweathermap
                        //esempio di query json verso openweathermap:
                        //http://api.openweathermap.org/data/2.5/weather?q=Procida,it&lang=it

                        //Pare che il problema sia nei dati di weather underground, che li prende da procidameteo ... per ora setto come riferimento pozzuoli

                        //aggiornamento del file locale con il dato meteo
                        act.aggiornamentoMeteo = Calendar.getInstance();
                        String rigaAggiornamento = act.aggiornamentoMeteo.get(Calendar.DAY_OF_MONTH) + "," + act.aggiornamentoMeteo.get(Calendar.MONTH) + "," + act.aggiornamentoMeteo.get(Calendar.YEAR) + "," + act.aggiornamentoMeteo.get(Calendar.HOUR_OF_DAY) + "," + act.aggiornamentoMeteo.get(Calendar.MINUTE);
                        try {
                            assert fos != null;
                            fos.write(rigaAggiornamento.getBytes());
                            fos.write("\n".getBytes());
                            fos.write(act.meteo.getWindKmh().toString().getBytes());
                            fos.write("\n".getBytes());
                            fos.write(String.valueOf(act.meteo.getWindDirection()).getBytes());
                            fos.write("\n".getBytes());
                            fos.write(act.meteo.getWindDirectionString().getBytes());
                            fos.write("\n".getBytes());
                            fos.close();
                        } catch (IOException e) {
                            //
                            e.printStackTrace();
                        }

                        System.out.println("");
                    }
                } catch (JSONException e) {
                    //
                    Log.d("ORARI", "dati meteo non caricati da web");
                    if (act.isOnline()) Log.d("ORARI", "per mancanza di connessione");
                    else Log.d("ORARI", "perche' ho dati abbastanza aggiornati");
                }

// COMMENTATO IL VECCHIO CODICE CHE LEGGEVA DATI METEO DA GOOGLE
//			try {
//				url = new URL("http://www.google.com/ig/api?weather=Procida");
//
//
//				/* Get a SAXParser from the SAXPArserFactory. */
//				SAXParserFactory spf = SAXParserFactory.newInstance();
//				SAXParser sp = spf.newSAXParser();
//
//				/* Get the XMLReader of the SAXParser we created. */
//				XMLReader xr = sp.getXMLReader();
//				/* Create a new ContentHandler and apply it to the XML-Reader*/
//				MeteoXMLHandler meteoXMLHandler = new MeteoXMLHandler(this);
//				xr.setContentHandler(meteoXMLHandler);
//
//				/* Parse the xml-data from our URL. */
//				xr.parse(new InputSource(url.openStream()));
//				/* Parsing has finished. */
//
//			} catch (MalformedURLException e) {
//				e.printStackTrace();
//			} catch (ParserConfigurationException e) {
//				e.printStackTrace();
//			} catch (SAXException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
// FINE VECCHIO CODICE
/*            } else {
                Log.d("ORARI", "dati meteo sufficientemente aggiornati o non disponibili da web");
                // Usa come meteo i dati da IS (o fittizi)
                act.meteo.setWindKmh(windKmhFromIS);
                act.meteo.setWindDirection((int) (45 * (Math.round(windDirFromIS / 45.0))) % 360);
                act.meteo.setWindDirectionString(windDirectionStringFromIS);
                act.meteo.setWindBeaufort(windKmhFromIS);
            }
*/
        return true;
    }

    // This is called each time you call publishProgress()
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }


    // This is called when doInBackground() is finished
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.d("CONDIMETEO", "AVVIO" + act.getString(R.string.condimeteo) + act.meteo.getWindBeaufortString() + " (" + act.meteo.getWindKmh().intValue() + " km/h) " + act.getString(R.string.da) + " " + act.meteo.getWindDirectionString() + "\n" + act.getString(R.string.updated) + " " + act.aggiornamentoMeteo.get(Calendar.DAY_OF_MONTH) + "/" + (1 + act.aggiornamentoMeteo.get(Calendar.MONTH)) + "/" + act.aggiornamentoMeteo.get(Calendar.YEAR) + " " + act.getString(R.string.ore) + " " + act.aggiornamentoMeteo.get(Calendar.HOUR_OF_DAY) + ":" + act.aggiornamentoMeteo.get(Calendar.MINUTE));

            act.meteoDialog.setMessage(act.getString(R.string.condimeteo) + act.meteo.getWindBeaufortString() + " (" + act.meteo.getWindKmh().intValue() + " km/h) " + act.getString(R.string.da) + " " + act.meteo.getWindDirectionString()
                    + "\n" + act.getString(R.string.updated) + " " + act.aggiornamentoMeteo.get(Calendar.DAY_OF_MONTH) + "/" + (1 + act.aggiornamentoMeteo.get(Calendar.MONTH)) + "/" + act.aggiornamentoMeteo.get(Calendar.YEAR) + " " + act.getString(R.string.ore) + " " + act.aggiornamentoMeteo.get(Calendar.HOUR_OF_DAY) + ":" + act.aggiornamentoMeteo.get(Calendar.MINUTE));

            act.aggiornaLista();
            Log.d("ORARI", "Terminato aggiornamento orari su GUI");
            //gli orari del web erano piu' aggiornati
            //bisogna aggiornare la GUI
        }
        //showNotification("Downloaded " + result + " bytes");
    }
}
