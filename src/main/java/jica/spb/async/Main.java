package jica.spb.async;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        Async async = new Async();
        async.supply(IntStream.range(0, 100)
                .mapToObj(Main::getRunnable));
    }

    @NotNull
    private static Supplier<Integer> getRunnable(int finalI) {
        return () -> {
            System.out.println("hello " + finalI);
            if (finalI == 66) {
                throw new RuntimeException();
            }
            return finalI;
        };
    }

}
