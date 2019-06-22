package com.porfirio.orariprocida2011.dialogs;


import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.entity.Compagnia;

public class BiglietterieDialog extends DialogFragment {
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
		TextView pn1 = view.findViewById(R.id.pn1);
		TextView pn2 = view.findViewById(R.id.pn2);
		TextView pn3 = view.findViewById(R.id.pn3);
		TextView pn4 = view.findViewById(R.id.pn4);
		TextView pn5 = view.findViewById(R.id.pn5);
		TextView pn6 = view.findViewById(R.id.pn6);

		if (c == null) {
			pn1.setText(getString(R.string.NoBiglietterie));
		} else {
			int s = c.nomeNumeroTelefono.size();
			if (s >= 1) {
				String text = c.nomeNumeroTelefono.get(0) + " : " + c.numeroTelefono.get(0);
				pn1.setText(text);
				Linkify.addLinks(pn1, Linkify.PHONE_NUMBERS);
			}
			if (s >= 2) {
				final String text = c.nomeNumeroTelefono.get(1) + " : " + c.numeroTelefono.get(1);
				pn2.setText(text);
				Linkify.addLinks(pn2, Linkify.PHONE_NUMBERS);
			}
			if (s >= 3) {
				final String text = c.nomeNumeroTelefono.get(2) + " : " + c.numeroTelefono.get(2);
				pn3.setText(text);
				Linkify.addLinks(pn3, Linkify.PHONE_NUMBERS);
			}
			if (s >= 4) {
				final String text = c.nomeNumeroTelefono.get(3) + " : " + c.numeroTelefono.get(3);
				pn4.setText(text);
				Linkify.addLinks(pn4, Linkify.PHONE_NUMBERS);
			}
			if (s >= 5) {
				final String text = c.nomeNumeroTelefono.get(4) + " : " + c.numeroTelefono.get(4);
				pn5.setText(text);
				Linkify.addLinks(pn5, Linkify.PHONE_NUMBERS);
			}
			if (s >= 6) {
				final String text = c.nomeNumeroTelefono.get(5) + " : " + c.numeroTelefono.get(5);
				pn6.setText(text);
				Linkify.addLinks(pn6, Linkify.PHONE_NUMBERS);
			}
		}
		Button btnBack = view.findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return view;
	}

}
