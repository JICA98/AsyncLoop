package jica.spb.async.model;

import lombok.Value;

import java.util.function.Function;

@Value
public class FunctionWrapper<I, O> {

    Function<I, O> function;

    I input;

}
