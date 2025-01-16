package com.porfirio.orariprocida2011.threads.companies;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.porfirio.orariprocida2011.entity.Compagnia;

import java.util.ArrayList;

public class OnRequestCompaniesDAO implements CompaniesDAO {

    private final MutableLiveData<CompaniesUpdate> update;

    public OnRequestCompaniesDAO() {
        this.update = new MutableLiveData<>();
    }

    public void requestUpdate() {
        ArrayList<Compagnia> companies = new ArrayList<>();

        Compagnia c = new Compagnia("Caremar");
        c.addTelefono("Napoli (Molo Beverello)", "0815513882");
        c.addTelefono("Pozzuoli", "0815262711");
        c.addTelefono("Pozzuoli", "0815261335");
        c.addTelefono("Ischia", "081984818");
        c.addTelefono("Ischia", "081991953");
        c.addTelefono("Procida", "0818967280");
        companies.add(c);

        c = new Compagnia("Gestur");
        c.addTelefono("Sede", "0818531405");
        c.addTelefono("Procida", "0818531405");
        c.addTelefono("Pozzuoli", "0815268165");
        companies.add(c);

        c = new Compagnia("SNAV");
        c.addTelefono("Call Center", "0814285111");
        c.addTelefono("Napoli", "0814285111");
        c.addTelefono("Ischia", "081984818");
        c.addTelefono("Procida", "0818969975");
        companies.add(c);

        c = new Compagnia("Medmar");
        c.addTelefono("Napoli", "0813334411");
        c.addTelefono("Procida", "0818969594");
        c.addTelefono("Procida", "0818969190");
        companies.add(c);

        c = new Compagnia("Ippocampo");
        c.addTelefono("Procida", "3663575751");
        c.addTelefono("Procida", "0818967764");
        c.addTelefono("Monte di Procida", "3397585125");
        companies.add(c);

        c = new Compagnia("Scotto Line");
        c.addTelefono("Procida", "3343525753");
        c.addTelefono("Procida", "0818968753");
        c.addTelefono("Procida", "3394775523");
        companies.add(c);

        c = new Compagnia("LazioMar");
        c.addTelefono("Napoli", "0771700604");
        companies.add(c);

        c = new Compagnia("Alilauro");
        c.addTelefono("Napoli", "0814972252");
        c.addTelefono("Call Center", "0814972222");
        companies.add(c);

        update.postValue(new CompaniesUpdate(companies));
    }

    @Override
    public LiveData<CompaniesUpdate> getUpdate() {
        return update;
    }

}
