package jica.spb.async.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.function.Function;

/**
 * A wrapper class that combines a function and its input value.
 *
 * @param <I> The type of the input value for the function.
 * @param <O> The type of the output value produced by the function.
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FunctionWrapper<I, O> {

    Function<I, O> function;

    I input;

    /**
     * Creates a new FunctionWrapper instance with the given function and input value.
     *
     * @param <R>      The type of the input value.
     * @param <S>      The type of the output value.
     * @param function The function to wrap.
     * @param input    The input value for the function.
     * @return A new FunctionWrapper instance.
     */
    public static <R, S> FunctionWrapper<R, S> of(Function<R, S> function, R input) {
        return new FunctionWrapper<>(function, input);
    }

    /**
     * Creates a function that produces FunctionWrapper instances with the provided function and accepts an input value.
     *
     * @param <R>      The type of the input value.
     * @param <S>      The type of the output value.
     * @param function The function to wrap.
     * @return A function that produces FunctionWrapper instances.
     */
    public static <R, S> Function<R, FunctionWrapper<R, S>> of(Function<R, S> function) {
        return input -> FunctionWrapper.of(function, input);
    }

}
