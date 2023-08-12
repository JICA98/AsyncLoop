package jica.spb.async.model;

import io.activej.promise.Promise;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class Option<T> {

    OptionType type;
    T value;
    Throwable throwable;

    public static <R> Option<R> withError(Throwable throwable) {
        return new Option<R>(OptionType.ERROR, null, throwable);
    }

    public static <R> Option<R> withValue(R value) {
        return new Option<R>(OptionType.RESULT, value, null);
    }

    public static <R> Option<R> fromPromise(Promise<R> promise) {
        return promise.getException() == null ? withValue(promise.getResult()) : withError(promise.getException());
    }

    public boolean hasException() {
        return type == OptionType.ERROR;
    }

}
