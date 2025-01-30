package com.porfirio.orariprocida2011.threads.taxies;

import androidx.lifecycle.LiveData;

/**
 * Interface to receive taxis data.
 */
public interface TaxisDAO {

    /**
     * Returns the LiveData containing the latest taxis update.
     * The implementation should send it in the main thread.
     *
     * @return LiveData with latest taxis update
     */
    LiveData<TaxisUpdate> getUpdate();

}
