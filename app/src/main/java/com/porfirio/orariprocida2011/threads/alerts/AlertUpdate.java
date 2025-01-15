package com.porfirio.orariprocida2011.threads.alerts;

import java.util.List;

public class AlertUpdate {

    private final boolean isValid;
    private final List<Alert> data;
    private final Exception error;

    public AlertUpdate(List<Alert> data) {
        this.isValid = true;
        this.data = data;
        this.error = null;
    }

    public AlertUpdate(Exception error) {
        this.isValid = false;
        this.data = null;
        this.error = error;
    }

    public boolean isValid() {
        return isValid;
    }

    public List<Alert> getData() {
        return data;
    }

    public Exception getError() {
        return error;
    }

}
