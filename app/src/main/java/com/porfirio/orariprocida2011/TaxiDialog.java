package com.porfirio.orariprocida2011;


import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TaxiDialog extends Dialog implements OnClickListener{
	private TextView tn1;
	private TextView tn2;
	private TextView tn3;
	private TextView tn4;
	private TextView tn5;
	private TextView tn6;
	private ArrayList<Taxi> taxiList;
	
	public TaxiDialog(Context context) {
		super(context);
		setContentView(R.layout.taxi);
		
		tn1 = (TextView) findViewById(R.id.tn1); tn1.setText(null);
		tn2 = (TextView) findViewById(R.id.tn2); tn2.setText(null);
		tn3 = (TextView) findViewById(R.id.tn3); tn3.setText(null);
		tn4 = (TextView) findViewById(R.id.tn4); tn4.setText(null);
		tn5 = (TextView) findViewById(R.id.tn5); tn5.setText(null);
		tn6 = (TextView) findViewById(R.id.tn6); tn6.setText(null);
			
	    Button btnBack = (Button)findViewById(R.id.btnBackTaxi);    
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
	    taxiList.add(new Taxi("Monte di Procida","Via Faro, Bacoli","3395352014"));
	    //TODO: Aggiungere Monte di Procida
	}

	public void fill(String porto) {
		ArrayList<Taxi> taxiPortoList=new ArrayList<Taxi>();
		for (int i=0;i<taxiList.size();i++)
			if (porto.contains(taxiList.get(i).getPorto()) && !(porto.contentEquals("Monte di Procida")&&taxiList.get(i).getPorto().contentEquals("Procida")))
				taxiPortoList.add(taxiList.get(i));
				
		if (taxiPortoList.size()>=1){
			tn1.setText(taxiPortoList.get(0).getCompagnia()+" : "+taxiPortoList.get(0).getNumero());
			Linkify.addLinks(tn1, Linkify.PHONE_NUMBERS);
		}
		if (taxiPortoList.size()>=2){
			tn2.setText(taxiPortoList.get(1).getCompagnia()+" : "+taxiPortoList.get(1).getNumero());
			Linkify.addLinks(tn2, Linkify.PHONE_NUMBERS);
		}
		if (taxiPortoList.size()>=3){
			tn3.setText(taxiPortoList.get(2).getCompagnia()+" : "+taxiPortoList.get(2).getNumero());
			Linkify.addLinks(tn3, Linkify.PHONE_NUMBERS);
		}
		if (taxiPortoList.size()>=4){
			tn4.setText(taxiPortoList.get(3).getCompagnia()+" : "+taxiPortoList.get(3).getNumero());
			Linkify.addLinks(tn4, Linkify.PHONE_NUMBERS);
		}
		if (taxiPortoList.size()>=5){
			tn5.setText(taxiPortoList.get(4).getCompagnia()+" : "+taxiPortoList.get(4).getNumero());
			Linkify.addLinks(tn5, Linkify.PHONE_NUMBERS);
		}
		if (taxiPortoList.size()>=6){
			tn6.setText(taxiPortoList.get(5).getCompagnia()+" : "+taxiPortoList.get(5).getNumero());
			Linkify.addLinks(tn6, Linkify.PHONE_NUMBERS);
		}
	        
       
        
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
