package com.porfirio.orariprocida2011.threads.companies;

import androidx.lifecycle.LiveData;

public interface CompaniesDAO {

    LiveData<CompaniesUpdate> getUpdate();

}
