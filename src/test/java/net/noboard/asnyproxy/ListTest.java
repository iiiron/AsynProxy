package net.noboard.asnyproxy;

import java.util.ArrayList;
import java.util.List;

public class ListTest {
    public static void main(String[] args) {
        List<Integer> run = AsynProxy.proxy(() -> {
            if (true) {
                return new ArrayList<Integer>();
            }
            List<Integer> objects = new ArrayList<>();
            objects.add(1);
            return objects;
        }, List.class).run();
    }
}
