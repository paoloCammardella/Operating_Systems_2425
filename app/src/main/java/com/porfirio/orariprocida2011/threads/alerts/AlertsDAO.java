package com.porfirio.orariprocida2011.threads.alerts;

import androidx.lifecycle.LiveData;

/**
 * Interface to receive and send alerts.
 */
public interface AlertsDAO {

    /**
     * Returns the LiveData containing the latest alerts update.
     * The implementation should send it in the main thread.
     *
     * @return LiveData with latest alerts update
     */
    LiveData<AlertUpdate> getUpdates();

    /**
     * Sends an alert.
     *
     * @param alert alert to send
     */
    void send(Alert alert);

}
