package net.noboard.asnyproxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author by wanxm
 * @date 2021/8/25 3:02 下午
 */
public class AsynProxy<T, Q> implements InvocationHandler, MethodInterceptor {

    private static final Executor defaultExecutor = Executors.newCachedThreadPool();

    private Executor executor;

    private CompletableFuture<T> completableFuture;

    private T result = null;

    private boolean isRun = false;

    private Long timeOut;

    private TimeUnit timeUnit;

    private FallbackSupport<T> fallback;

    private Class<?> clazz;

    private Support<T> support;


    private AsynProxy(Support<T> support, Class<?> clazz) {
        this.clazz = clazz;
        this.support = support;
    }

    public static <T> AsynProxy<T, T> proxy(Support<T> support, Class<?> clazz) {
        return new AsynProxy<>(support, clazz);
    }

    public static <T> AsynProxy<Wrapper<T>, T> proxyNullable(Supplier<T> supplier, Class<?> clazz) {
        return new AsynProxy<>(() -> Wrapper.ofNullable(supplier.get()), Wrapper.class);
    }

    public AsynProxy<T, Q> executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public AsynProxy<T, Q> timeOut(Long timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    public AsynProxy<T, Q> timeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public AsynProxy<T, Q> fallback(FallbackSupport<T> fallback) {
        this.fallback = fallback;
        return this;
    }


    public AsynProxy<T, Q> fallbackNullable(FallbackSupport<Q> fallback) {
        this.fallback = (e) -> (T) Wrapper.ofNullable(fallback.get(e));
        return this;
    }

    public T run() {
        String threadName = Thread.currentThread().getName();

        this.completableFuture = CompletableFuture.supplyAsync(() -> {
            String originThreadName = Thread.currentThread().getName();
            Thread.currentThread().setName(threadName);
            try {
                return support.get();
            } finally {
                Thread.currentThread().setName(originThreadName);
            }
        }, this.executor == null ? defaultExecutor : executor);


        Object proxy = null;
        if (clazz.isInterface()) {
            proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        } else {
            Enhancer enhancer = new Enhancer();
            enhancer.setUseCache(true);
            enhancer.setSuperclass(clazz);
            enhancer.setCallback(this);
            proxy = enhancer.create();
        }

        return (T) proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        T data = getResult();
        if (data == null) {
            return null;
        }
        return method.invoke(data, args);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        T data = getResult();
        if (data == null) {
            return null;
        }
        return methodProxy.invoke(data, objects);
    }

    private synchronized T getResult() throws Throwable {
        if (!isRun) {
            try {
                if (timeOut != null) {
                    result = completableFuture.get(timeOut, timeUnit);
                } else {
                    result = completableFuture.get();
                }
            } catch (Exception e) {
                if (fallback != null) {
                    result = fallback.get(e);
                } else {
                    result = null;
                }
            } finally {
                isRun = true;
            }
        }

        return result;
    }
}
