package jica.spb.async.model;

import io.activej.promise.Promise;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents the result of an operation that may have a value or an exception.
 *
 * @param <T> The type of the result value.
 */
@Value
@RequiredArgsConstructor
public class Result<T> {

    ResultType type;

    T value;

    Throwable exception;

    /**
     * Creates a Result instance indicating an error with the specified throwable.
     *
     * @param throwable The throwable representing the error.
     * @param <R>       The type of the result value.
     * @return A Result instance representing an error.
     */
    public static <R> Result<R> withError(Throwable throwable) {
        return new Result<>(ResultType.EXCEPTION, null, throwable);
    }

    /**
     * Creates a Result instance containing a value.
     *
     * @param value The value to be wrapped.
     * @param <R>   The type of the result value.
     * @return A Result instance containing the value.
     */
    public static <R> Result<R> withValue(R value) {
        return new Result<>(ResultType.VALUE, value, null);
    }

    /**
     * Creates a Result instance from a Promise, considering its value or exception.
     *
     * @param promise The Promise to create a Result from.
     * @param <R>     The type of the promise result.
     * @return A Result instance based on the Promise's value or exception.
     */
    public static <R> Result<R> fromPromise(Promise<R> promise) {
        return promise.getException() == null ? withValue(promise.getResult()) : withError(promise.getException());
    }

    /**
     * Checks if the Result contains an exception.
     *
     * @return {@code true} if the Result contains an exception, otherwise {@code false}.
     */
    public boolean hasException() {
        return type == ResultType.EXCEPTION;
    }

    /**
     * Checks if the Result contains a value.
     *
     * @return {@code true} if the Result contains a value, otherwise {@code false}.
     */
    public boolean hasValue() {
        return type == ResultType.VALUE;
    }

    /**
     * Executes a consumer on the value if present and not null.
     *
     * @param consumer The consumer to be executed on the value.
     */
    public void whenValue(Consumer<T> consumer) {
        if (hasValue() && value != null) {
            consumer.accept(value);
        }
    }

    /**
     * Executes a consumer on the exception if present.
     *
     * @param consumer The consumer to be executed on the exception.
     */
    public void whenException(Consumer<Throwable> consumer) {
        if (hasException()) {
            consumer.accept(exception);
        }
    }

    /**
     * Returns an Optional containing the value, if present.
     *
     * @return An Optional containing the value, or an empty Optional if the value is null or absent.
     */
    public Optional<T> value() {
        return Optional.ofNullable(value);
    }

    /**
     * Throws the specified throwable if the Result contains an exception.
     *
     * @param throwable The throwable to be thrown.
     * @param <T1>      The type of the throwable.
     * @return The same Result.
     * @throws T1 If the Result contains an exception.
     */
    public <T1 extends Throwable> Result<T> elseThrow(T1 throwable) throws T1 {
        if (hasException()) {
            throw throwable;
        }
        return this;
    }

    /**
     * Throws a computed throwable if the Result contains an exception.
     *
     * @param function The function to compute the throwable.
     * @param <T1>     The type of the throwable.
     * @return The same Result.
     * @throws T1 If the Result contains an exception.
     */
    public <T1 extends Throwable> Result<T> elseThrow(Function<Throwable, T1> function) throws T1 {
        if (hasException()) {
            throw function.apply(exception);
        }
        return this;
    }

}
