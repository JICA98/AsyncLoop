package jica.spb.async.model;

/**
 * Custom exception class representing an asynchronous exception that occurred during the execution of a promise.
 */
public class AsyncException extends RuntimeException {

    /**
     * Constructs a new AsyncException with the specified cause.
     *
     * @param throwable The cause of the exception.
     */
    public AsyncException(Throwable throwable) {
        super(throwable);
    }
}
