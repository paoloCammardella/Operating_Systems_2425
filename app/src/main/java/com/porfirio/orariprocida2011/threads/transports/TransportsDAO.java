package com.porfirio.orariprocida2011.threads.transports;

import androidx.lifecycle.LiveData;

public interface TransportsDAO {

    LiveData<TransportsUpdate> getUpdates();

}
