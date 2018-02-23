package com.porfirio.orariprocida2011.dialogs;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.entity.Taxi;

import java.util.ArrayList;

public class TaxiDialog extends DialogFragment {
	private TextView tn1;
	private TextView tn2;
	private TextView tn3;
	private TextView tn4;
	private TextView tn5;
	private TextView tn6;
	private ArrayList<Taxi> taxiList;
	private String porto;

	public void setPorto(String porto) {
		this.porto = porto;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.taxi, container);

		tn1 = (TextView) view.findViewById(R.id.tn1);
		tn1.setText(null);
		tn2 = (TextView) view.findViewById(R.id.tn2);
		tn2.setText(null);
		tn3 = (TextView) view.findViewById(R.id.tn3);
		tn3.setText(null);
		tn4 = (TextView) view.findViewById(R.id.tn4);
		tn4.setText(null);
		tn5 = (TextView) view.findViewById(R.id.tn5);
		tn5.setText(null);
		tn6 = (TextView) view.findViewById(R.id.tn6);
		tn6.setText(null);

		Button btnBack = (Button) view.findViewById(R.id.btnBackTaxi);
		btnBack.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View v) {
	    		dismiss();
	    	}
	    });	
	    
	    taxiList=new ArrayList<Taxi>();
	    
	    //TODO: qui l'elenco dei taxi
	    taxiList.add(new Taxi("Procida","Marina Grande","0818968785"));
	    taxiList.add(new Taxi("Napoli","Radiotaxi Free","0815515151"));
	    taxiList.add(new Taxi("Napoli","Radiopartenope","0815560202"));
	    taxiList.add(new Taxi("Napoli","Radionapoli","0815564444"));
	    taxiList.add(new Taxi("Ischia","Piazza Antica Reggia (Ischia Porto)","081984998"));
	    taxiList.add(new Taxi("Ischia","C.so Vittorio Colonna (Ischia Porto)","081993720"));
	    taxiList.add(new Taxi("Ischia","Piazza degli Eroi (Ischia Porto)","081992550"));
	    taxiList.add(new Taxi("Ischia","Piazza Marina (Casamicciola)","081994800"));
	    taxiList.add(new Taxi("Ischia","Arrivo porto turistico (Casamicciola)","081900369"));
	    taxiList.add(new Taxi("Ischia","Piazza Bagni (Casamicciola)","081900881"));
	    taxiList.add(new Taxi("Pozzuoli","Piazza della Repubblica","0815265800"));
	    taxiList.add(new Taxi("Monte di Procida","Via Faro, Bacoli","3349003894"));

		ArrayList<Taxi> taxiPortoList=new ArrayList<Taxi>();
		for (int i=0;i<taxiList.size();i++)
			if (porto.contains(taxiList.get(i).getPorto()) && !(porto.contentEquals("Monte di Procida")&&taxiList.get(i).getPorto().contentEquals("Procida")))
				taxiPortoList.add(taxiList.get(i));

		if (taxiPortoList.size()>=1){
			final String text = taxiPortoList.get(0).getCompagnia() + " : " + taxiPortoList.get(0).getNumero();
			tn1.setText(text);
			Linkify.addLinks(tn1, Linkify.PHONE_NUMBERS);
		}
		if (taxiPortoList.size()>=2){
			final String text = taxiPortoList.get(1).getCompagnia() + " : " + taxiPortoList.get(1).getNumero();
			tn2.setText(text);
			Linkify.addLinks(tn2, Linkify.PHONE_NUMBERS);
		}
		if (taxiPortoList.size()>=3){
			final String text = taxiPortoList.get(2).getCompagnia() + " : " + taxiPortoList.get(2).getNumero();
			tn3.setText(text);
			Linkify.addLinks(tn3, Linkify.PHONE_NUMBERS);
		}
		if (taxiPortoList.size()>=4){
			final String text = taxiPortoList.get(3).getCompagnia() + " : " + taxiPortoList.get(3).getNumero();
			tn4.setText(text);
			Linkify.addLinks(tn4, Linkify.PHONE_NUMBERS);
		}
		if (taxiPortoList.size()>=5){
			final String text = taxiPortoList.get(4).getCompagnia() + " : " + taxiPortoList.get(4).getNumero();
			tn5.setText(text);
			Linkify.addLinks(tn5, Linkify.PHONE_NUMBERS);
		}
		if (taxiPortoList.size()>=6){
			final String text = taxiPortoList.get(5).getCompagnia() + " : " + taxiPortoList.get(5).getNumero();
			tn6.setText(text);
			Linkify.addLinks(tn6, Linkify.PHONE_NUMBERS);
		}
		return view;
	}


}
