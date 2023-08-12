package jica.spb.async;

import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.promise.SettablePromise;
import jica.spb.async.model.AsyncException;
import jica.spb.async.model.BundleResult;
import jica.spb.async.model.Result;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AsyncLoop {

    private final Eventloop eventloop;

    public AsyncLoop() {
        this.eventloop = Eventloop.create().withCurrentThread();
    }

    public void run(Runnable runnable) {
        Objects.requireNonNull(runnable);
        eventloop.post(runnable);
        eventloop.run();
    }

    public void run(List<Runnable> runnableList) {
        if (nullOrEmpty(runnableList))
            return;

        runnableList.forEach(eventloop::post);
        eventloop.run();
    }

    public void run(Stream<Runnable> stream) {
        if (stream == null)
            return;

        run(stream.collect(Collectors.toList()));
    }

    public <T> Result<T> submit(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        Promise<T> promise = createPromise(supplier);
        eventloop.run();
        return Result.fromPromise(promise);
    }

    public <T> BundleResult<T> submit(List<Supplier<T>> suppliers) {
        if (nullOrEmpty(suppliers))
            return BundleResult.empty();
        List<Promise<T>> promises = suppliers.stream().map(this::createPromise).toList();
        eventloop.run();
        return BundleResult.fromPromises(promises);
    }

    public <T> BundleResult<T> submit(Stream<Supplier<T>> stream) {
        if (stream == null)
            return null;

        return submit(stream.toList());
    }

    private <T> Promise<T> createPromise(Supplier<T> supplier) {
        CompletableFuture<T> completableFuture = CompletableFuture.supplyAsync(supplier);
        var promise = (SettablePromise<T>) Promise.ofFuture(completableFuture);
        completableFuture.exceptionally(handleException(promise));
        return promise;
    }

    private <T> Function<Throwable, T> handleException(SettablePromise<T> settablePromise) {
        return throwable -> {
            if (throwable instanceof CompletionException exception) {
                if (exception.getCause() instanceof Exception cause) {
                    settablePromise.setException(cause);
                } else {
                    settablePromise.setException(exception);
                }
            } else {
                settablePromise.setException(new AsyncException(throwable));
            }
            return null;
        };
    }

    private boolean nullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

}
