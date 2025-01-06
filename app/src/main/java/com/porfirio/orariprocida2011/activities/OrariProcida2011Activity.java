package com.porfirio.orariprocida2011.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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

import com.porfirio.orariprocida2011.threads.DownloadTransportsHandler;
import com.porfirio.orariprocida2011.utils.Analytics;
import com.porfirio.orariprocida2011.utils.AnalyticsApplication;
import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.dao.OnRequestWeatherDAO;
import com.porfirio.orariprocida2011.threads.ReadAlertsHandler;
import com.porfirio.orariprocida2011.utils.WeatherUpdate;
import com.porfirio.orariprocida2011.dialogs.DettagliMezzoDialog;
import com.porfirio.orariprocida2011.dialogs.SegnalazioneDialog;
import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.entity.Meteo;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.entity.Osservazione;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrariProcida2011Activity extends FragmentActivity {

    private static final String ANALYTICS_CATEGORY_APP_EVENT = "App Event";
    private static final String ANALYTICS_CATEGORY_UI_EVENT = "UI Event";
    private static final String ANALYTICS_CATEGORY_USER_EVENT = "User";

    private static FragmentManager fm;

    public Calendar c;
    public Calendar updateWebTimes;
    public Calendar updateTimesIS;
    // Introdotto il concetto di ultima lettura degli orari da Web
    public Calendar ultimaLetturaOrariDaWeb;
    public Calendar aggiornamentoMeteo;
    public AlertDialog aboutDialog;
    public Meteo meteo;
    public AlertDialog meteoDialog;
    public ArrayList<Mezzo> transportList;
    public boolean aggiorna;
    private String[] ragioni = new String[100];

    private String nave;
    private String portoPartenza;
    private String portoArrivo;
    private ArrayAdapter<String> aalvMezzi;
    private TextView txtOrario;
    //public AlertDialog novitaDialog;
    private DettagliMezzoDialog dettagliMezzoDialog;
    private ArrayList<Mezzo> selectMezzi;
    private ArrayList<Compagnia> listCompagnia;
    private LocationManager myManager;
    private String BestProvider;
    private SegnalazioneDialog segnalazioneDialog;
    private boolean primoAvvio = true;
    public String msgToast;

    private OnRequestWeatherDAO weatherDAO;
    private ExecutorService executorService;

    private Analytics analytics;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Open Menu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case (R.id.about):
                analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "About");
                aboutDialog.show();
                return true;
            // cambiata semantica pulsante: se scelgo, allora carico esplicitamente da web
            case (R.id.updateWeb):
                analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Update Orari da Web da Menu");
                // Caricare da Web
                if (isOnline()) {
                    riempiMezzidaWeb();
                    if (updateWebTimes != null) {
                        int meseToast = updateWebTimes.get(Calendar.MONTH);
                        if (meseToast == 0) meseToast = 12;
                        if (!primoAvvio)
                            Toast.makeText(getApplicationContext(), getString(R.string.orariAggiornatiAl) + " " + updateWebTimes.get(Calendar.DATE) + "/" + meseToast + "/" + updateWebTimes.get(Calendar.YEAR), Toast.LENGTH_LONG).show();
                        //TODO: Forzare aggiornamento
                    }
                } else
                    Log.d("ORARI", "Non c'? la connessione: non carico orari da Web");
                return true;
            case (R.id.meteo):
                analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Update Meteo da Menu");
                weatherDAO.requestUpdate();
                return true;
            case (R.id.esci):
                analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Exit da Menu");
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        analytics = new Analytics((AnalyticsApplication) getApplication());
        analytics.send(ANALYTICS_CATEGORY_APP_EVENT, "onCreate");

        fm = getSupportFragmentManager();

        if (ActivityCompat.checkSelfPermission(OrariProcida2011Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            analytics.send(ANALYTICS_CATEGORY_APP_EVENT, "Request Permission");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET}, 1
            );
        }

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
                .setPositiveButton("OK", (dialog, id) -> dialog.cancel());
        aboutDialog = builder.create();


        ragioni = getResources().getStringArray(R.array.strRagioni);

        meteo = new Meteo();

        //TODO: Forzare aggiornamento

        //TODO: Problema: leggiMeteo non e' piu' bloccante, quindi bisogna togliere il meteo dal primo messaggio e aggiungerlo quando e' il momento

        builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> dialog.cancel());
        meteoDialog = builder.create();

        // get the current time

        c = Calendar.getInstance(TimeZone.getDefault());
        txtOrario = findViewById(R.id.txtOrario);
        setTxtOrario(c);

        Button buttonMinusMinus = findViewById(R.id.btnConfermaOSmentisci);
        buttonMinusMinus.setOnClickListener(v -> {
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Button --");
            c.add(Calendar.HOUR, -1);
            setTxtOrario(c);
            aggiornaLista();
        });

        Button buttonMinus = findViewById(R.id.button2);
        buttonMinus.setOnClickListener(v -> {
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Button -");
            c.add(Calendar.MINUTE, -15);
            setTxtOrario(c);
            aggiornaLista();
        });

        Button buttonPlus = findViewById(R.id.button3);
        buttonPlus.setOnClickListener(v -> {
            //TODO L'unico problema residuo ? che problemi correttamente segnalati con pi? di 24 ore di anticipo vengono visualizzati solo a meno di 24h
            //Forse potrebbe essere risolto forzando un refresh quando si avanza di 24h rispetto all'orario corrente
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Button +");
            c.add(Calendar.MINUTE, 15);
            setTxtOrario(c);
            aggiornaLista();
        });

        Button buttonPlusPlus = findViewById(R.id.button4);
        buttonPlusPlus.setOnClickListener(v -> {
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Button ++");
            c.add(Calendar.HOUR, 1);
            setTxtOrario(c);
            aggiornaLista();
        });

        // spostato in avanti setSpinner();


        transportList = new ArrayList<>();
        ListView lvMezzi = findViewById(R.id.listMezzi);
        aalvMezzi = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lvMezzi.setAdapter(aalvMezzi);

        dettagliMezzoDialog = new DettagliMezzoDialog();
        dettagliMezzoDialog.setDettagliMezzoDialog(fm, this, this, c);
        dettagliMezzoDialog.setAnalytics(analytics);

        segnalazioneDialog = new SegnalazioneDialog();
        //listener sul click di un item della lista
        lvMezzi.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Click Dettagli Mezzo");
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

        });
        lvMezzi.setLongClickable(true);
        lvMezzi.setOnItemLongClickListener((arg0, arg1, arg2, arg3) -> {
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "LongClick DettagliMezzo");

            if (!isOnline())
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.soloOnline), Toast.LENGTH_SHORT).show();
            else {
                for (int i = 0; i < aalvMezzi.getCount(); i++) {
                    if (selectMezzi.get(i).getOrderInList() == arg2)
                        segnalazioneDialog.setMezzo(selectMezzi.get(i));
                }


                // problema: clicco sulla lista ma ho solo la stringa, non il mezzo corrispondente
                // soluzione: mantenere una variabile ordine che abbini lvMezzi con Mezzi
                // altra soluzione: trovare il mezzo dalla stringa
                segnalazioneDialog.setOrarioRef(c);
                segnalazioneDialog.setCallingContext(getApplicationContext());
                segnalazioneDialog.setAnalytics(analytics);
                segnalazioneDialog.setListCompagnia(listCompagnia);

                //segnalazioneDialog.fill(listCompagnia);
                segnalazioneDialog.show(fm, "fragment_edit_name");
                //segnalazioneDialog.show();
                aggiornaLista(); //TODO Capire come si fa ad aggiornare dopo una segnalazione (oppure scrivere che prossimamente verr? aggiunta)
            }
            return true;
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

        executorService = Executors.newSingleThreadExecutor();

        weatherDAO = new OnRequestWeatherDAO(executorService);
        weatherDAO.getUpdates().observe(this, this::onWeatherUpdate);
        weatherDAO.requestUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        weatherDAO.getUpdates().removeObservers(this);

        executorService.shutdown();
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
            analytics.send(ANALYTICS_CATEGORY_APP_EVENT, "Riempi Mezzi da Internal Storage");
            // Open the file that is the first
            // command line parameter
            Log.d("ORARI", "Inizio caricamento orari da IS");

            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String rigaAggiornamento = br.readLine();
            StringTokenizer st0 = new StringTokenizer(rigaAggiornamento, ",");
            updateTimesIS = Calendar.getInstance(TimeZone.getDefault());
            updateTimesIS.set(Calendar.DAY_OF_MONTH, Integer.parseInt(st0.nextToken()));
            updateTimesIS.set(Calendar.MONTH, Integer.parseInt(st0.nextToken()));
            updateTimesIS.set(Calendar.YEAR, Integer.parseInt(st0.nextToken()));
            //aboutDialog.setMessage(R.string.credits+"+aggiornamentoOrariIS.get(Calendar.DAY_OF_MONTH)+"/"+aggiornamentoOrariIS.get(Calendar.MONTH)+"/"+aggiornamentoOrariIS.get(Calendar.YEAR)+");
            aboutDialog.setMessage(getString(R.string.disclaimer) + "\n" + getString(R.string.credits));

            for (String line = br.readLine(); line != null; line = br.readLine()) {
                //esamino la riga e creo un mezzo
                StringTokenizer st = new StringTokenizer(line, ",");
                transportList.add(new Mezzo(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken()));
            }

            //Close the input stream
            in.close();
            Log.d("ORARI", "Fine caricamento orari da IS");
            int meseToast = updateTimesIS.get(Calendar.MONTH);
            if (meseToast == 0) meseToast = 12;
            String str = getString(R.string.orariAggiornatiAl) + " " + updateTimesIS.get(Calendar.DAY_OF_MONTH) + "/" + meseToast + "/" + updateTimesIS.get(Calendar.YEAR);
            Log.d("ORARI", str);
            if (!primoAvvio)
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();

        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void riempiMezzidaWeb() {
        DownloadTransportsHandler handler = new DownloadTransportsHandler(this);
        handler.setAnalytics(analytics);
        handler.fetchTransports();
    }

    public boolean sameTransport(String rigaData, String rigaMezzo, Mezzo m, Calendar cal) {
        //cal contiene l'ora scritta in cima alla schermata
        //Verifico che la distanza tra cal e l'orario fissato da data di riferimento della segnalazione e ora normale ci partenza del mezzo sia inferiore a tra 0 e 24 h

        StringTokenizer st = new StringTokenizer(rigaMezzo, ",");
        if (st.nextToken().equals(m.nave))
            if (Integer.parseInt(st.nextToken()) == m.oraPartenza.get(Calendar.HOUR_OF_DAY))
                if (Integer.parseInt(st.nextToken()) == m.oraPartenza.get(Calendar.MINUTE))
                    if (Integer.parseInt(st.nextToken()) == m.oraArrivo.get(Calendar.HOUR_OF_DAY))
                        if (Integer.parseInt(st.nextToken()) == m.oraArrivo.get(Calendar.MINUTE))
                            if (st.nextToken().equals(m.portoPartenza))
                                if (st.nextToken().equals(m.portoArrivo)) {
                                    StringTokenizer st2 = new StringTokenizer(rigaData, ",");
                                    int giorno = Integer.parseInt(st2.nextToken());
                                    int mese = Integer.parseInt(st2.nextToken());
                                    int anno = Integer.parseInt(st2.nextToken());
                                    Calendar calMezzo = (Calendar) cal.clone();
                                    calMezzo.set(Calendar.DAY_OF_MONTH, giorno);
                                    calMezzo.set(Calendar.MONTH, mese - 1);
                                    calMezzo.set(Calendar.YEAR, anno);
                                    calMezzo.set(Calendar.HOUR_OF_DAY, m.oraPartenza.get(Calendar.HOUR_OF_DAY));
                                    calMezzo.set(Calendar.MINUTE, m.oraArrivo.get(Calendar.MINUTE));
                                    //TODO Qua devo controllare le 24 ore)
                                    if (calMezzo.after(cal)) {
                                        calMezzo.add(Calendar.DAY_OF_YEAR, -1);
                                        return calMezzo.before(cal);
                                    }
                                }
        return false;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void riempiLista() {
        //TODO Questi dati andrebbero letti da un file o risorsa su FireBase

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
            analytics.send(ANALYTICS_CATEGORY_APP_EVENT, "Riempi Lista da Codice");
            Log.d("ORARI", "File non trovato su IS. Leggo da codice");
            // convenzione giorni settimana:
            // DOMENICA =1 LUNEDI=2 MARTEDI=3 MERCOLEDI=4 GIOVEDI=5 VENERDI=6 SABATO=7

            updateTimesIS = Calendar.getInstance(TimeZone.getDefault());
            updateTimesIS.set(2011, 11, 1); //Orari aggiornato all'1/11/2011
            aboutDialog.setMessage("" + getString(R.string.disclaimer) + "\n" + getString(R.string.credits));

            //TODO Questa lista di dati è per il caso di prima esecuzione senza connessione. Essendo un caso ormai remoto ed essendo questi orari
            // ormai obsoleti, sarebbe meglio eliminare questo codice e far scaricare sempre da web, con un messaggio di errore se non c'è connessione

            transportList.add(new Mezzo("Aliscafo Caremar", 8, 10, 8, 25, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 9, 10, 9, 45, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 12, 10, 12, 40, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 18, 35, 19, 0, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 10, 20, 10, 55, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 13, 50, 14, 20, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 19, 15, 19, 45, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 8, 55, 9, 15, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 7, 30, 8, 10, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 8, 50, 9, 30, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 11, 45, 12, 25, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 13, 10, 13, 50, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 15, 10, 15, 50, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 17, 30, 18, 10, "Napoli Beverello", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 18, 15, 18, 55, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            //listMezzi.add(new Mezzo(getApplicationContext(),"Traghetto Caremar",0,15,1,15,"Napoli Porta di Massa","Procida",0,0,0,0,0,0,"1234567",this));
            transportList.add(new Mezzo("Traghetto Caremar", 6, 25, 7, 25, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 9, 10, 10, 10, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 10, 45, 11, 45, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 15, 15, 16, 15, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 17, 45, 18, 45, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 19, 30, 20, 30, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 22, 15, 23, 15, "Napoli Porta di Massa", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            //listMezzi.add(new Mezzo(getApplicationContext(),"Traghetto Caremar",2,20,3,20,"Procida","Napoli Porta di Massa",0,0,0,0,0,0,"1234567",this));
            transportList.add(new Mezzo("Traghetto Caremar", 7, 40, 8, 40, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 13, 35, 14, 35, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 14, 35, 15, 35, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 16, 15, 17, 15, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 18, 5, 19, 0, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 20, 30, 21, 30, "Procida", "Napoli Porta di Massa", 0, 0, 0, 0, 0, 0, "1234567"));
            //listMezzi.add(new Mezzo(getApplicationContext(),"Traghetto Caremar",22,55,23,55,"Procida","Napoli Porta di Massa",0,0,0,0,0,0,"1234567",this));
            transportList.add(new Mezzo("Aliscafo Caremar", 6, 35, 7, 15, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 7, 55, 8, 35, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 9, 25, 10, 5, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 10, 35, 11, 15, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 13, 30, 14, 10, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 14, 55, 15, 35, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 16, 55, 17, 35, "Procida", "Napoli Beverello", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Gestur", 6, 50, 7, 30, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 9, 40, 10, 20, "Procida", "Pozzuoli", 19, 10, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Gestur", 11, 30, 12, 10, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 14, 5, 14, 45, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 17, 5, 17, 45, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 8, 25, 9, 5, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 10, 40, 11, 20, "Pozzuoli", "Procida", 19, 10, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Gestur", 13, 0, 13, 40, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 15, 30, 16, 10, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Gestur", 17, 55, 18, 35, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 8, 25, 9, 0, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 12, 20, 12, 55, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 16, 20, 16, 55, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 19, 0, 19, 35, "Napoli Beverello", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 7, 30, 8, 5, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 10, 10, 10, 45, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 14, 15, 14, 50, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 18, 5, 18, 40, "Procida", "Napoli Beverello", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Medmar", 4, 10, 4, 50, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "23456"));
            transportList.add(new Mezzo("Medmar", 20, 30, 21, 10, "Pozzuoli", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Medmar", 3, 10, 3, 50, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "23456"));
            transportList.add(new Mezzo("Medmar", 19, 40, 20, 20, "Procida", "Pozzuoli", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Medmar", 5, 0, 5, 20, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "23456"));
            transportList.add(new Mezzo("Medmar", 21, 20, 21, 40, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Medmar", 2, 30, 2, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "23456"));
            transportList.add(new Mezzo("Medmar", 6, 25, 6, 45, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Medmar", 10, 35, 10, 55, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));

            transportList.add(new Mezzo("Traghetto Caremar", 7, 35, 7, 55, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 10, 20, 10, 40, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 11, 5, 11, 25, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 11, 55, 12, 15, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 14, 30, 14, 50, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 16, 25, 18, 45, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 18, 55, 19, 15, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 19, 50, 20, 10, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 20, 35, 20, 55, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 23, 20, 23, 40, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));

            transportList.add(new Mezzo("Traghetto Caremar", 7, 0, 7, 20, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 8, 30, 8, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 11, 30, 11, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 12, 55, 13, 15, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 13, 55, 14, 15, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 15, 30, 15, 50, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 17, 25, 17, 45, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 18, 0, 18, 20, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Traghetto Caremar", 19, 55, 20, 15, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));

            transportList.add(new Mezzo("Aliscafo Caremar", 9, 35, 9, 50, "Procida", "Ischia Porto", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 12, 30, 12, 45, "Procida", "Ischia Porto", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 13, 55, 14, 10, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 15, 55, 16, 10, "Procida", "Ischia Porto", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 19, 0, 19, 15, "Procida", "Ischia Porto", 0, 0, 0, 0, 0, 0, "1234567"));

            transportList.add(new Mezzo("Aliscafo Caremar", 7, 30, 7, 45, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 10, 10, 10, 25, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 13, 5, 13, 20, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 14, 30, 14, 45, "Ischia Porto", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo Caremar", 16, 30, 16, 45, "Ischia Porto", "Procida", 1, 11, 2011, 1, 6, 2012, "1234567"));

            transportList.add(new Mezzo("Aliscafo SNAV", 7, 10, 7, 25, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 9, 45, 10, 0, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 13, 50, 14, 10, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 17, 40, 17, 55, "Procida", "Casamicciola", 0, 0, 0, 0, 0, 0, "1234567"));

            transportList.add(new Mezzo("Aliscafo SNAV", 9, 0, 9, 15, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 13, 15, 13, 30, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 17, 5, 17, 20, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
            transportList.add(new Mezzo("Aliscafo SNAV", 19, 45, 10, 0, "Casamicciola", "Procida", 0, 0, 0, 0, 0, 0, "1234567"));
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
        ReadAlertsHandler handler = new ReadAlertsHandler(this);
        handler.setAnalytics(analytics);
        handler.start();
    }

    public void aggiornaLista() {
        //Non è chiaro perchè il controllo del locale debba essere fatto proprio qui!!!
        analytics.send(ANALYTICS_CATEGORY_APP_EVENT, "Aggiorna Lista");

        selectMezzi = new ArrayList<>();
        Comparator<Mezzo> comparator = (m1, m2) -> {
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
        oraLimite.add(Calendar.HOUR_OF_DAY, 24);

        //qui riempio aalvMezzi in base agli input e ai dati di listMezzi
        for (int i = 0; i < transportList.size(); i++) {
            //per ogni mezzo valuta se ci interessa
            Calendar oraNave = (Calendar) transportList.get(i).oraPartenza.clone();
            oraNave.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
            oraNave.set(Calendar.MONTH, c.get(Calendar.MONTH));
            oraNave.set(Calendar.YEAR, c.get(Calendar.YEAR));
            if ((oraNave.get(Calendar.HOUR_OF_DAY) < c.get(Calendar.HOUR_OF_DAY)) || (oraNave.get(Calendar.HOUR_OF_DAY) == c.get(Calendar.HOUR_OF_DAY)) && (oraNave.get(Calendar.MINUTE) < c.get(Calendar.MINUTE)))
                oraNave.add(Calendar.DAY_OF_MONTH, 1);

            if (naveEspanso.contains(transportList.get(i).nave) || nave.equals(getString(R.string.tutti))) {
                if ((transportList.get(i).portoPartenza.equals((portoPartenza))) || (portoPartenzaEspanso.contains(transportList.get(i).portoPartenza)) || (portoPartenza.equals(getString(R.string.tutti)))) {
                    if ((transportList.get(i).portoArrivo.equals((portoArrivo))) || (portoArrivoEspanso.contains(transportList.get(i).portoArrivo)) || (portoArrivo.equals(getString(R.string.tutti)))) {
                        if (transportList.get(i).inizioEsclusione.after(oraNave) || transportList.get(i).fineEsclusione.before(oraNave))
                            if (transportList.get(i).giorniSettimana.contains(String.valueOf(oraNave.get(Calendar.DAY_OF_WEEK))))
                                if (oraNave.before(oraLimite)) {
                                    if (oraNave.get(Calendar.DAY_OF_MONTH) != c.get(Calendar.DAY_OF_MONTH))
                                        transportList.get(i).setGiornoSeguente(true);
                                    else
                                        transportList.get(i).setGiornoSeguente(false);
                                    //listMezzi.get(i).setId(i);
                                    selectMezzi.add(transportList.get(i));
                                }
                    }
                }
            }


        }

        selectMezzi.sort(comparator);

        for (int i = 0; i < selectMezzi.size(); i++) {
            Mezzo route = selectMezzi.get(i);

            route.setOrderInList(i);
            String s = route.nave + " - " + route.portoPartenza + " - " + route.portoArrivo + " - ";
            if (route.oraPartenza.get(Calendar.HOUR_OF_DAY) < 10)
                s += "0";

            s += route.oraPartenza.get(Calendar.HOUR_OF_DAY) + ":";

            if (route.oraPartenza.get(Calendar.MINUTE) < 10)
                s += "0";

            s += route.oraPartenza.get(Calendar.MINUTE) + " ";

            s += getWeatherConditionsString(this, route);

            //Qui aggiungo le segnalazioni

            String spc = "";
            if (route.segnalazionePiuComune() > -1)
                spc = ragioni[route.segnalazionePiuComune()];
            if (route.tot > 0 || route.conferme > 0) {
                //Trasformato con resources
                if (route.tot > 0) { //c'? qualcosa
                    if (route.conc) {
                        s += " -  " + route.tot;
                        if (route.tot == 1)
                            s += " " + getString(R.string.segnalazione);
                        else
                            s += " " + getString(R.string.segnalazioni);
                        s += " " + getString(R.string.diProblemi) + " (" + spc + ")";
                    } else {
                        s += " - " + getString(R.string.possibiliProblemi) + " (" + route.tot;
                        if (route.tot == 1)
                            s += " " + getString(R.string.segnalazione) + ")";
                        else
                            s += " " + getString(R.string.segnalazioni) + ")";
                        s += ", " + getString(R.string.inParticolare) + " " + spc;
                    }
                }
                if (route.conferme > 0) {
                    s += " - " + route.conferme;
                    if (route.conferme == 1)
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
        msgToast = "";
        if (!(portoPartenza.equals(getString(R.string.tutti))))
            msgToast += (getString(R.string.secondoMeVuoiPartireDa) + " " + portoPartenza + "\n");
        if (updateTimesIS != null) {
            int mese = updateTimesIS.get(Calendar.MONTH);
            if (mese == 0) mese = 12;
            msgToast += getString(R.string.orariAggiornatiAl) + " " + updateTimesIS.get(Calendar.DATE) + "/" + mese + "/" + updateTimesIS.get(Calendar.YEAR);
        }

        if (!aggiorna)
            Toast.makeText(this, msgToast, Toast.LENGTH_LONG).show();
    }

    private void setSpnPortoArrivo(Spinner spnPortoArrivo, final ArrayAdapter<CharSequence> adapter3) {
        //trova il valore corretto nello spinner
        for (int i = 0; i < spnPortoArrivo.getCount(); i++) {
            if (adapter3.getItem(i).equals(portoArrivo)) {
                spnPortoArrivo.setSelection(i);
            }
        }
    }

    private void setSpnPortoPartenza(Spinner spnPortoPartenza, ArrayAdapter<CharSequence> adapter2) {
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

        // l'accesso al GPS potrebbe non essere garantito per ragioni legate a Google Play
        // Bisogna gestire l'eccezione e restituire un valore di default che si potrà settare in altro punto dell'app
        try {
            l = myManager.getLastKnownLocation(BestProvider);
            Log.d("ACTIVITY", "Posizione:" + l.getLongitude() + "," + l.getLatitude());
        } catch (Exception e) {
            Log.e("Activity", "GPS: ", e);
        }
        if (l == null)
            return getString(R.string.tutti);
        //Coordinate angoli Procida
        if ((l.getLatitude() > 40.7374) && (l.getLatitude() < 40.7733) && (l.getLongitude() > 13.9897) && (l.getLongitude() < 14.0325)) {
            analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Procida");
            return "Procida";
        }
        //Coordinate angoli Isola d'Ischia
        if ((l.getLatitude() > 40.6921) && (l.getLatitude() < 40.7626) && (l.getLongitude() > 13.8465) && (l.getLongitude() < 13.9722))
            //Isola d'Ischia
            if (calcolaDistanza(l, 13.9063, 40.7496) > calcolaDistanza(l, 13.9602, 40.7319)) {
                analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Ischia");
                return "Ischia";
            } else {
                analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Casamicciola");
                return "Casamicciola";
            }
        //Inserire coordinate Napoli (media porti) e Pozzuoli
        double distNapoli = calcolaDistanza(l, 14.2575, 40.84);
        Log.d("OrariProcida", "d(Napoli)=" + distNapoli);
        double distPozzuoli = calcolaDistanza(l, 14.1179, 40.8239);
        Log.d("OrariProcida", "d(Pozzuoli)=" + distPozzuoli);
        double distMonteProcida = calcolaDistanza(l, 14.05, 40.8);
        if (distMonteProcida < 1500) {
            analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Monte di Procida");
            return "Monte di Procida";
        }
        if (distPozzuoli < distNapoli) {
            if (distPozzuoli < 15000) {
                analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Pozzuoli");
                return "Pozzuoli";
            } else {
                analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Napoli o Pozzuoli");
                return "Napoli o Pozzuoli";
            }
        } else {
            if (distNapoli < 15000) {
                if (distNapoli > 1000) {
                    analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Napoli");
                    return "Napoli";
                } else {
                    if (calcolaDistanza(l, 14.2548, 40.8376) < calcolaDistanza(l, 14.2602, 40.8424)) {
                        analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Napoli Beverello");
                        return "Napoli Beverello";
                    } else {
                        analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Napoli Porta di Massa");
                        return "Napoli Porta di Massa";
                    }
                }
            } else {
                analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Napoli o Pozzuoli");
                return "Napoli o Pozzuoli";
            }
        }
    }

    private double calcolaDistanza(Location location, double lon, double lat) {
        //calcola distanza da obiettivo
        double deltaLong = Math.abs(lon - location.getLongitude());
        double deltaLat = Math.abs(lat - location.getLatitude());
        double delta = (Math.sqrt(deltaLong * deltaLong + deltaLat * deltaLat));
        delta = delta * 60 * 1852;
        return Math.ceil(delta);
    }

    private void onWeatherUpdate(WeatherUpdate update) {
        if (update.isValid()) {
            List<Osservazione> forecasts = update.getData();

            aggiornamentoMeteo = Calendar.getInstance();
            meteo.setForecasts(forecasts);

            aggiornaLista();
            showWeatherUpdateMessage(meteo.getForecasts().get(0));
        } else {
            // TODO: handle exception
            Log.e("MainActivity", "OnWeatherUpdate: ", update.getError());
            Toast.makeText(this, "Could not update weather: " + update.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showWeatherUpdateMessage(Osservazione forecast) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy", Locale.getDefault());

        String message = getString(R.string.updated) + " " + dateFormatter.format(forecast.getTime()) + "\n" +
                getString(R.string.condimeteo) + " " + getWindBeaufortString(forecast) +
                " (" + (int) forecast.getWindSpeed() + " km/h) " + getString(R.string.da) + " " + getWindDirectionString(forecast);

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String getWindBeaufortString(Osservazione forecast) {
        return getWindBeaufortString((int) forecast.getWindBeaufort());
    }

    private String getWindBeaufortString(int force) {
        switch (force) {
            case 0:
                return getString(R.string.calma);
            case 1:
                return getString(R.string.bavaDiVento);
            case 2:
                return getString(R.string.brezzaLeggera);
            case 3:
                return getString(R.string.brezzaTesa);
            case 4:
                return getString(R.string.ventoModerato);
            case 5:
                return getString(R.string.ventoTeso);
            case 6:
                return getString(R.string.ventoFresco);
            case 7:
                return getString(R.string.ventoForte);
            case 8:
                return getString(R.string.burrasca);
            case 9:
                return getString(R.string.burrascaForte);
            case 10:
                return getString(R.string.tempesta);
            case 11:
                return getString(R.string.fortunale);
            case 12:
                return getString(R.string.uragano);
        }

        return getString(R.string.errore);
    }

    private String getWindDirectionString(Osservazione forecast) {
        return getWindDirectionString(forecast.getWindDirection());
    }

    private String getWindDirectionString(Osservazione.Direction direction) {
        switch (direction) {
            case N:
                return getString(R.string.nord);
            case NW:
                return getString(R.string.nordOvest);
            case NE:
                return getString(R.string.nordEst);
            case E:
                return getString(R.string.est);
            case SE:
                return getString(R.string.sudEst);
            case S:
                return getString(R.string.sud);
            case SW:
                return getString(R.string.sudOvest);
            case W:
                return getString(R.string.ovest);
            default:
                return null; // NOTE: it can't happen, here just to make the compiler happy
        }
    }

    private String getWeatherConditionsString(Context context, Mezzo route) {
        double extraWind = meteo.getForecast(context, route);

        if (extraWind <= 0)
            return "";
        else if (extraWind <= 1)
            return " - " + getString(R.string.pocoProbabile);
        else if (extraWind <= 2)
            return " - " + getString(R.string.aRischio);
        else if (extraWind <= 3)
            return " - " + getString(R.string.corsaQuasi);
        else
            return " - " + getString(R.string.corsaImpossibile);
    }


}
