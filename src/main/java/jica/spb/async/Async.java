package jica.spb.async;

import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import jica.spb.async.model.Option;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Async {

    private final Eventloop eventloop;

    public Async() {
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

    public <T> Option<T> supply(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        Promise<T> promise = createPromise(supplier);
        eventloop.run();
        return Option.fromPromise(promise);
    }

    public <T> void supply(List<Supplier<T>> suppliers) {
        if (nullOrEmpty(suppliers))
            return;
        suppliers.forEach(this::createPromise);
        eventloop.run();
    }

    public <T> void supply(Stream<Supplier<T>> stream) {
        if (stream == null)
            return;

        supply(stream.collect(Collectors.toList()));
    }

    private <T> Promise<T> createPromise(Supplier<T> supplier) {
        CompletableFuture<T> completableFuture = CompletableFuture.supplyAsync(supplier);
        return Promise.ofFuture(completableFuture);
    }

    private boolean nullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

}
