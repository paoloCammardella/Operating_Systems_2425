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
import com.porfirio.orariprocida2011.entity.Taxi;

import java.util.ArrayList;
import java.util.List;

public class TaxiDialog extends DialogFragment {

    private String porto;
    private List<Taxi> taxis;

    public void setPorto(String porto) {
        this.porto = porto;
    }

    public void setTaxis(List<Taxi> taxis) {
        this.taxis = taxis;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.taxi, container);

        TextView tn1 = view.findViewById(R.id.tn1);
        tn1.setText(null);
        TextView tn2 = view.findViewById(R.id.tn2);
        tn2.setText(null);
        TextView tn3 = view.findViewById(R.id.tn3);
        tn3.setText(null);
        TextView tn4 = view.findViewById(R.id.tn4);
        tn4.setText(null);
        TextView tn5 = view.findViewById(R.id.tn5);
        tn5.setText(null);
        TextView tn6 = view.findViewById(R.id.tn6);
        tn6.setText(null);

        Button btnBack = view.findViewById(R.id.btnBackTaxi);
        btnBack.setOnClickListener(v -> dismiss());

        ArrayList<Taxi> taxiPortoList = new ArrayList<>();
        for (int i = 0; i < taxis.size(); i++)
            if (porto.contains(taxis.get(i).getPorto()) && !(porto.contentEquals("Monte di Procida") && taxis.get(i).getPorto().contentEquals("Procida")))
                taxiPortoList.add(taxis.get(i));
        if (taxiPortoList.size() >= 1) {
            final String text = taxiPortoList.get(0).getCompagnia() + " : " + taxiPortoList.get(0).getNumero();
            tn1.setText(text);
            Linkify.addLinks(tn1, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 2) {
            final String text = taxiPortoList.get(1).getCompagnia() + " : " + taxiPortoList.get(1).getNumero();
            tn2.setText(text);
            Linkify.addLinks(tn2, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 3) {
            final String text = taxiPortoList.get(2).getCompagnia() + " : " + taxiPortoList.get(2).getNumero();
            tn3.setText(text);
            Linkify.addLinks(tn3, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 4) {
            final String text = taxiPortoList.get(3).getCompagnia() + " : " + taxiPortoList.get(3).getNumero();
            tn4.setText(text);
            Linkify.addLinks(tn4, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 5) {
            final String text = taxiPortoList.get(4).getCompagnia() + " : " + taxiPortoList.get(4).getNumero();
            tn5.setText(text);
            Linkify.addLinks(tn5, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 6) {
            final String text = taxiPortoList.get(5).getCompagnia() + " : " + taxiPortoList.get(5).getNumero();
            tn6.setText(text);
            Linkify.addLinks(tn6, Linkify.PHONE_NUMBERS);
        }
        return view;
    }


}
