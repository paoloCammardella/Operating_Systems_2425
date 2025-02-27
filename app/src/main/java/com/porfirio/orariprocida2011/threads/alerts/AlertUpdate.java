package com.porfirio.orariprocida2011.threads.alerts;

import com.porfirio.orariprocida2011.entity.Alert;
import com.porfirio.orariprocida2011.threads.DataUpdate;

import java.util.List;

/**
 * The result of an alerts update. It contains the new received alerts.
 */
public class AlertUpdate extends DataUpdate<List<Alert>> {

    /**
     * Constructs a new successful update.
     *
     * @param data new alerts received from the update
     */
    public AlertUpdate(List<Alert> data) {
        super(data);
    }

    /**
     * Constructs a new failed update.
     *
     * @param error error occurred during the update
     */
    public AlertUpdate(Exception error) {
        super(error);
    }

}
