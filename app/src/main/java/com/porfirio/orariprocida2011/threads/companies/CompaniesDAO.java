package com.porfirio.orariprocida2011.threads.companies;

import androidx.lifecycle.LiveData;

/**
 * Interface to receive companies data.
 */
public interface CompaniesDAO {

    /**
     * Returns the LiveData containing the latest companies update.
     * The implementation should send it in the main thread.
     *
     * @return LiveData with latest companies update
     */
    LiveData<CompaniesUpdate> getUpdate();

}
