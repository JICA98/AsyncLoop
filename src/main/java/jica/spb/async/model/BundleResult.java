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


@Value
@RequiredArgsConstructor
public class BundleResult<T> {

    Collection<Result<T>> results;

    public static <R> BundleResult<R> fromPromises(Collection<Promise<R>> promises) {
        return new BundleResult<R>(promises.stream().map(Result::fromPromise).toList());
    }

    public static <R> BundleResult<R> empty() {
        return new BundleResult<>(Collections.emptyList());
    }

    public BundleResult<T> whenException(Consumer<Throwable> consumer) {
        results.forEach(result -> {
            if (result.hasException()) {
                consumer.accept(result.getException());
            }
        });
        return this;
    }

    public BundleResult<T> onFirstException(Consumer<Throwable> consumer) {
        results.stream().filter(Result::hasException)
                .findFirst()
                .map(Result::getException)
                .ifPresent(consumer);
        return this;
    }

    public boolean hasAnyValue() {
        return results.stream().anyMatch(Result::hasValue);
    }

    public boolean hasAnyException() {
        return results.stream().anyMatch(Result::hasException);
    }

    public Collection<Throwable> exceptions() {
        return results.stream().filter(Result::hasException).map(Result::getException).toList();
    }

    public Collection<Optional<T>> values() {
        return results.stream().filter(Result::hasValue).map(Result::value).toList();
    }

    public Collection<T> nonNullValues() {
        return results.stream()
                .filter(Result::hasValue)
                .map(Result::value)
                .filter(Optional::isPresent)
                .map(Optional::get).toList();
    }

    public static <R, E extends Collection<R>> List<R> flatMapBundle(BundleResult<E> result) {
        return result.nonNullValues()
                .stream()
                .flatMap(Collection::stream)
                .toList();
    }


    public <T1 extends Throwable> BundleResult<T> elseThrow(T1 throwable) throws T1 {
        if (hasAnyException()) {
            throw throwable;
        }
        return this;
    }

    public <T1 extends Throwable> BundleResult<T> elseThrow(Function<Collection<Throwable>, T1> function) throws T1 {
        if (hasAnyException()) {
            throw function.apply(exceptions());
        }
        return this;
    }

    public <T1 extends Throwable> BundleResult<T> elseThrowAny(Function<Throwable, T1> function) throws T1 {
        Optional<Throwable> any = exceptions().stream().findAny();
        if (any.isEmpty()) {
            return this;
        }
        throw function.apply(any.get());
    }

}
