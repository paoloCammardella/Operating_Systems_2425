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

import com.google.android.gms.analytics.HitBuilders;
import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.tasks.ScriviSegnalazioneTask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;


public class SegnalazioneDialog extends DialogFragment implements OnClickListener {
	private Mezzo mezzo;
	private Context callingContext;
	private int ragione;
	private EditText txtDettagli;
	private Calendar orarioRef;
	private ArrayList<Compagnia> listCompagnia;
	private OrariProcida2011Activity callingActivity;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

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
        		ragione=pos;
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

		Button btnInvia = view.findViewById(R.id.btnInvia);
		btnInvia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				callingActivity.mTracker.send(new HitBuilders.EventBuilder()
						.setCategory("App Event")
						.setAction("Segnala Avaria")
						.build());
				//Qui il codice per salvare la segnalazione in coda al file delle segnalazioni
	    		String resp=scriviSegnalazione(true);
				Toast.makeText(v.getContext(),R.string.ringraziamentoSegnalazione, Toast.LENGTH_SHORT).show();
				dismiss();
			}
	    });

		Button btnConferma = view.findViewById(R.id.btnConferma);
		btnConferma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				callingActivity.mTracker.send(new HitBuilders.EventBuilder()
						.setCategory("App Event")
						.setAction("Conferma Corsa")
						.build());
	    		//Qui il codice per salvare la segnalazione in coda al file delle segnalazioni
	    		String resp=scriviSegnalazione(false);
				Toast.makeText(v.getContext(),R.string.ringraziamentoSegnalazione, Toast.LENGTH_SHORT).show();
				dismiss();
			}
	    });

        final String text = "    " + mezzo.nave + "    ";
        txtMezzo.setText(text);
        String s;
		s = callingContext.getString(R.string.parteAlle) + " " + mezzo.oraPartenza.get(Calendar.HOUR_OF_DAY) + ":" + mezzo.oraPartenza.get(Calendar.MINUTE);
		s += " " + callingContext.getString(R.string.del) + " " + mezzo.oraPartenza.get(Calendar.DAY_OF_MONTH) + "/" + (mezzo.oraPartenza.get(Calendar.MONTH) + 1) + "/" + mezzo.oraPartenza.get(Calendar.YEAR);
		s += " " + callingContext.getString(R.string.da) + " " + mezzo.portoPartenza;
		txtPartenza.setText(s);
		//s=new String();
		s = callingContext.getString(R.string.arrivaAlle) + " " + mezzo.oraArrivo.get(Calendar.HOUR_OF_DAY) + ":" + mezzo.oraArrivo.get(Calendar.MINUTE);
		s += " " + callingContext.getString(R.string.del) + " " + mezzo.oraArrivo.get(Calendar.DAY_OF_MONTH) + "/" + (mezzo.oraArrivo.get(Calendar.MONTH) + 1) + "/" + mezzo.oraArrivo.get(Calendar.YEAR);
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


	private String scriviSegnalazione(boolean problema){

		String URL = "http://unoprocidaresidente.altervista.org/segnala.php?data=";
		//Pare funzionino le segnalazioni relative al giorno successivo!
		if (mezzo.getGiornoSeguente())
        	orarioRef.add(Calendar.DAY_OF_YEAR, 1);
		URL+=orarioRef.get(Calendar.DAY_OF_MONTH)+","+(1+orarioRef.get(Calendar.MONTH))+","+orarioRef.get(Calendar.YEAR)+"&s=";
		if (mezzo.getGiornoSeguente()) //rimettiamo a posto!
        	orarioRef.add(Calendar.DAY_OF_YEAR, -1);
        String dettagli=txtDettagli.getText().toString().replaceAll ("\r\n|\r|\n", " ");
		try {
			URL+=URLEncoder.encode((mezzo.nave+","+mezzo.oraPartenza.get(Calendar.HOUR_OF_DAY)+","+mezzo.oraPartenza.get(Calendar.MINUTE)
			   +","+mezzo.oraArrivo.get(Calendar.HOUR_OF_DAY)+","+mezzo.oraArrivo.get(Calendar.MINUTE)+","
			   +mezzo.portoPartenza+","+mezzo.portoArrivo+","
			   +mezzo.inizioEsclusione.get(Calendar.DAY_OF_MONTH)+","+mezzo.inizioEsclusione.get(Calendar.MONTH)+","+mezzo.inizioEsclusione.get(Calendar.YEAR)+","
			   +mezzo.fineEsclusione.get(Calendar.DAY_OF_MONTH)+","+mezzo.fineEsclusione.get(Calendar.MONTH)+","+mezzo.fineEsclusione.get(Calendar.YEAR)+","
			   +mezzo.giorniSettimana), "UTF-8");
			if (problema)
				URL = URL + "&motivo=" + ragione + "&dettagli=" + URLEncoder.encode(dettagli, "UTF-8");
			else
				URL=URL+"&motivo=99"; //99 convenzionalmente sta per Conferma
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}
		new ScriviSegnalazioneTask().execute(URL);


		return "ok";
}


	public void setMezzo(Mezzo m){
		mezzo=m;
	}
	
	@Override
	public void onClick(View arg0) {
		this.dismiss();
	}


	public void setCallingActivity(OrariProcida2011Activity c) {
		callingActivity = c;
		return;
	}
}
