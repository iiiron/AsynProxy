package net.noboard.asnyproxy;


class AsynProxyTest {
    public static void main(String[] args) throws TestException {
        System.out.println("start");

        // 触发CGLib代理
        Wrapper<TestA> wanxm = AsynProxy.proxyWithWrapper(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new TestA("wanxm");
        }, TestA.class);

        System.out.println("getName");
        System.out.println(wanxm.isPresent());
        System.out.println(wanxm.get().getName());
    }
}