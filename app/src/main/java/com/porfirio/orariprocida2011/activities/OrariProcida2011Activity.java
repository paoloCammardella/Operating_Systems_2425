package com.porfirio.orariprocida2011.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.porfirio.orariprocida2011.AnalyticsApplication;
import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.dialogs.DettagliMezzoDialog;
import com.porfirio.orariprocida2011.dialogs.SegnalazioneDialog;
import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.entity.Meteo;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.obsolete.ConfigData;
import com.porfirio.orariprocida2011.tasks.DownloadMezziTask;
import com.porfirio.orariprocida2011.tasks.LeggiMeteoTask;
import com.porfirio.orariprocida2011.tasks.LeggiSegnalazioniTask;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class OrariProcida2011Activity extends FragmentActivity {
    private static FragmentManager fm;

    //public final String urlOrari = "http://wpage.unina.it/ptramont/orari.csv";
    //public final String urlNovita = "http://wpage.unina.it/ptramont/novita.txt";
    public Calendar c;
    public Calendar aggiornamentoOrariWeb;
    public Calendar aggiornamentoOrariIS;
    // Introdotto il concetto di ultima lettura degli orari da Web
    public Calendar ultimaLetturaOrariDaWeb;
    public Calendar aggiornamentoMeteo;
	public AlertDialog aboutDialog;
    public Meteo meteo;
    public AlertDialog meteoDialog;
    public ArrayList<Mezzo> listMezzi;
    public boolean aggiorna;
    private String[] ragioni = new String[100];

    private String nave;
    private String portoPartenza;
    private String portoArrivo;
    private ArrayAdapter<String> aalvMezzi;
    private TextView txtOrario;
    //public AlertDialog novitaDialog;
    private ConfigData configData;
    private DettagliMezzoDialog dettagliMezzoDialog;
    private ArrayList<Mezzo> selectMezzi;
    private ArrayList<Compagnia> listCompagnia;
    private LocationManager myManager;
    private String BestProvider;
    private Locale locale;
    private SegnalazioneDialog segnalazioneDialog;
    private boolean primoAvvio = true;
    public Tracker mTracker;
    private OrariProcida2011Activity act;
    public String msgToast;

    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

/*    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } finally {
            is.close();
        }
    }*/

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//    //language hack here.
//        if (!((Locale.getDefault().getLanguage().contentEquals("en"))||	(Locale.getDefault().getLanguage().contentEquals("it"))))
//    	{
//    	String languageToLoad  = "en";
//    	locale = new Locale(languageToLoad);
//    	Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config,
//        getBaseContext().getResources().getDisplayMetrics());
//    }
//    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("UI Event")
                .setAction("Open Menu")
                .build());
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.about:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UI Event")
                        .setAction("About")
                        .build());
                aboutDialog.show();
                //showDialog(ABOUT_DIALOG_ID);
                return true;
            // cambiata semantica pulsante: se scelgo, allora carico esplicitamente da web
            case R.id.updateWeb:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UI Event")
                        .setAction("Update Orari da Web da Menu")
                        .build());
                // Caricare da Web
                if (isOnline()) {
                    riempiMezzidaWeb();
                    if (aggiornamentoOrariWeb != null) {
                        int meseToast = aggiornamentoOrariWeb.get(Calendar.MONTH);
                        if (meseToast == 0) meseToast = 12;
                        if (!primoAvvio)
                            Toast.makeText(getApplicationContext(), getString(R.string.orariAggiornatiAl) + " " + aggiornamentoOrariWeb.get(Calendar.DATE) + "/" + meseToast + "/" + aggiornamentoOrariWeb.get(Calendar.YEAR), Toast.LENGTH_LONG).show();
                        //TODO: Forzare aggiornamento
                    }
                } else
                    Log.d("ORARI", "Non c'? la connessione: non carico orari da Web");
                return true;
            case R.id.meteo:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UI Event")
                        .setAction("Update Meteo da Menu")
                        .build());
                leggiMeteo(true);
                if (aggiornamentoMeteo!=null){
                    String s = getString(R.string.condimeteo) + meteo.getWindBeaufortString() + " (" + meteo.getWindKmh().intValue() + " km/h) " + getString(R.string.da) + " " + meteo.getWindDirectionString() + "\n" + getString(R.string.updated) + " " + aggiornamentoMeteo.get(Calendar.DAY_OF_MONTH) + "/" + (1 + aggiornamentoMeteo.get(Calendar.MONTH)) + "/" + aggiornamentoMeteo.get(Calendar.YEAR) + " " + getString(R.string.ore) + " " + aggiornamentoMeteo.get(Calendar.HOUR_OF_DAY) + ":" + aggiornamentoMeteo.get(Calendar.MINUTE);
                    meteoDialog.setMessage(s);
                    meteoDialog.show();
                    //TODO: Forzare aggiornamento
                }
                    this.aggiornaLista();

                return true;
            case R.id.esci:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UI Event")
                        .setAction("Exit da Menu")
                        .build());
                //aboutDialog.show();

                OrariProcida2011Activity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // Attiva funzioni display.
        mTracker.enableAdvertisingIdCollection(true);

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("onCreate")
                .build());

        fm = getSupportFragmentManager();
        act = this;

        if (ActivityCompat.checkSelfPermission(OrariProcida2011Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("App Event")
                    .setAction("Request Permission")
                    .build());
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET}, 1
            );
        }

        //TODO: Necessario?
        if (!((Locale.getDefault().getLanguage().contentEquals("en")) || (Locale.getDefault().getLanguage().contentEquals("it")))) {
            String languageToLoad = "en";
            locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
        Log.d("ACTIVITY", "create");
        //TODO: Sempre necessario?
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);

        myManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        BestProvider = myManager.getBestProvider(criteria, true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.disclaimer) + "\n" + getString(R.string.credits))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        aboutDialog = builder.create();


        ragioni = getResources().getStringArray(R.array.strRagioni);

        meteo = new Meteo(this);
        leggiMeteo(false);
        //TODO: Forzare aggiornamento

        //TODO: Problema: leggiMeteo non e' piu' bloccante, quindi bisogna togliere il meteo dal primo messaggio e aggiungerlo quando e' il momento

        builder = new AlertDialog.Builder(this);
/*        builder.setMessage(getString(R.string.condimeteo) + meteo.getWindBeaufortString() + " (" + meteo.getWindKmh().intValue() + " km/h) " + getString(R.string.da) + " " + meteo.getWindDirectionString()
                + "\n" + getString(R.string.updated) + " " + aggiornamentoMeteo.get(Calendar.DAY_OF_MONTH) + "/" + (1 + aggiornamentoMeteo.get(Calendar.MONTH)) + "/" + aggiornamentoMeteo.get(Calendar.YEAR) + " " + getString(R.string.ore) + " " + aggiornamentoMeteo.get(Calendar.HOUR_OF_DAY) + ":" + aggiornamentoMeteo.get(Calendar.MINUTE))
*/
        builder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        meteoDialog = builder.create();

        configData = new ConfigData();
        configData.setFinestraTemporale();

        // get the current time

        c = Calendar.getInstance(TimeZone.getDefault());

        txtOrario = findViewById(R.id.txtOrario);

		setTxtOrario(c);

        Button buttonMinusMinus = findViewById(R.id.btnConfermaOSmentisci);
        buttonMinusMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UI Event")
                        .setAction("Button --")
                        .build());
                c.add(Calendar.HOUR, -1);
                setTxtOrario(c);
                aggiornaLista();
            }
        });

        Button buttonMinus = findViewById(R.id.button2);
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UI Event")
                        .setAction("Button -")
                        .build());
                c.add(Calendar.MINUTE, -15);
                setTxtOrario(c);
                aggiornaLista();
            }
        });

        Button buttonPlus = findViewById(R.id.button3);
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO L'unico problema residuo ? che problemi correttamente segnalati con pi? di 24 ore di anticipo vengono visualizzati solo a meno di 24h
                //Forse potrebbe essere risolto forzando un refresh quando si avanza di 24h rispetto all'orario corrente
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UI Event")
                        .setAction("Button +")
                        .build());
                c.add(Calendar.MINUTE, 15);
                setTxtOrario(c);
                aggiornaLista();
            }
        });

        Button buttonPlusPlus = findViewById(R.id.button4);
        buttonPlusPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//        		orario.setHours(orario.getHours()+1);
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UI Event")
                        .setAction("Button ++")
                        .build());
                c.add(Calendar.HOUR, 1);
                setTxtOrario(c);
                aggiornaLista();
            }
        });
        // spostato in avanti setSpinner();


        listMezzi = new ArrayList<>();
        ListView lvMezzi = findViewById(R.id.listMezzi);
        aalvMezzi = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lvMezzi.setAdapter(aalvMezzi);

        dettagliMezzoDialog = new DettagliMezzoDialog();
        dettagliMezzoDialog.setDettagliMezzoDialog(fm, this, this, c);
        segnalazioneDialog = new SegnalazioneDialog();
        lvMezzi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //listener sul click di un item della lista

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UI Event")
                        .setAction("Click Dettagli Mezzo")
                        .build());
                //aboutDialog.show();

                for (int i = 0; i < aalvMezzi.getCount(); i++) {
                    if (selectMezzi.get(i).getOrderInList() == arg2)
                        dettagliMezzoDialog.setMezzo(selectMezzi.get(i));
                }


//				problema: clicco sulla lista ma ho solo la stringa, non il mezzo corrispondente
//				soluzione: mantenere una variabile ordine che abbini lvMezzi con Mezzi
//				altra soluzione: trovare il mezzo dalla stringa
                //dettagliMezzoDialog.fill(listCompagnia);
                dettagliMezzoDialog.setListCompagnia(listCompagnia);
                dettagliMezzoDialog.show(fm, "fragment_edit_name");

            }


        });
        lvMezzi.setLongClickable(true);
        lvMezzi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, long arg3) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UI Event")
                        .setAction("LongClick DettagliMezzo")
                        .build());

                if (!isOnline())
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.soloOnline), Toast.LENGTH_SHORT).show();
                else {
                    for (int i = 0; i < aalvMezzi.getCount(); i++) {
                        if (selectMezzi.get(i).getOrderInList() == arg2)
                            segnalazioneDialog.setMezzo(selectMezzi.get(i));
                    }


                    //				problema: clicco sulla lista ma ho solo la stringa, non il mezzo corrispondente
                    //				soluzione: mantenere una variabile ordine che abbini lvMezzi con Mezzi
                    //				altra soluzione: trovare il mezzo dalla stringa
                    segnalazioneDialog.setOrarioRef(c);
                    segnalazioneDialog.setCallingContext(getApplicationContext());
                    segnalazioneDialog.setCallingActivity(act);
                    segnalazioneDialog.setListCompagnia(listCompagnia);

                    //segnalazioneDialog.fill(listCompagnia);
                    segnalazioneDialog.show(fm, "fragment_edit_name");
                    //segnalazioneDialog.show();
                    aggiornaLista(); //TODO Capire come si fa ad aggiornare dopo una segnalazione (oppure scrivere che prossimamente verr? aggiunta)
                }
                return true;
            }
        });


        //aggiungere onlongclick su lvMezzi che faccia partire il dialog di segnalazione
        //che ha due funzioni: segnala un cambiamento (interazione con mail)
        //esegui un cambiamento (richiede una password che conosco solo io)
        //il cambiamento si ottiene andando ad aggiornare un file esclusioni giornaliere
        //ad ogni cambiamento si apre il file, si riscrivono le righe di oggi, si aggiunge la riga della segnalazione
        //bisogna cambiare anche la lettura dei mezzi prevedendo la lettura di questo file con la conseguente
        //eliminazione delle corse indicate

        ultimaLetturaOrariDaWeb = Calendar.getInstance();
        ultimaLetturaOrariDaWeb.setLenient(true);
        //setto fittiziamente ad un valore diverso da oggi
        ultimaLetturaOrariDaWeb.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE - 1));
        riempiLista();
        setSpinner();
        aggiornaLista();
        setMsgToast();


    }

    private void leggiMeteo(boolean aggiorna) {
        this.aggiorna = aggiorna;
        //TODO Provare a mettere anche la lettura dei dati meteo in un task

        new LeggiMeteoTask(this).execute();



    }

	private void setTxtOrario(Calendar c) {
        String s = getString(R.string.dalle) + " ";
        if (c.get(Calendar.HOUR_OF_DAY) < 10)
            s += "0";
        s += c.get(Calendar.HOUR_OF_DAY) + ":";
        if (c.get(Calendar.MINUTE) < 10)
            s += "0";
        s += c.get(Calendar.MINUTE) + " " + getString(R.string.del) + " ";
        s += c.get(Calendar.DAY_OF_MONTH) + "/";
        s += (c.get(Calendar.MONTH) + 1) + "";
        txtOrario.setText(s);
    }

    private void riempiMezzidaInternalStorage(FileInputStream fstream) {
        try {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("App Event")
                    .setAction("Riempi Mezzi da Internal Storage")
                    .build());
            // Open the file that is the first
            // command line parameter
            Log.d("ORARI", "Inizio caricamento orari da IS");

            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String rigaAggiornamento = br.readLine();
            StringTokenizer st0 = new StringTokenizer(rigaAggiornamento, ",");
            aggiornamentoOrariIS = Calendar.getInstance(TimeZone.getDefault());
            aggiornamentoOrariIS.set(Calendar.DAY_OF_MONTH, Integer.parseInt(st0.nextToken()));
            aggiornamentoOrariIS.set(Calendar.MONTH, Integer.parseInt(st0.nextToken()));
            aggiornamentoOrariIS.set(Calendar.YEAR, Integer.parseInt(st0.nextToken()));
            //aboutDialog.setMessage(R.string.credits+"+aggiornamentoOrariIS.get(Calendar.DAY_OF_MONTH)+"/"+aggiornamentoOrariIS.get(Calendar.MONTH)+"/"+aggiornamentoOrariIS.get(Calendar.YEAR)+");
            aboutDialog.setMessage("" + getString(R.string.disclaimer) + "\n" + getString(R.string.credits));

            for (String line = br.readLine(); line != null; line = br.readLine()) {
                //esamino la riga e creo un mezzo
                StringTokenizer st = new StringTokenizer(line, ",");
                listMezzi.add(new Mezzo(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken()));
            }

            //Close the input stream
            in.close();
            Log.d("ORARI", "Fine caricamento orari da IS");
            int meseToast = aggiornamentoOrariIS.get(Calendar.MONTH);
            if (meseToast == 0) meseToast = 12;
            String str = getString(R.string.orariAggiornatiAl) + " " + aggiornamentoOrariIS.get(Calendar.DAY_OF_MONTH) + "/" + meseToast + "/" + aggiornamentoOrariIS.get(Calendar.YEAR);
            Log.d("ORARI", str);
            if (!primoAvvio)
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();

        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void riempiMezzidaWeb() {
        //Da eseguire all'avvio

        //soluzione momentanea per il problema Network.OnMainThreadException
        // https://developer.android.com/reference/android/os/NetworkOnMainThreadException.html

        //in background legge gli orari dal web; se sono piu' aggiornati li scrive sul file interno
        //(downloadMezziTask = new DownloadMezziTask(this)).execute();
        new DownloadMezziTask(this).execute();


        //Al termine dovrebbe ricaricare la lista degli orari

        //Non piu' necessario, grazie agli asynctask



    }

    public boolean stessoMezzo(String rigaData, String rigaMezzo, Mezzo m, Calendar cal) {
        //cal contiene l'ora scritta in cima alla schermata
        //Verifico che la distanza tra cal e l'orario fissato da data di riferimento della segnalazione e ora normale ci partenza del mezzo sia inferiore a tra 0 e 24 h

        StringTokenizer st = new StringTokenizer(rigaMezzo, ",");
        if (st.nextToken().equals(m.nave))
            if (Integer.valueOf(st.nextToken()) == m.oraPartenza.get(Calendar.HOUR_OF_DAY))
                if (Integer.valueOf(st.nextToken()) == m.oraPartenza.get(Calendar.MINUTE))
                    if (Integer.valueOf(st.nextToken()) == m.oraArrivo.get(Calendar.HOUR_OF_DAY))
                        if (Integer.valueOf(st.nextToken()) == m.oraArrivo.get(Calendar.MINUTE))
                            if (st.nextToken().equals(m.portoPartenza))
                                if (st.nextToken().equals(m.portoArrivo)) {
                                    StringTokenizer st2 = new StringTokenizer(rigaData, ",");
                                    Integer giorno = Integer.valueOf(st2.nextToken());
                                    Integer mese = Integer.valueOf(st2.nextToken());
                                    Integer anno = Integer.valueOf(st2.nextToken());
                                    Calendar calMezzo = (Calendar) cal.clone();
                                    calMezzo.set(Calendar.DAY_OF_MONTH, giorno);
                                    calMezzo.set(Calendar.MONTH, mese - 1);
                                    calMezzo.set(Calendar.YEAR, anno);
                                    calMezzo.set(Calendar.HOUR_OF_DAY, m.oraPartenza.get(Calendar.HOUR_OF_DAY));
                                    calMezzo.set(Calendar.MINUTE, m.oraArrivo.get(Calendar.MINUTE));
                                    //TODO Qua devo controllare le 24 ore)
                                    if (calMezzo.after(cal)) {
                                        calMezzo.add(Calendar.DAY_OF_YEAR, -1);
                                        if (calMezzo.before(cal))
                                            return true;
                                    }
                                }


		return false;
	}

    public boolean isGiornoVisualizzato(String rigaData, Calendar cal) {

		//Da semplificare, supponendo che ci interessino solo le segnalazioni di oggi
//		StringTokenizer st = new StringTokenizer( rigaData, "," );
//		Integer giorno=Integer.valueOf(st.nextToken());
//		Integer mese=Integer.valueOf(st.nextToken());
//		Integer anno=Integer.valueOf(st.nextToken());
//
//		if ((giorno==cal.get(Calendar.DAY_OF_MONTH) )&&(mese==1+cal.get(Calendar.MONTH))&&(anno==cal.get(Calendar.YEAR)))
//				return true;
//		else{
//			Calendar calDomani=Calendar.getInstance();
//			calDomani.set(Calendar.DATE, cal.get(Calendar.DATE));
//			calDomani.add(Calendar.DAY_OF_YEAR, 1);
//			if ((giorno==calDomani.get(Calendar.DAY_OF_MONTH) )&&(mese==1+calDomani.get(Calendar.MONTH))&&(anno==calDomani.get(Calendar.YEAR)))
        return true;
//
//		}
//
//			return false;
	}

	public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void riempiLista() {
        listCompagnia = new ArrayList<Compagnia>();

        Compagnia c = new Compagnia("Caremar");
        c.addTelefono("Napoli (Molo Beverello)", "0815513882");
        c.addTelefono("Pozzuoli", "0815262711");
        c.addTelefono("Pozzuoli", "0815261335");
        c.addTelefono("Ischia", "081984818");
        c.addTelefono("Ischia", "081991953");
        c.addTelefono("Procida", "0818967280");
        listCompagnia.add(c);

        c = new Compagnia("Gestur");
        c.addTelefono("Sede", "0818531405");
        c.addTelefono("Procida", "0818531405");
        c.addTelefono("Pozzuoli", "0815268165");
        listCompagnia.add(c);

        c = new Compagnia("SNAV");
        c.addTelefono("Call Center", "0814285111");
        c.addTelefono("Napoli", "0814285111");
        c.addTelefono("Ischia", "081984818");
        c.addTelefono("Procida", "0818969975");
        listCompagnia.add(c);

        c = new Compagnia("Medmar");
        c.addTelefono("Napoli", "0813334411");
        c.addTelefono("Procida", "0818969594");
        c.addTelefono("Procida", "0818969190");
        listCompagnia.add(c);

//        c = new Compagnia("Procida Lines");
//        c.addTelefono("Procida", "0818960328");
//        listCompagnia.add(c);

        c = new Compagnia("Ippocampo");
        c.addTelefono("Procida", "3663575751");
        c.addTelefono("Procida", "0818967764");
        c.addTelefono("Monte di Procida", "3397585125");
        listCompagnia.add(c);

        c = new Compagnia("Scotto Line");
        c.addTelefono("Procida", "3343525753");
        c.addTelefono("Procida", "0818968753");
        c.addTelefono("Procida", "3394775523");
        listCompagnia.add(c);

//        c = new Compagnia("Aladino");
//        c.addTelefono("Procida", "0818968089");
//        listCompagnia.add(c);

        c = new Compagnia("LazioMar");
        c.addTelefono("Napoli", "0771700604");
        listCompagnia.add(c);

        c = new Compagnia("Alilauro");
        c.addTelefono("Napoli", "0814972252");
        c.addTelefono("Call Center", "0814972222");
        listCompagnia.add(c);

        try {
            FileInputStream fstream = new FileInputStream(getApplicationContext().getFilesDir().getPath() + "/orari.csv");
            riempiMezzidaInternalStorage(fstream);
        } catch (FileNotFoundException e) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("App Event")
                    .setAction("Riempi Lista da Codice")
                    .build());
            Log.d("ORARI", "File non trovato su IS. Leggo da codice");
            // convenzione giorni settimana:
            // DOMENICA =1 LUNEDI=2 MARTEDI=3 MERCOLEDI=4 GIOVEDI=5 VENERDI=6 SABATO=7

            aggiornamentoOrariIS = Calendar.getInstance(TimeZone.getDefault());
            aggiornamentoOrariIS.set(2011, 11, 1); //Orari aggiornato all'1/11/2011
            aboutDialog.setMessage("" + getString(R.string.disclaimer) + "\n" + getString(R.string.credits));


            listMezzi.add(new Mezzo("Aliscafo Caremar", 8, 10, 8, 25, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 9, 10, 9, 45, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 12, 10, 12, 40, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 18, 35, 19, 0, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 10, 20, 10, 55, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 13, 50, 14, 20, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 19, 15, 19, 45, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 8, 55, 9, 15, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 7, 30, 8, 10, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 8, 50, 9, 30, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 11, 45, 12, 25, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 13, 10, 13, 50, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 15, 10, 15, 50, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 17, 30, 18, 10, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 18, 15, 18, 55, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            //listMezzi.add(new Mezzo(getApplicationContext(),"Traghetto Caremar",0,15,1,15,"Napoli Porta di Massa","Procida",0,0,0,0,0,0,"1234567",this));
            listMezzi.add(new Mezzo("Traghetto Caremar", 6, 25, 7, 25, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 9, 10, 10, 10, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 10, 45, 11, 45, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 15, 15, 16, 15, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 17, 45, 18, 45, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 19, 30, 20, 30, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 22, 15, 23, 15, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            //listMezzi.add(new Mezzo(getApplicationContext(),"Traghetto Caremar",2,20,3,20,"Procida","Napoli Porta di Massa",0,0,0,0,0,0,"1234567",this));
            listMezzi.add(new Mezzo("Traghetto Caremar", 7, 40, 8, 40, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 13, 35, 14, 35, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 14, 35, 15, 35, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 16, 15, 17, 15, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 18, 5, 19, 0, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 20, 30, 21, 30, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            //listMezzi.add(new Mezzo(getApplicationContext(),"Traghetto Caremar",22,55,23,55,"Procida","Napoli Porta di Massa",0,0,0,0,0,0,"1234567",this));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 6, 35, 7, 15, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 7, 55, 8, 35, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 9, 25, 10, 5, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 10, 35, 11, 15, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 13, 30, 14, 10, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 14, 55, 15, 35, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 16, 55, 17, 35, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Gestur", 6, 50, 7, 30, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Gestur", 9, 40, 10, 20, "Procida", "Pozzuoli", 19, 10, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Gestur", 11, 30, 12, 10, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Gestur", 14, 5, 14, 45, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Gestur", 17, 5, 17, 45, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Gestur", 8, 25, 9, 5, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Gestur", 10, 40, 11, 20, "Pozzuoli", "Procida", 19, 10, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Gestur", 13, 0, 13, 40, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Gestur", 15, 30, 16, 10, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Gestur", 17, 55, 18, 35, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 8, 25, 9, 0, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 12, 20, 12, 55, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 16, 20, 16, 55, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 19, 0, 19, 35, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 7, 30, 8, 5, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 10, 10, 10, 45, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 14, 15, 14, 50, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 18, 5, 18, 40, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Medmar", 4, 10, 4, 50, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "23456"));
            listMezzi.add(new Mezzo("Medmar", 20, 30, 21, 10, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Medmar", 3, 10, 3, 50, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "23456"));
            listMezzi.add(new Mezzo("Medmar", 19, 40, 20, 20, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Medmar", 5, 0, 5, 20, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "23456"));
            listMezzi.add(new Mezzo("Medmar", 21, 20, 21, 40, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Medmar", 2, 30, 2, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "23456"));
            listMezzi.add(new Mezzo("Medmar", 6, 25, 6, 45, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Medmar", 10, 35, 10, 55, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));

            listMezzi.add(new Mezzo("Traghetto Caremar", 7, 35, 7, 55, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 10, 20, 10, 40, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 11, 5, 11, 25, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 11, 55, 12, 15, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 14, 30, 14, 50, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 16, 25, 18, 45, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 18, 55, 19, 15, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 19, 50, 20, 10, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 20, 35, 20, 55, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 23, 20, 23, 40, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));

            listMezzi.add(new Mezzo("Traghetto Caremar", 7, 0, 7, 20, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 8, 30, 8, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 11, 30, 11, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 12, 55, 13, 15, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 13, 55, 14, 15, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 15, 30, 15, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 17, 25, 17, 45, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 18, 0, 18, 20, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Traghetto Caremar", 19, 55, 20, 15, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));

            listMezzi.add(new Mezzo("Aliscafo Caremar", 9, 35, 9, 50, "Procida", "Ischia Porto", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 12, 30, 12, 45, "Procida", "Ischia Porto", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 13, 55, 14, 10, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 15, 55, 16, 10, "Procida", "Ischia Porto", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 19, 0, 19, 15, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));

            listMezzi.add(new Mezzo("Aliscafo Caremar", 7, 30, 7, 45, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 10, 10, 10, 25, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 13, 5, 13, 20, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 14, 30, 14, 45, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo Caremar", 16, 30, 16, 45, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));

            listMezzi.add(new Mezzo("Aliscafo SNAV", 7, 10, 7, 25, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 9, 45, 10, 0, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 13, 50, 14, 10, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 17, 40, 17, 55, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));

            listMezzi.add(new Mezzo("Aliscafo SNAV", 9, 0, 9, 15, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 13, 15, 13, 30, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 17, 5, 17, 20, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            listMezzi.add(new Mezzo("Aliscafo SNAV", 19, 45, 10, 0, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
        }


        boolean updateWeb = true;
        // Carica da Web solo se non sono abbastnza aggiornati

        if (isOnline() && (ultimaLetturaOrariDaWeb.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)))
            riempiMezzidaWeb();
        else
            Log.d("ORARI", "Non c'? connessione o non c'? bisogno di aggiornamento: non carico orari da Web");
        if (isOnline())
            leggiSegnalazioniDaWeb();
	}

	private void leggiSegnalazioniDaWeb() {
        //Leggo il file delle segnalazioni

        new LeggiSegnalazioniTask(this).execute();


	}

    public void aggiornaLista() {
        //NOn ? chiaro perch? il controllo del locale debba essere fatto proprio qui!!!
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("Aggiorna Lista")
                .build());
/*
        if (!((Locale.getDefault().getLanguage().contentEquals("en")) || (Locale.getDefault().getLanguage().contentEquals("it")))) {
            String languageToLoad = "en";
            locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
*/

        selectMezzi = new ArrayList<Mezzo>();
        Comparator<Mezzo> comparator = new Comparator<Mezzo>() {
            @Override
            public int compare(Mezzo m1, Mezzo m2) {
                if (m1.getGiornoSeguente() == m2.getGiornoSeguente()) {
                    if (m1.oraPartenza.before(m2.oraPartenza))
                        return -1;
                    else if (m1.oraPartenza.after(m2.oraPartenza))
                        return 1;
                    else
                        return 0;
                } else if (m1.getGiornoSeguente()) {
                    return 1;
                } else if (m2.getGiornoSeguente()) {
                    return -1;
                }
                return 0;
            }
        };

        aalvMezzi.clear();

        String naveEspanso = nave;
        if (nave.contains(getString(R.string.traghetti)))
            naveEspanso = "Traghetto Caremar Medmar Ippocampo Ippocampo(da Chiaiolella) Ippocampo(a Chiaiolella) Traghetto LazioMar";
        if (nave.contains(getString(R.string.aliscafi)))
            naveEspanso = "Aliscafo Caremar Aliscafo SNAV Scotto Line Aliscafo Alilauro";
        if (nave.equals("Ippocampo"))
            naveEspanso = "Ippocampo Ippocampo(da Chiaiolella) Ippocampo(a Chiaiolella)";
        if (nave.contains("Gestur"))
            naveEspanso = "Motonave Gestur Traghetto Gestur";

        String portoPartenzaEspanso = portoPartenza;
        if (portoPartenza.equals("Napoli"))
            portoPartenzaEspanso = "Napoli Porta di Massa o Napoli Beverello";
        if (portoPartenza.equals("Napoli o Pozzuoli"))
            portoPartenzaEspanso = "Napoli Porta di Massa o Napoli Beverello o Pozzuoli";
        if (portoPartenza.equals("Ischia"))
            portoPartenzaEspanso = "Ischia Porto o Casamicciola";
        if (portoPartenza.equals("Monte di Procida"))
            portoPartenzaEspanso = "Monte di Procida";
        String portoArrivoEspanso = portoArrivo;
        if (portoArrivo.equals("Napoli"))
            portoArrivoEspanso = "Napoli Porta di Massa o Napoli Beverello";
        if (portoArrivo.equals("Napoli o Pozzuoli"))
            portoArrivoEspanso = "Napoli Porta di Massa o Napoli Beverello o Pozzuoli";
        if (portoArrivo.equals("Ischia"))
            portoArrivoEspanso = "Ischia Porto o Casamicciola";
        if (portoArrivo.equals("Monte di Procida"))
            portoArrivoEspanso = "Monte di Procida";
        Calendar oraLimite = (Calendar) c.clone();
        oraLimite.add(Calendar.HOUR_OF_DAY, configData.getFinestraTemporale());

        //qui riempio aalvMezzi in base agli input e ai dati di listMezzi
        for (int i = 0; i < listMezzi.size(); i++) {
            //per ogni mezzo valuta se ci interessa
            Calendar oraNave = (Calendar) listMezzi.get(i).oraPartenza.clone();
            oraNave.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
            oraNave.set(Calendar.MONTH, c.get(Calendar.MONTH));
            oraNave.set(Calendar.YEAR, c.get(Calendar.YEAR));
            if ((oraNave.get(Calendar.HOUR_OF_DAY) < c.get(Calendar.HOUR_OF_DAY)) || (oraNave.get(Calendar.HOUR_OF_DAY) == c.get(Calendar.HOUR_OF_DAY)) && (oraNave.get(Calendar.MINUTE) < c.get(Calendar.MINUTE)))
                oraNave.add(Calendar.DAY_OF_MONTH, 1);

            if (naveEspanso.contains(listMezzi.get(i).nave) || nave.equals(getString(R.string.tutti))) {
                if ((listMezzi.get(i).portoPartenza.equals((portoPartenza))) || (portoPartenzaEspanso.contains(listMezzi.get(i).portoPartenza)) || (portoPartenza.equals(getString(R.string.tutti)))) {
                    if ((listMezzi.get(i).portoArrivo.equals((portoArrivo))) || (portoArrivoEspanso.contains(listMezzi.get(i).portoArrivo)) || (portoArrivo.equals(getString(R.string.tutti)))) {
                        if (listMezzi.get(i).inizioEsclusione.after(oraNave) || listMezzi.get(i).fineEsclusione.before(oraNave))
                            if (listMezzi.get(i).giorniSettimana.contains(String.valueOf(oraNave.get(Calendar.DAY_OF_WEEK))))
                                if (oraNave.before(oraLimite)) {
                                    if (oraNave.get(Calendar.DAY_OF_MONTH) != c.get(Calendar.DAY_OF_MONTH))
                                        listMezzi.get(i).setGiornoSeguente(true);
                                    else
                                        listMezzi.get(i).setGiornoSeguente(false);
                                    listMezzi.get(i).setId(i);
                                    selectMezzi.add(listMezzi.get(i));
                                }
                    }
                }
            }


        }
        Collections.sort(selectMezzi, comparator);
        for (int i = 0; i < selectMezzi.size(); i++) {
            selectMezzi.get(i).setOrderInList(i);
            String s = selectMezzi.get(i).nave + " - " + selectMezzi.get(i).portoPartenza + " - " + selectMezzi.get(i).portoArrivo + " - ";
//    		s += selectMezzi.get(i).getOrderInList()+" - ";
            if (selectMezzi.get(i).oraPartenza.get(Calendar.HOUR_OF_DAY) < 10)
                s += "0";
            s += selectMezzi.get(i).oraPartenza.get(Calendar.HOUR_OF_DAY) + ":";
            if (selectMezzi.get(i).oraPartenza.get(Calendar.MINUTE) < 10)
                s += "0";
            s += selectMezzi.get(i).oraPartenza.get(Calendar.MINUTE) + " ";
//    		s+=selectMezzi.get(i).getGiornoSeguente()+" ";
            // Qui aggiungo l'indicazione meteo eventuale
            s += meteo.condimeteoString(selectMezzi.get(i));
            //Qui aggiungo le segnalazioni

            String spc = "";
            if (selectMezzi.get(i).segnalazionePiuComune() > -1)
                spc = ragioni[selectMezzi.get(i).segnalazionePiuComune()];
            if (selectMezzi.get(i).tot > 0 || selectMezzi.get(i).conferme > 0) {
                //Trasformato con resources
                if (selectMezzi.get(i).tot > 0) { //c'? qualcosa
                    if (selectMezzi.get(i).conc) {
                        s += " -  " + selectMezzi.get(i).tot;
                        if (selectMezzi.get(i).tot == 1)
                            s += " " + getString(R.string.segnalazione);
                        else
                            s += " " + getString(R.string.segnalazioni);
                        s += " " + getString(R.string.diProblemi) + " (" + spc + ")";
                    } else {
                        s += " - " + getString(R.string.possibiliProblemi) + " (" + selectMezzi.get(i).tot;
                        if (selectMezzi.get(i).tot == 1)
                            s += " " + getString(R.string.segnalazione) + ")";
                        else
                            s += " " + getString(R.string.segnalazioni) + ")";
                        s += ", " + getString(R.string.inParticolare) + " " + spc;
                    }
                }
                if (selectMezzi.get(i).conferme > 0) {
                    s += " - " + selectMezzi.get(i).conferme;
                    if (selectMezzi.get(i).conferme == 1)
                        s += " " + getString(R.string.utenteDice);
                    else
                        s += " " + getString(R.string.utentiDicono);
                    s += " " + getString(R.string.cheLaCorsaERegolare);
                }

            }
            aalvMezzi.add(s);
        }

    }

    private void setSpinner() {
        Spinner spnNave = findViewById(R.id.spnNave);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.strMezzi, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spnNave.setAdapter(adapter);

        nave = getString(R.string.tutti);
        portoPartenza = getString(R.string.tutti);
        portoArrivo = getString(R.string.tutti);

        spnNave.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                nave = parent.getItemAtPosition(pos).toString();
                aggiornaLista();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Spinner spnPortoPartenza = findViewById(R.id.spnPortoPartenza);
        final ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this, R.array.strPorti, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(R.layout.spinner_item);
        spnPortoPartenza.setAdapter(adapter2);

        //controllo e setto tramite algoritmo di set con gps
        portoPartenza = setPortoPartenza();

        setSpnPortoPartenza(spnPortoPartenza, adapter2);

        final Spinner spnPortoArrivo = findViewById(R.id.spnPortoArrivo);
        final ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
                this, R.array.strPorti, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(R.layout.spinner_item);
        spnPortoArrivo.setAdapter(adapter3);

        if (!(portoPartenza.contentEquals("Procida") || portoPartenza.contentEquals(getString(R.string.tutti)))) {
            portoArrivo = "Procida";
            setSpnPortoArrivo(spnPortoArrivo, adapter3);
        }

        spnPortoPartenza.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                portoPartenza = parent.getItemAtPosition(pos).toString();
                if (portoPartenza.contentEquals("Procida") && portoArrivo.contentEquals("Procida")) {
                    portoArrivo = getString(R.string.tutti);
                    setSpnPortoArrivo(spnPortoArrivo, adapter2);
                    setSpnPortoPartenza(spnPortoPartenza, adapter3);
                } else if (!(portoPartenza.contentEquals("Procida") || portoPartenza.contentEquals(getString(R.string.tutti)))) {
                    portoArrivo = "Procida";
                    setSpnPortoArrivo(spnPortoArrivo, adapter2);
                    setSpnPortoPartenza(spnPortoPartenza, adapter3);
                }
                aggiornaLista();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        spnPortoArrivo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                portoArrivo = parent.getItemAtPosition(pos).toString();
                if (portoArrivo.contentEquals("Procida") && portoPartenza.contentEquals("Procida")) {
                    portoPartenza = getString(R.string.tutti);
                    setSpnPortoPartenza(spnPortoPartenza, adapter2);
                    setSpnPortoArrivo(spnPortoArrivo, adapter3);
                } else if (!(portoArrivo.contentEquals("Procida") || portoArrivo.contentEquals(getString(R.string.tutti)))) {
                    portoPartenza = "Procida";
                    setSpnPortoPartenza(spnPortoPartenza, adapter2);
                    setSpnPortoArrivo(spnPortoArrivo, adapter3);
                }
                aggiornaLista();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void setMsgToast() {
        primoAvvio = false;
        msgToast = new String("");
        if (!(portoPartenza.equals(getString(R.string.tutti))))
            msgToast += (getString(R.string.secondoMeVuoiPartireDa) + " " + portoPartenza + "\n");
        if (aggiornamentoOrariIS != null) {
            int mese = aggiornamentoOrariIS.get(Calendar.MONTH);
            if (mese == 0) mese = 12;
            msgToast += getString(R.string.orariAggiornatiAl) + " " + aggiornamentoOrariIS.get(Calendar.DATE) + "/" + mese + "/" + aggiornamentoOrariIS.get(Calendar.YEAR) + "\n";
        }
        if (aggiornamentoMeteo != null) {
            meteo.setWindBeaufort(meteo.getWindKmh());
            msgToast += (getString(R.string.updated) + " " + aggiornamentoMeteo.get(Calendar.DAY_OF_MONTH) + "/" + (1 + aggiornamentoMeteo.get(Calendar.MONTH)) + "/" + aggiornamentoMeteo.get(Calendar.YEAR) + " " + getString(R.string.ore) + " " + aggiornamentoMeteo.get(Calendar.HOUR_OF_DAY) + ":" + aggiornamentoMeteo.get(Calendar.MINUTE) + " " + getString(R.string.condimeteo) + meteo.getWindBeaufortString() + " (" + meteo.getWindKmh().intValue() + " km/h) " + getString(R.string.da) + " " + meteo.getWindDirectionString() + "\n");
        }

        Toast.makeText(getApplicationContext(), msgToast, Toast.LENGTH_LONG).show();
    }

    private void setSpnPortoArrivo(Spinner spnPortoArrivo,
                                   final ArrayAdapter<CharSequence> adapter3) {
        //trova il valore corretto nello spinner
        for (int i = 0; i < spnPortoArrivo.getCount(); i++) {
            if (adapter3.getItem(i).equals(portoArrivo)) {
                spnPortoArrivo.setSelection(i);
            }
        }
	}

	private void setSpnPortoPartenza(Spinner spnPortoPartenza,
                                     ArrayAdapter<CharSequence> adapter2) {
        //trova il valore corretto nello spinner
        for (int i = 0; i < spnPortoPartenza.getCount(); i++) {
            if (adapter2.getItem(i).equals(portoPartenza)) {
                spnPortoPartenza.setSelection(i);
            }
        }
    }


    private String setPortoPartenza() {
        // Trova il porto pi? vicino a quello di partenza
        Location l = null;


        if (ActivityCompat.checkSelfPermission(OrariProcida2011Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(OrariProcida2011Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //System.exit(0);
            return "Any";
        }

        try {
            l = myManager.getLastKnownLocation(BestProvider);
            Log.d("ACTIVITY", "Posizione:" + l.getLongitude() + "," + l.getLatitude());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ACTIVITY", "Problema con GPS");
        }
        if (l == null)
            return getString(R.string.tutti);
        //Coordinate angoli Procida
        if ((l.getLatitude() > 40.7374) && (l.getLatitude() < 40.7733) && (l.getLongitude() > 13.9897) && (l.getLongitude() < 14.0325)) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("User")
                    .setAction("From Procida")
                    .build());
            return "Procida";
        }
        //Coordinate angoli Isola d'Ischia
        if ((l.getLatitude() > 40.6921) && (l.getLatitude() < 40.7626) && (l.getLongitude() > 13.8465) && (l.getLongitude() < 13.9722))
            //Isola d'Ischia
            if (calcolaDistanza(l, 13.9063, 40.7496) > calcolaDistanza(l, 13.9602, 40.7319)) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("User")
                        .setAction("From Ischia")
                        .build());
                return "Ischia";
            } else {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("User")
                        .setAction("From Casamicciola")
                        .build());
                return "Casamicciola";
        } 
      //Inserire coordinate Napoli (media porti) e Pozzuoli
      double distNapoli=calcolaDistanza(l,14.2575,40.84); Log.d("OrariProcida","d(Napoli)="+distNapoli);
      double distPozzuoli=calcolaDistanza(l,14.1179,40.8239); Log.d("OrariProcida","d(Pozzuoli)="+distPozzuoli);
      double distMonteProcida=calcolaDistanza(l,14.05,40.8);
        if (distMonteProcida < 1500) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("User")
                    .setAction("From Monte di Procida")
                    .build());
            return "Monte di Procida";
        }
      if (distPozzuoli<distNapoli){
          if (distPozzuoli < 15000) {
              mTracker.send(new HitBuilders.EventBuilder()
                      .setCategory("User")
                      .setAction("From Pozzuoli")
                      .build());
              return "Pozzuoli";
          } else {
              mTracker.send(new HitBuilders.EventBuilder()
                      .setCategory("User")
                      .setAction("From Napoli o Pozzuoli")
                      .build());
              return "Napoli o Pozzuoli";
          }
      }
      else { 
    	  if (distNapoli<15000){
              if (distNapoli > 1000) {
                  mTracker.send(new HitBuilders.EventBuilder()
                          .setCategory("User")
                          .setAction("From Napoli")
                          .build());
                  return "Napoli";
              }
    		  else{
                  if (calcolaDistanza(l, 14.2548, 40.8376) < calcolaDistanza(l, 14.2602, 40.8424)) {
                      mTracker.send(new HitBuilders.EventBuilder()
                              .setCategory("User")
                              .setAction("From Napoli Beverello")
                              .build());
                      return "Napoli Beverello";
                  } else {
                      mTracker.send(new HitBuilders.EventBuilder()
                              .setCategory("User")
                              .setAction("From Napoli Porta di Massa")
                              .build());
                      return "Napoli Porta di Massa";
                  }
              }
          } else {
              mTracker.send(new HitBuilders.EventBuilder()
                      .setCategory("User")
                      .setAction("From Napoli o Pozzuoli")
                      .build());
              return "Napoli o Pozzuoli";
          }
      }
	}

    private double calcolaDistanza(Location location, double lon, double lat) {
        //calcola distanza da obiettivo
		double deltaLong=Math.abs(lon-location.getLongitude());
		double deltaLat=Math.abs(lat-location.getLatitude());
		double delta=(Math.sqrt(deltaLong*deltaLong+deltaLat*deltaLat));
		delta=delta*60*1852;		
		return Math.ceil(delta);
	}
	protected void onStart(){
		super.onStart();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("onStart")
                .build());
	}
    
    protected void onRestart(){
    	super.onRestart();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("onRestart")
                .build());
        Log.d("ACTIVITY", "restart");
    }

    protected void onResume(){
    	super.onResume();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("onResume")
                .build());
        Log.i("ORARI", "Setting screen name: " + "Main Activity");
//        if (!((Locale.getDefault().getLanguage().contentEquals("en"))||	(Locale.getDefault().getLanguage().contentEquals("it"))))
//    	{
//    	String languageToLoad  = "en";
//    	locale = new Locale(languageToLoad);        	
//    	Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config, 
//        getBaseContext().getResources().getDisplayMetrics());
//    }

    	Log.d("ACTIVITY","resume");
    }

    protected void onPause(){
    	super.onPause();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("onPause")
                .build());
    	Log.d("ACTIVITY","pause");
    }

    protected void onStop(){
    	super.onStop();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("onStop")
                .build());
    }


    protected void onDestroy(){
    	super.onDestroy();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("App Event")
                .setAction("onDestroy")
                .build());
    	Log.d("ACTIVITY","destroy");
    }


}
