package com.porfirio.orariprocida2011.threads.transports;

import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.threads.DataUpdate;

import java.time.LocalDateTime;
import java.util.List;

public class TransportsUpdate extends DataUpdate<List<Mezzo>> {

    private final LocalDateTime updateTime;

    public TransportsUpdate(List<Mezzo> data) {
        this(data, LocalDateTime.now());
    }

    public TransportsUpdate(List<Mezzo> data, LocalDateTime updateTime) {
        super(data);
        this.updateTime = updateTime;
    }

    public TransportsUpdate(Exception error) {
        this(error, LocalDateTime.now());
    }

    public TransportsUpdate(Exception error, LocalDateTime updateTime) {
        super(error);
        this.updateTime = updateTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

}
