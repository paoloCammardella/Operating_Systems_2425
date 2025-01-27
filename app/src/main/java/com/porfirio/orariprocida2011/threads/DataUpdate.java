package com.porfirio.orariprocida2011.threads;

/**
 * An object representing the result of an update. It either contains the new data or the error generated during the update.
 *
 * @param <T> type of the data
 */
public class DataUpdate<T> {

    private final boolean isValid;
    private final T data;
    private final Exception error;

    /**
     * Creates a new successful update with the given data.
     *
     * @param data result of the update
     */
    public DataUpdate(T data) {
        this.isValid = true;
        this.data = data;
        this.error = null;
    }

    /**
     * Creates a new failed update with the given error.
     *
     * @param error error occurred during the update
     */
    public DataUpdate(Exception error) {
        this.isValid = false;
        this.data = null;
        this.error = error;
    }

    /**
     * Checks if the update contains data or an error.
     *
     * @return {@code true} if the update is successful and it contains data, {@code false} otherwise
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Returns the new data of the update. It should not be used if {@link #isValid()} returns {@code false}.
     *
     * @return data of the update
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the error occurred during the update. It should not be used if {@link #isValid()} returns {@code true}.
     *
     * @return error of the update
     */
    public Exception getError() {
        return error;
    }

}
