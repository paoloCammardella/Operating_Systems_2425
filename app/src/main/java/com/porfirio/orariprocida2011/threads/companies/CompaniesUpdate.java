package com.porfirio.orariprocida2011.threads.companies;

import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.threads.DataUpdate;

import java.util.List;

public class CompaniesUpdate extends DataUpdate<List<Compagnia>> {

    public CompaniesUpdate(List<Compagnia> data) {
        super(data);
    }

    public CompaniesUpdate(Exception error) {
        super(error);
    }

}
