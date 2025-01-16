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

import com.porfirio.orariprocida2011.threads.transports.DownloadTransportsHandler;
import com.porfirio.orariprocida2011.threads.transports.TransportsUpdate;
import com.porfirio.orariprocida2011.threads.alerts.Alert;
import com.porfirio.orariprocida2011.threads.alerts.AlertUpdate;
import com.porfirio.orariprocida2011.threads.alerts.OnRequestAlertsDAO;
import com.porfirio.orariprocida2011.utils.Analytics;
import com.porfirio.orariprocida2011.utils.AnalyticsApplication;
import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.threads.weather.OnRequestWeatherDAO;
import com.porfirio.orariprocida2011.threads.weather.WeatherUpdate;
import com.porfirio.orariprocida2011.dialogs.DettagliMezzoDialog;
import com.porfirio.orariprocida2011.dialogs.SegnalazioneDialog;
import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.entity.Meteo;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.entity.Osservazione;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrariProcida2011Activity extends FragmentActivity {

    private static final String ANALYTICS_CATEGORY_APP_EVENT = "App Event";
    private static final String ANALYTICS_CATEGORY_UI_EVENT = "UI Event";
    private static final String ANALYTICS_CATEGORY_USER_EVENT = "User";

    private static FragmentManager fm;

    public Calendar c;
    public AlertDialog aboutDialog;
    public Meteo meteo;
    //    public AlertDialog meteoDialog;
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

    private OnRequestWeatherDAO weatherDAO;
    private DownloadTransportsHandler transportsDAO;
    private OnRequestAlertsDAO alertsDAO;
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
                transportsDAO.requestUpdate();
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

        weatherDAO = new OnRequestWeatherDAO();
        weatherDAO.getUpdates().observe(this, this::onWeatherUpdate);
        weatherDAO.requestUpdate();

        transportsDAO = new DownloadTransportsHandler(analytics);
        transportsDAO.getUpdates().observe(this, this::onTransportsUpdate);
        transportsDAO.requestUpdate();

        alertsDAO = new OnRequestAlertsDAO(analytics);
        alertsDAO.getUpdates().observe(this, this::onAlertsUpdate);

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

//        builder = new AlertDialog.Builder(this);
//        builder.setCancelable(false)
//                .setPositiveButton("OK", (dialog, id) -> dialog.cancel());
//        meteoDialog = builder.create();

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

        dettagliMezzoDialog = new DettagliMezzoDialog(alertsDAO);
        dettagliMezzoDialog.setDettagliMezzoDialog(fm, this, this, c);
        dettagliMezzoDialog.setAnalytics(analytics);

        segnalazioneDialog = new SegnalazioneDialog(alertsDAO);
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

        riempiLista();
        setSpinner();
//        aggiornaLista();

        if (!portoPartenza.equals(getString(R.string.tutti)) && !aggiorna)
            Toast.makeText(this, (getString(R.string.secondoMeVuoiPartireDa) + " " + portoPartenza), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        alertsDAO.getUpdates().removeObservers(this);
        transportsDAO.getUpdates().removeObservers(this);
        weatherDAO.getUpdates().removeObservers(this);
        weatherDAO.close();
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
            return getString(R.string.tutti);
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

    private void onAlertsUpdate(AlertUpdate update) {
        if (update.isValid()) {

            // FIXME: highly inefficient, transport list is sorted by time so it could be possible to do a binary search
            for (Alert alert : update.getData()) {
                for (Mezzo transport : transportList) {
                    if (sameTransport(transport, alert))
                        transport.addReason(alert.getReason());
                }
            }

            aggiornaLista();
        } else {
            // TODO: handle exception
            Log.e("MainActivity", "OnAlertsUpdate: ", update.getError());
            Toast.makeText(this, "Could not update alerts: " + update.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean sameTransport(Mezzo transport, Alert alert) {
        LocalTime transportDepartureTime = transport.getDepartureTime();
        LocalTime transportArrivalTime = transport.getArrivalTime();
        LocalDate transportDate = LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));

        if (transport.getGiornoSeguente())
            transportDate = transportDate.plusDays(1);

        return alert.getTransport().equals(transport.nave)
                && alert.getDepartureTime().equals(transportDepartureTime)
                && alert.getArrivalTime().equals(transportArrivalTime)
                && alert.getDepartureLocation().equals(transport.portoPartenza)
                && alert.getArrivalLocation().equals(transport.portoArrivo)
                && alert.getTransportDate().equals(transportDate);
    }

    private void onTransportsUpdate(TransportsUpdate update) {
        if (update.isValid()) {
            transportList.clear();
            transportList.addAll(update.getData());
            aggiornaLista();

            alertsDAO.requestUpdate();

            Toast.makeText(this, getString(R.string.orariAggiornatiAl) + " " + DateTimeFormatter.ISO_LOCAL_DATE.format(update.getUpdateTime()), Toast.LENGTH_LONG).show();
        } else {
            // TODO: handle exception
            Log.e("MainActivity", "OnTransportsUpdate: ", update.getError());
            Toast.makeText(this, "Could not update transports: " + update.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void onWeatherUpdate(WeatherUpdate update) {
        if (update.isValid()) {
            List<Osservazione> forecasts = update.getData();

            meteo.setForecasts(forecasts);

            aggiornaLista();

            // TODO: before it would show a complete dialog, as of now I changed it to just display a toast
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
