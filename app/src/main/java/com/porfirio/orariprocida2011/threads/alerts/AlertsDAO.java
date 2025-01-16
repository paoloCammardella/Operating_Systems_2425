package com.porfirio.orariprocida2011.threads.alerts;

import androidx.lifecycle.LiveData;

public interface AlertsDAO {

    LiveData<AlertUpdate> getUpdates();

    void send(Alert alert);

}
