package net.noboard.asnyproxy;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class AsynProxyTest {
    public static void main(String[] args) throws TestException {
        System.out.println("start");

        AsynProxy<Wrapper<TestA>, TestA> optionalTestAAsynProxy = AsynProxy.proxyNullable(() -> {
                    if (true) {
                        throw new NullPointerException();
                    }

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return new TestA("wanxm");
                }, TestA.class)
                .timeOut(2000L).timeUnit(TimeUnit.MILLISECONDS);

        Wrapper<TestA> run = optionalTestAAsynProxy.run();

        System.out.println("getName");
        System.out.println(run.isPresent());
        System.out.println(run.get());
    }
}