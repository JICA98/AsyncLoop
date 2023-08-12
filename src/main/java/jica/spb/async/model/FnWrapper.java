package jica.spb.async.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.function.Function;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FnWrapper<I, O> {

    Function<I, O> function;

    I input;

    public static <R, S> FnWrapper<R, S> of(Function<R, S> function, R input) {
        return new FnWrapper<>(function, input);
    }

    public static <R, S> Function<R, FnWrapper<R, S>> wrap(Function<R, S> function) {
        return input -> FnWrapper.of(function, input);
    }

}
