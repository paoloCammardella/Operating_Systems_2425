package com.porfirio.orariprocida2011.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.threads.alerts.Alert;
import com.porfirio.orariprocida2011.threads.alerts.AlertsDAO;
import com.porfirio.orariprocida2011.utils.Analytics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class SegnalazioneDialog extends DialogFragment implements OnClickListener {
    private Mezzo mezzo;
    private Context callingContext;
    private int ragione;
    private EditText txtDettagli;
    private Calendar orarioRef;
    private ArrayList<Compagnia> listCompagnia;

    private final AlertsDAO alertsDAO;
    private Analytics analytics;

    public SegnalazioneDialog(AlertsDAO alertsDAO) {
        this.alertsDAO = Objects.requireNonNull(alertsDAO);
    }

    public void setCallingContext(Context callingContext) {
        this.callingContext = callingContext;
    }

    public void setOrarioRef(Calendar orarioRef) {
        this.orarioRef = orarioRef;
    }

    public void setListCompagnia(ArrayList<Compagnia> listCompagnia) {
        this.listCompagnia = listCompagnia;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.segnalazione, container);
        TextView txtMezzo = view.findViewById(R.id.txtMezzo);
        TextView txtPartenza = view.findViewById(R.id.txtPartenza);
        TextView txtArrivo = view.findViewById(R.id.txtArrivo);
        txtDettagli = view.findViewById(R.id.txtDettagli);

        Spinner spnRagioni = view.findViewById(R.id.spnRagioni);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                callingContext, R.array.strRagioni, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spnRagioni.setAdapter(adapter);

        spnRagioni.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ragione = pos;
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button btnInvia = view.findViewById(R.id.btnInvia);
        btnInvia.setOnClickListener(v -> {
            analytics.send("App Event", "Segnala Avaria");
            //Qui il codice per salvare la segnalazione in coda al file delle segnalazioni
            String resp = scriviSegnalazione(true);
            Toast.makeText(v.getContext(), R.string.ringraziamentoSegnalazione, Toast.LENGTH_SHORT).show();
            dismiss();
        });

        Button btnConferma = view.findViewById(R.id.btnConferma);
        btnConferma.setOnClickListener(v -> {
            analytics.send("App Event", "Conferma Corsa");
            //Qui il codice per salvare la segnalazione in coda al file delle segnalazioni
            String resp = scriviSegnalazione(false);
            Toast.makeText(v.getContext(), R.string.ringraziamentoSegnalazione, Toast.LENGTH_SHORT).show();
            dismiss();
        });

        LocalDate departureDate = LocalDateTime.ofInstant(orarioRef.toInstant(), orarioRef.getTimeZone().toZoneId()).toLocalDate();
        LocalDate arrivalDate = LocalDateTime.ofInstant(orarioRef.toInstant(), orarioRef.getTimeZone().toZoneId()).toLocalDate();

        if (mezzo.getGiornoSeguente()) {
            departureDate = departureDate.plusDays(1);
            arrivalDate = arrivalDate.plusDays(1);
        }

        final String text = "    " + mezzo.nave + "    ";
        txtMezzo.setText(text);
        String s = callingContext.getString(R.string.parteAlle) + " " + DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(mezzo.getDepartureTime());
        s += " " + callingContext.getString(R.string.del) + " " + DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(departureDate);
        s += " " + callingContext.getString(R.string.da) + " " + mezzo.portoPartenza;
        txtPartenza.setText(s);
        //s=new String();
        s = callingContext.getString(R.string.arrivaAlle) + " " + DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(mezzo.getArrivalTime());
        s += " " + callingContext.getString(R.string.del) + " " + DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(arrivalDate);
        s += " " + callingContext.getString(R.string.a) + " " + mezzo.portoArrivo;
        txtArrivo.setText(s);


        //trova compagnia c
        Compagnia c = null;
        for (int i = 0; i < listCompagnia.size(); i++) {
            if (mezzo.nave.contains(listCompagnia.get(i).nome))
                c = listCompagnia.get(i);
        }


        return view;
    }

    //Aggiunta anche la possibilita' di confermare (con un extra button)
//SOstituire con la nuova HTTPUrlConnection
    // https://developer.android.com/reference/java/net/HttpURLConnection.html


    private String scriviSegnalazione(boolean problema) {
        String dettagli = txtDettagli.getText().toString().replaceAll("\r\n|\r|\n", " ");

        int reason = problema ? ragione : Alert.REASON_NO_PROBLEM;
        LocalDate transportDate = LocalDate.of(orarioRef.get(Calendar.YEAR), orarioRef.get(Calendar.MONTH) + 1, orarioRef.get(Calendar.DAY_OF_MONTH));

        if (mezzo.getGiornoSeguente())
            transportDate = transportDate.plusDays(1);

        Alert alert = new Alert(mezzo.nave, reason, dettagli, mezzo.portoPartenza, mezzo.getDepartureTime(), mezzo.portoArrivo, mezzo.getArrivalTime(), transportDate);
        alertsDAO.send(alert);

        return "ok";
    }


    public void setMezzo(Mezzo m) {
        mezzo = m;
    }

    @Override
    public void onClick(View arg0) {
        this.dismiss();
    }

    public void setAnalytics(Analytics analytics) {
        this.analytics = analytics;
    }
}
