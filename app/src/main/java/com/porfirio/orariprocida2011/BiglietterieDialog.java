package com.porfirio.orariprocida2011;


import android.app.Dialog;
import android.content.Context;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BiglietterieDialog extends Dialog implements OnClickListener{
	private TextView pn1;
	private TextView pn2;
	private TextView pn3;
	private TextView pn4;
	private TextView pn5;
	private TextView pn6;
	
	public BiglietterieDialog(Context context) {
		super(context);
		setContentView(R.layout.biglietterie);
		
		pn1 = (TextView) findViewById(R.id.pn1); pn1.setText(null);
		pn2 = (TextView) findViewById(R.id.pn2); pn2.setText(null);
		pn3 = (TextView) findViewById(R.id.pn3); pn3.setText(null);
		pn4 = (TextView) findViewById(R.id.pn4); pn4.setText(null);
		pn5 = (TextView) findViewById(R.id.pn5); pn5.setText(null);
		pn6 = (TextView) findViewById(R.id.pn6); pn6.setText(null);
			
	    Button btnBack = (Button)findViewById(R.id.btnBack);    
	    btnBack.setOnClickListener(new View.OnClickListener(){
	    	@Override
	    	public void onClick(View v) {
	    		dismiss();
	    	}
	    });	
	}

	public void fill(Compagnia c) {	
		int s=c.nomeNumeroTelefono.size();
		if (s>=1){
			pn1.setText(c.nomeNumeroTelefono.get(0)+" : "+c.numeroTelefono.get(0));
			Linkify.addLinks(pn1, Linkify.PHONE_NUMBERS);
		}
		if (s>=2){
			pn2.setText(c.nomeNumeroTelefono.get(1)+" : "+c.numeroTelefono.get(1));
			Linkify.addLinks(pn2, Linkify.PHONE_NUMBERS);
		}
		if (s>=3){
			pn3.setText(c.nomeNumeroTelefono.get(2)+" : "+c.numeroTelefono.get(2));
			Linkify.addLinks(pn3, Linkify.PHONE_NUMBERS);
		}
		if (s>=4){
			pn4.setText(c.nomeNumeroTelefono.get(3)+" : "+c.numeroTelefono.get(3));
			Linkify.addLinks(pn4, Linkify.PHONE_NUMBERS);
		}
		if (s>=5){
			pn5.setText(c.nomeNumeroTelefono.get(4)+" : "+c.numeroTelefono.get(4));
			Linkify.addLinks(pn5, Linkify.PHONE_NUMBERS);
		}
		if (s>=6){
			pn6.setText(c.nomeNumeroTelefono.get(5)+" : "+c.numeroTelefono.get(5));
			Linkify.addLinks(pn6, Linkify.PHONE_NUMBERS);
		}
	        
       
        
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
