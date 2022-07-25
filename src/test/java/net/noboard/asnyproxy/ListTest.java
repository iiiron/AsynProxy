package net.noboard.asnyproxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ListTest {
    public static void main(String[] args) {
        List<Integer> run = AsynProxy.proxy(() -> {
                    if (false) {
                        return new ArrayList<Integer>();
                    }
                    List<Integer> objects = new ArrayList<>();
                    objects.add(1);
                    return objects;
                }, List.class)
                .executor(Executors.newSingleThreadExecutor())
                .run();

        System.out.println(run);
    }
}
