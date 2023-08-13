package jica.spb.async;

import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.promise.SettablePromise;
import jica.spb.async.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides utility methods for working with ActiveJ promises using an event loop.
 */
@Slf4j
public class AsyncLoop {

    private <T> T withEventLoop(Function<Eventloop, T> callBack) {
        try {
            Eventloop eventloop = Eventloop.create().withCurrentThread();
            return callBack.apply(eventloop);
        } catch (Exception e) {
            log.error("Error occurred while setting up eventLoop: {}", String.valueOf(e));
            throw new AsyncException(e);
        }
    }

    /**
     * Executes a runnable task on an event loop.
     *
     * @param runnable The task to be executed.
     */
    public void run(Runnable runnable) {
        Objects.requireNonNull(runnable);
        withEventLoop(eventloop -> {
            eventloop.post(runnable);
            eventloop.run();
            return null;
        });
    }

    /**
     * Executes a collection of runnable tasks on an event loop.
     *
     * @param runnables The collection of tasks to be executed.
     */
    public void run(Collection<Runnable> runnables) {
        if (nullOrEmpty(runnables))
            return;

        withEventLoop(eventloop -> {
            runnables.forEach(eventloop::post);
            eventloop.run();
            return null;
        });
    }

    /**
     * Executes a stream of runnable tasks on an event loop.
     *
     * @param stream The stream of tasks to be executed.
     */
    public void run(Stream<Runnable> stream) {
        if (stream == null)
            return;

        run(stream.collect(Collectors.toList()));
    }

    /**
     * Retrieves the result of a supplier executed on an event loop.
     *
     * @param supplier The supplier whose result is to be retrieved.
     * @param <T>      The type of the result.
     * @return The result of the supplier wrapped in a Result.
     */
    public <T> Result<T> get(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return withEventLoop(eventloop -> {
            Promise<T> promise = createPromise(supplier);
            eventloop.run();
            return Result.fromPromise(promise);
        });
    }

    /**
     * Retrieves results from a collection of suppliers executed on an event loop.
     *
     * @param suppliers The collection of suppliers whose results are to be retrieved.
     * @param <T>       The type of the result.
     * @return The results of the suppliers wrapped in a BundleResult.
     */
    public <T> BundleResult<T> get(Collection<Supplier<T>> suppliers) {
        if (nullOrEmpty(suppliers))
            return BundleResult.empty();

        return withEventLoop(eventloop -> {
            List<Promise<T>> promises = suppliers.stream().map(this::createPromise).toList();
            eventloop.run();
            return BundleResult.fromPromises(promises);
        });
    }

    /**
     * Retrieves results from a stream of suppliers executed on an event loop.
     *
     * @param stream The stream of suppliers whose results are to be retrieved.
     * @param <T>    The type of the result.
     * @return The results of the suppliers wrapped in a BundleResult.
     */
    public <T> BundleResult<T> get(Stream<Supplier<T>> stream) {
        if (stream == null)
            return null;

        return get(stream.toList());
    }

    public <T> Result<Void> accept(ConsumerWrapper<T> wrapper) {
        return withEventLoop(eventloop -> {
            Promise<Void> promise = createConsumerPromise(wrapper);
            eventloop.run();
            return Result.fromPromise(promise);
        });
    }

    /**
     * Executes a collection of consumer tasks on an event loop and returns a BundleResult.
     *
     * @param wrappers The list of consumer wrappers containing the consumer tasks.
     * @param <T>      The type of input for the consumer.
     * @return A BundleResult containing promises of the executed consumer tasks.
     */
    public <T> BundleResult<Void> accept(List<ConsumerWrapper<T>> wrappers) {
        return withEventLoop(eventloop -> {
            List<Promise<Void>> promises = wrappers.stream().map(this::createConsumerPromise).toList();
            eventloop.run();
            return BundleResult.fromPromises(promises);
        });
    }

    /**
     * Executes a stream of consumer tasks on an event loop and returns a BundleResult.
     *
     * @param stream The stream of consumer wrappers containing the consumer tasks.
     * @param <T>    The type of input for the consumer.
     * @return A BundleResult containing promises of the executed consumer tasks.
     */
    public <T> BundleResult<Void> accept(Stream<ConsumerWrapper<T>> stream) {
        return accept(stream.toList());
    }

    /**
     * Creates a promise for executing a consumer task on an event loop.
     *
     * @param wrapper The consumer wrapper containing the consumer task.
     * @param <T>     The type of input for the consumer.
     * @return A Promise representing the execution of the consumer task.
     */
    private <T> Promise<Void> createConsumerPromise(ConsumerWrapper<T> wrapper) {
        Objects.requireNonNull(wrapper);
        Objects.requireNonNull(wrapper.getConsumer());
        return createPromise(wrapper);
    }

    /**
     * Executes a function task on an event loop and returns a Result.
     *
     * @param wrapper The function wrapper containing the function task.
     * @param <I>     The input type for the function.
     * @param <O>     The output type of the function.
     * @return A Result containing the result of the executed function task.
     */
    public <I, O> Result<O> apply(FunctionWrapper<I, O> wrapper) {
        return withEventLoop(eventloop -> {
            Promise<O> promise = createFunctionPromise(wrapper);
            eventloop.run();
            return Result.fromPromise(promise);
        });
    }

    /**
     * Executes a collection of function tasks on an event loop and returns a BundleResult.
     *
     * @param wrappers The collection of function wrappers containing the function tasks.
     * @param <I>      The input type for the function.
     * @param <O>      The output type of the function.
     * @return A BundleResult containing promises of the executed function tasks.
     */
    public <I, O> BundleResult<O> apply(Collection<FunctionWrapper<I, O>> wrappers) {
        return withEventLoop(eventloop -> {
            List<Promise<O>> promises = wrappers.stream().map(this::createFunctionPromise).toList();
            eventloop.run();
            return BundleResult.fromPromises(promises);
        });
    }

    /**
     * Executes a stream of function tasks on an event loop and returns a BundleResult.
     *
     * @param stream The stream of function wrappers containing the function tasks.
     * @param <I>    The input type for the function.
     * @param <O>    The output type of the function.
     * @return A BundleResult containing promises of the executed function tasks.
     */
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
