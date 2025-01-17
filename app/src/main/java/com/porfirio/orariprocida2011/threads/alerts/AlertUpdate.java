package com.porfirio.orariprocida2011.threads.alerts;

import com.porfirio.orariprocida2011.threads.DataUpdate;

import java.util.List;

public class AlertUpdate extends DataUpdate<List<Alert>> {

    public AlertUpdate(List<Alert> data) {
        super(data);
    }

    public AlertUpdate(Exception error) {
        super(error);
    }
    
}
