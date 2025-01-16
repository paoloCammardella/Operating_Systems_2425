package com.porfirio.orariprocida2011.threads.companies;

import com.porfirio.orariprocida2011.entity.Compagnia;

import java.util.List;

public class CompaniesUpdate {

    private final boolean isValid;
    private final List<Compagnia> data;
    private final Exception error;

    public CompaniesUpdate(List<Compagnia> data) {
        this.isValid = true;
        this.data = data;
        this.error = null;
    }

    public CompaniesUpdate(Exception error) {
        this.isValid = false;
        this.data = null;
        this.error = error;
    }

    public boolean isValid() {
        return isValid;
    }

    public List<Compagnia> getData() {
        return data;
    }

    public Exception getError() {
        return error;
    }

}
