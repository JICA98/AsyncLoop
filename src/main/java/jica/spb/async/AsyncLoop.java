package jica.spb.async;

import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.promise.SettablePromise;
import jica.spb.async.model.*;

import java.util.Collection;
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

    public void run(Collection<Runnable> runnables) {
        if (nullOrEmpty(runnables))
            return;

        runnables.forEach(eventloop::post);
        eventloop.run();
    }

    public void run(Stream<Runnable> stream) {
        if (stream == null)
            return;

        run(stream.collect(Collectors.toList()));
    }

    public <T> Result<T> get(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        Promise<T> promise = createPromise(supplier);
        eventloop.run();
        return Result.fromPromise(promise);
    }

    public <T> BundleResult<T> get(Collection<Supplier<T>> suppliers) {
        if (nullOrEmpty(suppliers))
            return BundleResult.empty();
        List<Promise<T>> promises = suppliers.stream().map(this::createPromise).toList();
        eventloop.run();
        return BundleResult.fromPromises(promises);
    }

    public <T> BundleResult<T> get(Stream<Supplier<T>> stream) {
        if (stream == null)
            return null;

        return get(stream.toList());
    }

    public <T> Result<Void> accept(ConsumerWrapper<T> wrapper) {
        Promise<Void> promise = createConsumerPromise(wrapper);
        eventloop.run();
        return Result.fromPromise(promise);
    }

    public <T> BundleResult<Void> accept(List<ConsumerWrapper<T>> wrappers) {
        List<Promise<Void>> promises = wrappers.stream().map(this::createConsumerPromise).toList();
        eventloop.run();
        return BundleResult.fromPromises(promises);
    }

    public <T> BundleResult<Void> accept(Stream<ConsumerWrapper<T>> stream) {
        return accept(stream.toList());
    }

    private <T> Promise<Void> createConsumerPromise(ConsumerWrapper<T> wrapper) {
        Objects.requireNonNull(wrapper);
        Objects.requireNonNull(wrapper.getConsumer());
        return createPromise(wrapper);
    }

    public <I, O> Result<O> apply(FunctionWrapper<I, O> wrapper) {
        Promise<O> promise = createFunctionPromise(wrapper);
        eventloop.run();
        return Result.fromPromise(promise);
    }

    public <I, O> BundleResult<O> apply(Collection<FunctionWrapper<I, O>> wrappers) {
        List<Promise<O>> promises = wrappers.stream().map(this::createFunctionPromise).toList();
        eventloop.run();
        return BundleResult.fromPromises(promises);
    }

    public <I, O> BundleResult<O> apply(Stream<FunctionWrapper<I, O>> stream) {
        return apply(stream.toList());
    }

    private <I, O> Promise<O> createFunctionPromise(FunctionWrapper<I, O> wrapper) {
        Objects.requireNonNull(wrapper);
        Objects.requireNonNull(wrapper.getFunction());
        return createPromise(wrapper);
    }

    private <I, O> Promise<O> createPromise(FunctionWrapper<I, O> wrapper) {
        return exceptionalPromise(CompletableFuture.supplyAsync(functionWrapper(wrapper)));
    }


    private <T> Promise<Void> createPromise(ConsumerWrapper<T> wrapper) {
        return exceptionalPromise(CompletableFuture.supplyAsync(consumerWrapper(wrapper)));
    }

    private <O> Promise<O> createPromise(Supplier<O> supplier) {
        return exceptionalPromise(CompletableFuture.supplyAsync(supplier));
    }

    private <T> SettablePromise<T> exceptionalPromise(CompletableFuture<T> completableFuture) {
        var promise = (SettablePromise<T>) Promise.ofFuture(completableFuture);
        completableFuture.exceptionally(handleException(promise));
        return promise;
    }

    private <I, O> Supplier<O> functionWrapper(FunctionWrapper<I, O> wrapper) {
        return () -> wrapper.getFunction().apply(wrapper.getInput());
    }

    private <I> Supplier<Void> consumerWrapper(ConsumerWrapper<I> wrapper) {
        return () -> {
            wrapper.getConsumer().accept(wrapper.getInput());
            return null;
        };
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
