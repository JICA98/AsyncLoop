package jica.spb.async.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A wrapper class that combines a consumer and its input value.
 *
 * @param <I> The type of the input value for the consumer.
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsumerWrapper<I> {

    Consumer<I> consumer;

    I input;

    /**
     * Creates a new ConsumerWrapper instance with the given consumer and input value.
     *
     * @param <R>      The type of the input value.
     * @param consumer The consumer to wrap.
     * @param input    The input value for the consumer.
     * @return A new ConsumerWrapper instance.
     */
    public static <R> ConsumerWrapper<R> of(Consumer<R> consumer, R input) {
        return new ConsumerWrapper<>(consumer, input);
    }

    /**
     * Creates a function that produces ConsumerWrapper instances with the provided consumer and accepts an input value.
     *
     * @param <R>      The type of the input value.
     * @param consumer The consumer to wrap.
     * @return A function that produces ConsumerWrapper instances.
     */
    public static <R> Function<R, ConsumerWrapper<R>> of(Consumer<R> consumer) {
        return input -> ConsumerWrapper.of(consumer, input);
    }
}
