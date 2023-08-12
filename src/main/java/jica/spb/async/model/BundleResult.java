package jica.spb.async.model;

import io.activej.promise.Promise;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;


@Value
@RequiredArgsConstructor
public class BundleResult<T> {

    Collection<Result<T>> results;

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

    public static <R> BundleResult<R> fromPromises(Collection<Promise<R>> promises) {
        return new BundleResult<R>(promises.stream().map(Result::fromPromise).toList());
    }

    public static <R> BundleResult<R> empty() {
        return new BundleResult<>(Collections.emptyList());
    }


}
