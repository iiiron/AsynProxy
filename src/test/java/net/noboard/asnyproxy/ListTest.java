package net.noboard.asnyproxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ListTest {
    public static void main(String[] args) {
        List<Integer> run = AsynProxy.proxy(() -> {
                    List<Integer> objects = new ArrayList<>();
                    objects.add(1);
                    return objects;
                }, List.class)
                .executor(Executors.newSingleThreadExecutor())
                .timeOut(500L)
                .timeUnit(TimeUnit.MILLISECONDS)
                .fallback((e) -> {
                    return new ArrayList<>();
                })
                .run();


        System.out.println(run);
    }
}
