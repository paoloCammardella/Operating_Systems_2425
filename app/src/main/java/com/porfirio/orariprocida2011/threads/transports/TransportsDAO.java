package com.porfirio.orariprocida2011.threads.transports;

import androidx.lifecycle.LiveData;

/**
 * Interface to receive transports data.
 */
public interface TransportsDAO {

    /**
     * Returns the LiveData containing the latest transports update.
     * The implementation should send it in the main thread.
     *
     * @return LiveData with latest transports update
     */
    LiveData<TransportsUpdate> getUpdates();

}
