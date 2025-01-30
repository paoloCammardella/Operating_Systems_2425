package com.porfirio.orariprocida2011.threads.taxies;

import com.porfirio.orariprocida2011.entity.Taxi;
import com.porfirio.orariprocida2011.threads.DataUpdate;

import java.util.List;

public class TaxisUpdate extends DataUpdate<List<Taxi>> {

    public TaxisUpdate(List<Taxi> data) {
        super(data);
    }

    public TaxisUpdate(Exception error) {
        super(error);
    }

}
