package jica.spb.async.model;

import io.activej.promise.Promise;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a bundle of results from executing promises and provides various utility methods for handling the results.
 *
 * @param <T> The type of the result values.
 */
@Value
@RequiredArgsConstructor
public class BundleResult<T> {

    Collection<Result<T>> results;

    /**
     * Creates a new BundleResult from a collection of promises.
     *
     * @param promises The collection of promises whose results are bundled.
     * @param <R>      The type of the promise result.
     * @return A BundleResult containing results from the promises.
     */
    public static <R> BundleResult<R> fromPromises(Collection<Promise<R>> promises) {
        return new BundleResult<>(promises.stream().map(Result::fromPromise).toList());
    }

    /**
     * Creates an empty BundleResult.
     *
     * @param <R> The type of the result values.
     * @return An empty BundleResult.
     */
    public static <R> BundleResult<R> empty() {
        return new BundleResult<>(Collections.emptyList());
    }

    /**
     * Performs the given action on each exception in the results.
     *
     * @param consumer The action to be performed on each exception.
     * @return This BundleResult.
     */
    public BundleResult<T> whenException(Consumer<Throwable> consumer) {
        results.forEach(result -> {
            if (result.hasException()) {
                consumer.accept(result.getException());
            }
        });
        return this;
    }

    /**
     * Performs the given action on the first exception in the results, if any.
     *
     * @param consumer The action to be performed on the first exception.
     * @return This BundleResult.
     */
    public BundleResult<T> onFirstException(Consumer<Throwable> consumer) {
        results.stream().filter(Result::hasException)
                .findFirst()
                .map(Result::getException)
                .ifPresent(consumer);
        return this;
    }

    /**
     * Checks if the BundleResult contains any non-null result values.
     *
     * @return {@code true} if any result value is present, otherwise {@code false}.
     */
    public boolean hasAnyValue() {
        return results.stream().anyMatch(Result::hasValue);
    }

    /**
     * Checks if the BundleResult contains any exceptions.
     *
     * @return {@code true} if any exceptions are present, otherwise {@code false}.
     */
    public boolean hasAnyException() {
        return results.stream().anyMatch(Result::hasException);
    }

    /**
     * Returns a collection of exceptions from the results.
     *
     * @return A collection of exceptions.
     */
    public Collection<Throwable> exceptions() {
        return results.stream().filter(Result::hasException).map(Result::getException).toList();
    }

    /**
     * Returns a collection of optional result values from the results.
     *
     * @return A collection of optional result values.
     */
    public Collection<Optional<T>> values() {
        return results.stream().filter(Result::hasValue).map(Result::value).toList();
    }

    /**
     * Returns a collection of non-null result values from the results.
     *
     * @return A collection of non-null result values.
     */
    public Collection<T> nonNullValues() {
        return results.stream()
                .filter(Result::hasValue)
                .map(Result::value)
                .filter(Optional::isPresent)
                .map(Optional::get).toList();
    }

    /**
     * Flattens a BundleResult of collections into a list of values.
     *
     * @param result The BundleResult containing collections of values.
     * @param <R>    The type of the values.
     * @return A list of flattened values.
     */
    public static <R, E extends Collection<R>> List<R> flatMapBundle(BundleResult<E> result) {
        return result.nonNullValues()
                .stream()
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * Throws the specified throwable if the BundleResult contains any exceptions.
     *
     * @param throwable The throwable to be thrown.
     * @param <T1>      The type of the throwable.
     * @return The same BundleResult.
     * @throws T1 If the BundleResult contains any exceptions.
     */
    public <T1 extends Throwable> BundleResult<T> elseThrow(T1 throwable) throws T1 {
        if (hasAnyException()) {
            throw throwable;
        }
        return this;
    }

    /**
     * Throws a computed throwable if the BundleResult contains any exceptions.
     *
     * @param function The function to compute the throwable.
     * @param <T1>     The type of the throwable.
     * @return The same BundleResult.
     * @throws T1 If the BundleResult contains any exceptions.
     */
    public <T1 extends Throwable> BundleResult<T> elseThrow(Function<Collection<Throwable>, T1> function) throws T1 {
        if (hasAnyException()) {
            throw function.apply(exceptions());
        }
        return this;
    }

    /**
     * Throws any throwable computed by the function if the BundleResult contains any exceptions.
     *
     * @param function The function to compute the throwable.
     * @param <T1>     The type of the throwable.
     * @return The same BundleResult.
     * @throws T1 If the BundleResult contains any exceptions.
     */
    public <T1 extends Throwable> BundleResult<T> elseThrowAny(Function<Throwable, T1> function) throws T1 {
        Optional<Throwable> any = exceptions().stream().findAny();
        if (any.isEmpty()) {
            return this;
        }
        throw function.apply(any.get());
    }

}
