package jica.spb.async.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.function.Function;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FunctionWrapper<I, O> {

    Function<I, O> function;

    I input;

    public static <R, S> FunctionWrapper<R, S> of(Function<R, S> function, R input) {
        return new FunctionWrapper<>(function, input);
    }

    public static <R, S> Function<R, FunctionWrapper<R, S>> of(Function<R, S> function) {
        return input -> FunctionWrapper.of(function, input);
    }

}
