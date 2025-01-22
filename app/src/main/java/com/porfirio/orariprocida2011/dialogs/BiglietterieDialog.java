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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.biglietterie, container);

        TextView[] phoneNumbersLabels = {
                view.findViewById(R.id.pn1),
                view.findViewById(R.id.pn2),
                view.findViewById(R.id.pn3),
                view.findViewById(R.id.pn4),
                view.findViewById(R.id.pn5),
                view.findViewById(R.id.pn6)
        };

        if (c == null) {
            phoneNumbersLabels[0].setText(getString(R.string.NoBiglietterie));
        } else {
            int s = Math.min(c.getContactsCount(), phoneNumbersLabels.length);

            for (int i = 0; i < s; i++) {
                phoneNumbersLabels[i].setText(c.getContactName(i) + ": " + c.getContactNumber(i));
                Linkify.addLinks(phoneNumbersLabels[i], Linkify.PHONE_NUMBERS);
            }
        }

        Button btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> dismiss());

        return view;
    }

}
