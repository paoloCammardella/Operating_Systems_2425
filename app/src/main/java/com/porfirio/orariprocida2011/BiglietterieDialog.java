package com.porfirio.orariprocida2011;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class BiglietterieDialog extends DialogFragment {
	private TextView pn1;
	private TextView pn2;
	private TextView pn3;
	private TextView pn4;
	private TextView pn5;
	private TextView pn6;
	private Compagnia c;

	public BiglietterieDialog() {
		// Empty constructor required for DialogFragment
	}

	public void setCompagnia(Compagnia c) {
		this.c = c;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.biglietterie, container);
		pn1 = (TextView) view.findViewById(R.id.pn1);
		pn2 = (TextView) view.findViewById(R.id.pn2);
		pn3 = (TextView) view.findViewById(R.id.pn3);
		pn4 = (TextView) view.findViewById(R.id.pn4);
		pn5 = (TextView) view.findViewById(R.id.pn5);
		pn6 = (TextView) view.findViewById(R.id.pn6);

		int s = c.nomeNumeroTelefono.size();
		if (s >= 1) {
			pn1.setText(c.nomeNumeroTelefono.get(0) + " : " + c.numeroTelefono.get(0));
			Linkify.addLinks(pn1, Linkify.PHONE_NUMBERS);
		}
		if (s >= 2) {
			pn2.setText(c.nomeNumeroTelefono.get(1) + " : " + c.numeroTelefono.get(1));
			Linkify.addLinks(pn2, Linkify.PHONE_NUMBERS);
		}
		if (s >= 3) {
			pn3.setText(c.nomeNumeroTelefono.get(2) + " : " + c.numeroTelefono.get(2));
			Linkify.addLinks(pn3, Linkify.PHONE_NUMBERS);
		}
		if (s >= 4) {
			pn4.setText(c.nomeNumeroTelefono.get(3) + " : " + c.numeroTelefono.get(3));
			Linkify.addLinks(pn4, Linkify.PHONE_NUMBERS);
		}
		if (s >= 5) {
			pn5.setText(c.nomeNumeroTelefono.get(4) + " : " + c.numeroTelefono.get(4));
			Linkify.addLinks(pn5, Linkify.PHONE_NUMBERS);
		}
		if (s >= 6) {
			pn6.setText(c.nomeNumeroTelefono.get(5) + " : " + c.numeroTelefono.get(5));
			Linkify.addLinks(pn6, Linkify.PHONE_NUMBERS);
		}

		Button btnBack = (Button) view.findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return view;
	}

}
