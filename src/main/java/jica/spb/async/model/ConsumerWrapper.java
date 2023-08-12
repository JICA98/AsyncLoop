package jica.spb.async.model;

import lombok.Value;

import java.util.function.Consumer;

@Value
public class ConsumerWrapper<I> {

    Consumer<I> consumer;

    I input;

}
