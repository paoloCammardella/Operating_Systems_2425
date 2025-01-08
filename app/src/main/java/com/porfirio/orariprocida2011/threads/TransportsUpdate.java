package com.porfirio.orariprocida2011.threads;

import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.entity.Osservazione;

import java.time.LocalDateTime;
import java.util.List;

public class TransportsUpdate {

    private final boolean isValid;
    private final List<Mezzo> data;
    private final Exception error;
    private final LocalDateTime updateTime;

    public TransportsUpdate(List<Mezzo> data) {
        this(data, LocalDateTime.now());
    }

    public TransportsUpdate(List<Mezzo> data, LocalDateTime updateTime) {
        this.isValid = true;
        this.data = data;
        this.error = null;
        this.updateTime = updateTime;
    }

    public TransportsUpdate(Exception error) {
        this(error, LocalDateTime.now());
    }

    public TransportsUpdate(Exception error, LocalDateTime updateTime) {
        this.isValid = false;
        this.data = null;
        this.error = error;
        this.updateTime = updateTime;
    }

    public boolean isValid() {
        return isValid;
    }

    public List<Mezzo> getData() {
        return data;
    }

    public Exception getError() {
        return error;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

}
