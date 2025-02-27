package com.porfirio.orariprocida2011.threads.companies;

import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.threads.DataUpdate;

import java.util.List;

/**
 * The result of a companies update. It contains the new received companies.
 */
public class CompaniesUpdate extends DataUpdate<List<Compagnia>> {

    /**
     * Constructs a new successful update.
     *
     * @param data new companies received from the update
     */
    public CompaniesUpdate(List<Compagnia> data) {
        super(data);
    }

    /**
     * Constructs a new failed update.
     *
     * @param error error occurred during the update
     */
    public CompaniesUpdate(Exception error) {
        super(error);
    }

}
