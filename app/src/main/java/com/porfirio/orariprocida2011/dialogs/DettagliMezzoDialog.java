package com.porfirio.orariprocida2011.dialogs;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.analytics.HitBuilders;
import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.entity.Mezzo;

import java.util.ArrayList;
import java.util.Calendar;

public class DettagliMezzoDialog extends DialogFragment implements OnClickListener {

	private final BiglietterieDialog biglietterieDialog = new BiglietterieDialog();
	private Mezzo mezzo;
	private Context callingContext;
    private TaxiDialog taxiDialog;
    private SegnalazioneDialog segnalazioneDialog;
	private Calendar calen;
	private OrariProcida2011Activity callingActivity;
	private FragmentManager fragmentManager;
	private ArrayList<Compagnia> lc;

	public DettagliMezzoDialog() {
		// Empty constructor required for DialogFragment
	}

	public void setDettagliMezzoDialog(FragmentManager fm, OrariProcida2011Activity a, Context context, Calendar cal) {
		fragmentManager = fm;
		callingActivity=a;
		callingContext=context;
		fragmentManager = fm;
		calen=cal;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {


		View view = inflater.inflate(R.layout.dettaglimezzo, container);
		//setContentView(R.layout.dettaglimezzo);

		TextView txtMezzo = view.findViewById(R.id.txtMezzo);
		TextView txtPartenza = view.findViewById(R.id.txtPartenza);
		TextView txtArrivo = view.findViewById(R.id.txtArrivo);
//		txtOrario = (TextView) findViewById(R.id.txtOrario);
//		txtOraPartenza = (TextView) findViewById(R.id.txtOraPartenza);
//		txtOraArrivo = (TextView) findViewById(R.id.txtOraArrivo);
//		txtPortoPartenza = (TextView) findViewById(R.id.txtPortoPartenza);
//		txtPortoArrivo = (TextView) findViewById(R.id.txtPortoArrivo);
		TextView txtPeriodo = view.findViewById(R.id.txtPeriodo);
		txtPeriodo.setText("");
		TextView txtGiorniSettimana = view.findViewById(R.id.txtGiorniSettimana);
		txtGiorniSettimana.setText("");
//		txtNomeCompagnia = (TextView) findViewById(R.id.txtNomeCompagnia);
//		txtTelefonoCompagnia = (TextView) findViewById(R.id.txtTelefonoCompagnia);
		TextView txtCosto = view.findViewById(R.id.txtCosto);
		TextView txtAuto = view.findViewById(R.id.txtAuto);

		Button btnReturnToHome = view.findViewById(R.id.btnReturnToHome);
		btnReturnToHome.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View v) {
	    		dismiss();
	    	}
	    });

		Button btnTaxi = view.findViewById(R.id.btnTaxi);
		btnTaxi.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View v) {
				callingActivity.mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("App Event")
                        .setAction("Click Taxi Dialog")
                        .build());
				taxiDialog.show(fragmentManager, "fragment_edit_name");
			}
	    });

		Button btnBiglietterie = view.findViewById(R.id.btnBiglietterie);
		btnBiglietterie.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View v) {
				callingActivity.mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("App Event")
                        .setAction("Click Biglietterie Dialog")
                        .build());
				biglietterieDialog.show(fragmentManager, "fragment_edit_name");
			}
	    });

		Button btnConfermaOSmentisci = view.findViewById(R.id.btnConfermaOSmentisci);
		btnConfermaOSmentisci.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View v) {
	    	    if (!callingActivity.isOnline())
	    	    	Toast.makeText(getContext(), callingActivity.getString(R.string.soloOnline), Toast.LENGTH_SHORT).show();
                else {
                    callingActivity.mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("App Event")
                            .setAction("Click Segnalazione Dialog")
                            .build());
                    segnalazioneDialog.show(fragmentManager, "fragment_edit_name");
                }
			}
	    });

        final String text = "    " + mezzo.nave + "    ";
        txtMezzo.setText(text);
        //String s=new String();
		String s=callingContext.getString(R.string.parteAlle)+" "+mezzo.oraPartenza.get(Calendar.HOUR_OF_DAY)+":"+mezzo.oraPartenza.get(Calendar.MINUTE);
		s+=" "+callingContext.getString(R.string.del)+" "+mezzo.oraPartenza.get(Calendar.DAY_OF_MONTH)+"/"+(mezzo.oraPartenza.get(Calendar.MONTH)+1)+"/"+mezzo.oraPartenza.get(Calendar.YEAR);
		s+=" "+callingContext.getString(R.string.da)+" "+mezzo.portoPartenza;
		txtPartenza.setText(s);
		//s=new String();
		s = callingContext.getString(R.string.arrivaAlle) + " " + mezzo.oraArrivo.get(Calendar.HOUR_OF_DAY) + ":" + mezzo.oraArrivo.get(Calendar.MINUTE);
		s+=" "+callingContext.getString(R.string.del)+" "+mezzo.oraArrivo.get(Calendar.DAY_OF_MONTH)+"/"+(mezzo.oraArrivo.get(Calendar.MONTH)+1)+"/"+mezzo.oraArrivo.get(Calendar.YEAR);
		s+=" "+callingContext.getString(R.string.a)+" "+mezzo.portoArrivo;
		txtArrivo.setText(s);


//		if (mezzo.isEsclusione())
//			txtPeriodo.setText(mezzo.inizioEsclusione.get(Calendar.DAY_OF_MONTH));
//		txtGiorniSettimana.setText(mezzo.giorniSettimana);

//        listNumeri = new ArrayList <String>();
//        lvNumeri=(ListView)findViewById(R.id.listViewNumeri);
//        aalvNumeri = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_list_item_1);
//        lvNumeri.setAdapter(aalvNumeri);
//
		s="";
		if (mezzo.getCostoResidente()>0.0){
			if (mezzo.isCircaResidente())
				s+=callingContext.getString(R.string.circa)+" ";
			s+=callingContext.getString(R.string.costo)+" "+mezzo.getCostoResidente()+" euro ";
		} else
			//s+=callingContext.getString(R.string.costoResidenteNonNoto)+" ";
			s += " ";
		if (mezzo.getCostoIntero()>0.0){
			if (mezzo.isCircaIntero())
				s+=callingContext.getString(R.string.circa)+" ";
			s += callingContext.getString(R.string.residenteO) + " " + mezzo.getCostoIntero() + " euro ";
			s+=" "+callingContext.getString(R.string.intero);
		} else
			//s+=callingContext.getString(R.string.costoInteroNonNoto)+" ";
			s += " ";
		txtCosto.setText(s);

		//trova compagnia c
		Compagnia c = null;
		for (int i = 0; i < lc.size(); i++) {
			if (mezzo.nave.contains(lc.get(i).nome))
				c = lc.get(i);
		}

		//Aggiunto Aladino
		if (c!=null) {
			if (c.nome.contains("Ippocampo") || c.nome.contentEquals("Procida Lines") || mezzo.nave.contains("Aliscafo") || mezzo.nave.contains("Aladino") || mezzo.nave.contains("Motonave") || mezzo.nave.contains("Scotto Line"))
				txtAuto.setText(callingContext.getString(R.string.trasportaSoloPasseggeri));
			else
				txtAuto.setText( callingContext.getString(R.string.trasportaAutoPasseggeri));

			//biglietterieDialog = new BiglietterieDialog(this.getContext());
			//FragmentManager fm = callingContext.getSupportFragmentManager();
			//BiglietterieDialog biglietterieDialog = new BiglietterieDialog();
			biglietterieDialog.setCompagnia(c);
		} else
			txtAuto.setText("");
		taxiDialog = new TaxiDialog();
		taxiDialog.setPorto(mezzo.portoPartenza);

		segnalazioneDialog = new SegnalazioneDialog();
		segnalazioneDialog.setOrarioRef(calen);
		segnalazioneDialog.setMezzo(mezzo);
		segnalazioneDialog.setCallingContext(this.getContext());
		segnalazioneDialog.setCallingActivity(callingActivity);
		segnalazioneDialog.setListCompagnia(lc);
		//segnalazioneDialog.fill(lc);


		return view;
	}


	public void setMezzo(Mezzo m) {
		mezzo = m;
	}

	@Override
	public void onClick(View arg0) {
		this.dismiss();
	}


	public void setListCompagnia(ArrayList<Compagnia> listCompagnia) {
		lc = listCompagnia;
	}
}
