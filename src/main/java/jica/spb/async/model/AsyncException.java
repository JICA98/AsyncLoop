package jica.spb.async.model;

public class AsyncException extends RuntimeException {

    public AsyncException(Throwable throwable) {
        super(throwable);
    }

}
