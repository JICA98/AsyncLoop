package jica.spb.async;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        AsyncLoop asyncLoop = new AsyncLoop();
        asyncLoop.submit(IntStream.range(0, 100)
                        .mapToObj(Main::getRunnable))
                .whenException(e -> {
                    throw new RuntimeException("Some Exception");
                })
                .getResults()
                .forEach(option -> {
//                    System.out.println(option);
                    if (option.hasValue()) {
                        System.out.println("value" + option.value());
                    }
                });
    }

    @NotNull
    private static Supplier<Integer> getRunnable(int finalI) {
        return () -> {
            System.out.println("hello " + finalI);
            if (finalI % 10 == 0) {
                throw new RuntimeException();
            }
            return finalI;
        };
    }

}
