package jica.spb.async;

import jica.spb.async.model.FnWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        AsyncLoop asyncLoop = new AsyncLoop();
        Stream<Integer> integers = Stream.of(1, 3, 4);
        asyncLoop.apply(integers.map(FnWrapper.wrap(Main::plusOne)))
                .whenException(System.out::println)
                .nonNullValues()
                .forEach(System.out::println);

    }

    private static int plusOne(int number) {
        return number + 1;
    }

    @NotNull
    private static Supplier<Integer> getRunnable(int finalI) {
        return () -> {
            System.out.println("hello " + finalI);
            if (finalI % 10 == 0) {
                throw new RuntimeException();
            } else if (finalI % 3 == 0) {
                return null;
            }
            return finalI;
        };
    }

}
