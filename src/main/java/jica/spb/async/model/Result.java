package jica.spb.async.model;

import io.activej.promise.Promise;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Optional;
import java.util.function.Consumer;

@Value
@RequiredArgsConstructor
public class Result<T> {

    ResultType type;

    T value;

    Throwable exception;

    public static <R> Result<R> withError(Throwable throwable) {
        return new Result<R>(ResultType.EXCEPTION, null, throwable);
    }

    public static <R> Result<R> withValue(R value) {
        return new Result<R>(ResultType.VALUE, value, null);
    }

    public static <R> Result<R> fromPromise(Promise<R> promise) {
        return promise.getException() == null ? withValue(promise.getResult()) : withError(promise.getException());
    }

    public boolean hasException() {
        return type == ResultType.EXCEPTION;
    }

    public boolean hasValue() {
        return type == ResultType.VALUE;
    }

    public void whenValue(Consumer<T> consumer) {
        if (hasValue() && value != null) {
            consumer.accept(value);
        }
    }

    public void whenException(Consumer<Throwable> consumer) {
        if (hasException()) {
            consumer.accept(exception);
        }
    }

    public Optional<T> value() {
        return Optional.ofNullable(value);
    }

}
