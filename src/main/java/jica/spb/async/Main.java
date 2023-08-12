package jica.spb.async;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        AsyncLoop asyncLoop = new AsyncLoop();
        asyncLoop.supply(IntStream.range(0, 100).mapToObj(Main::getRunnable))
                .whenException(System.out::println)
                .nonNullValues()
                .forEach(System.out::println);
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
