package jica.spb.async.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.function.Consumer;
import java.util.function.Function;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsumerWrapper<I> {

    Consumer<I> consumer;

    I input;

    public static <R> ConsumerWrapper<R> of(Consumer<R> consumer, R input) {
        return new ConsumerWrapper<>(consumer, input);
    }

    public static <R> Function<R, ConsumerWrapper<R>> wrap(Consumer<R> consumer) {
        return input -> ConsumerWrapper.of(consumer, input);
    }
}
