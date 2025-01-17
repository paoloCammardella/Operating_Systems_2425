package com.porfirio.orariprocida2011.threads;

public class DataUpdate<T> {

    private final boolean isValid;
    private final T data;
    private final Exception error;

    public DataUpdate(T data) {
        this.isValid = true;
        this.data = data;
        this.error = null;
    }

    public DataUpdate(Exception error) {
        this.isValid = false;
        this.data = null;
        this.error = error;
    }

    public boolean isValid() {
        return isValid;
    }

    public T getData() {
        return data;
    }

    public Exception getError() {
        return error;
    }

}
