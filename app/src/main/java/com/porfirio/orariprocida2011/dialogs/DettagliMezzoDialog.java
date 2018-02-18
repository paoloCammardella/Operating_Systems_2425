package com.porfirio.orariprocida2011.dialogs;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.entity.Mezzo;

import java.util.ArrayList;
import java.util.Calendar;

public class DettagliMezzoDialog extends DialogFragment implements OnClickListener {

	public ArrayAdapter<String> aalvNumeri;
	public ListView lvNumeri;
	public ArrayList<String> listNumeri;
	private Mezzo mezzo;
	private TextView txtMezzo;
	private TextView txtPartenza;
//	private TextView txtTelefonoCompagnia;
//	private TextView txtNomeCompagnia;
	private TextView txtArrivo;
	private TextView txtCosto;
	private Context callingContext;
    private TaxiDialog taxiDialog;
    private SegnalazioneDialog segnalazioneDialog;
	private TextView txtAuto;
	private Calendar calen;
	private OrariProcida2011Activity callingActivity;
	private FragmentManager fragmentManager;
	private BiglietterieDialog biglietterieDialog = new BiglietterieDialog();
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

		txtMezzo = (TextView) view.findViewById(R.id.txtMezzo);
		txtPartenza = (TextView) view.findViewById(R.id.txtPartenza);
		txtArrivo = (TextView) view.findViewById(R.id.txtArrivo);
//		txtOrario = (TextView) findViewById(R.id.txtOrario);
//		txtOraPartenza = (TextView) findViewById(R.id.txtOraPartenza);
//		txtOraArrivo = (TextView) findViewById(R.id.txtOraArrivo);
//		txtPortoPartenza = (TextView) findViewById(R.id.txtPortoPartenza);
//		txtPortoArrivo = (TextView) findViewById(R.id.txtPortoArrivo);
		TextView txtPeriodo = (TextView) view.findViewById(R.id.txtPeriodo);
		txtPeriodo.setText("");
		TextView txtGiorniSettimana = (TextView) view.findViewById(R.id.txtGiorniSettimana);
		txtGiorniSettimana.setText("");
//		txtNomeCompagnia = (TextView) findViewById(R.id.txtNomeCompagnia);
//		txtTelefonoCompagnia = (TextView) findViewById(R.id.txtTelefonoCompagnia);
		txtCosto = (TextView) view.findViewById(R.id.txtCosto);
		txtAuto = (TextView) view.findViewById(R.id.txtAuto);

		Button btnReturnToHome = (Button) view.findViewById(R.id.btnReturnToHome);
		btnReturnToHome.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View v) {
	    		dismiss();
	    	}
	    });

		Button btnTaxi = (Button) view.findViewById(R.id.btnTaxi);
		btnTaxi.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View v) {
				taxiDialog.show(fragmentManager, "fragment_edit_name");
			}
	    });

		Button btnBiglietterie = (Button) view.findViewById(R.id.btnBiglietterie);
		btnBiglietterie.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View v) {
				biglietterieDialog.show(fragmentManager, "fragment_edit_name");
			}
	    });

		Button btnConfermaOSmentisci = (Button) view.findViewById(R.id.btnConfermaOSmentisci);
		btnConfermaOSmentisci.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View v) {
	    	    if (!callingActivity.isOnline())
	    	    	Toast.makeText(getContext(), callingActivity.getString(R.string.soloOnline), Toast.LENGTH_SHORT).show();
	    		else
					segnalazioneDialog.show(fragmentManager, "fragment_edit_name");
			}
	    });

		txtMezzo.setText("    "+mezzo.nave+"    ");
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
			if (c.nome.contains("Ippocampo") || c.nome.contentEquals("Procida Lines") || mezzo.nave.contains("Aliscafo") || mezzo.nave.contains("Aladino"))
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
		segnalazioneDialog.setListCompagnia(lc);
		//segnalazioneDialog.fill(lc);


//
//        for (int i=0;i<c.nomeNumeroTelefono.size();i++){
//        	aalvNumeri.add(c.nomeNumeroTelefono.get(i)+" : "+c.numeroTelefono.get(i));
////        	Linkify.addLinks( (TextView) lvNumeri.getChildAt(i), Linkify.PHONE_NUMBERS);
//        }


		return view;
	}


	public void setMezzo(Mezzo m) {
		mezzo = m;
	}

	@Override
	public void onClick(View arg0) {
		this.dismiss();
	}


	void fill(ArrayList<Compagnia> listCompagnia) {

	}

	public void setListCompagnia(ArrayList<Compagnia> listCompagnia) {
		lc = listCompagnia;
	}
}
