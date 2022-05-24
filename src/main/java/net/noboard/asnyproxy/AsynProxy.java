package net.noboard.asnyproxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @author by wanxm
 * @date 2021/8/25 3:02 下午
 */
public class AsynProxy<T> implements InvocationHandler, MethodInterceptor {

    private static final Executor defaultExecutor = Executors.newCachedThreadPool();

    private final CompletableFuture<T> completableFuture;

    private T result = null;

    private boolean isRun = false;

    private final Long timeOut;

    private final TimeUnit timeUnit;

    private final Support<T> fallback;

    private AsynProxy(CompletableFuture<T> completableFuture, Long timeOut, TimeUnit timeUnit, Support<T> fallback) {
        this.completableFuture = completableFuture;
        this.timeOut = timeOut;
        this.timeUnit = timeUnit;
        this.fallback = fallback;
    }

    public static <T> T proxy(Support<T> supplier, Class<?> proxyFace, Executor executor, Long timeOut, TimeUnit timeUnit, Support<T> fallback) {
        String threadName = Thread.currentThread().getName();

        AsynProxy<T> tAsynProxy;
        tAsynProxy = new AsynProxy<>(CompletableFuture.supplyAsync(() -> {
            String originThreadName = Thread.currentThread().getName();
            Thread.currentThread().setName(threadName);
            try {
                return supplier.get();
            } finally {
                Thread.currentThread().setName(originThreadName);
            }
        }, executor == null ? defaultExecutor : executor), timeOut, timeUnit, fallback);

        Object proxy = null;
        if (proxyFace.isInterface()) {
            proxy = Proxy.newProxyInstance(proxyFace.getClassLoader(), new Class[]{proxyFace}, tAsynProxy);
        } else {
            Enhancer enhancer = new Enhancer();
            enhancer.setUseCache(true);
            enhancer.setSuperclass(proxyFace);
            enhancer.setCallback(tAsynProxy);
            proxy = enhancer.create();
        }

        return (T) proxy;
    }

    public static <T> T proxy(Support<T> supplier, Class<?> tClass, Executor executor) {
        return proxy(supplier, tClass, executor, null, null, null);
    }

    public static <T> T proxy(Support<T> supplier, Class<?> tClass) {
        return proxy(supplier, tClass, null, null, null, null);
    }

    public static <T> T proxy(Support<T> supplier, Class<?> tClass, Long timeOut, TimeUnit timeUnit, Support<T> fallback) {
        return proxy(supplier, tClass, null, timeOut, timeUnit, fallback);
    }

    public static <T> Wrapper<T> proxyWithWrapper(Supplier<T> supplier, Class<?> tClass, Executor executor, Long timeOut, TimeUnit timeUnit, Supplier<T> fallback) {
        return proxy(() -> Wrapper.ofNullable(supplier.get()), Wrapper.class, executor, timeOut, timeUnit, () -> Wrapper.ofNullable(fallback.get()));
    }

    public static <T> Wrapper<T> proxyWithWrapper(Supplier<T> supplier, Class<?> tClass) {
        return proxyWithWrapper(supplier, tClass, null, null, null, null);
    }

    public static <T> Wrapper<T> proxyWithWrapper(Supplier<T> supplier, Class<?> tClass, Long timeOut, TimeUnit timeUnit, Supplier<T> fallback) {
        return proxyWithWrapper(supplier, tClass, null, timeOut, timeUnit, fallback);
    }

    public static <T> Wrapper<T> proxyWithWrapper(Supplier<T> supplier, Class<?> tClass, Executor executor) {
        return proxyWithWrapper(supplier, tClass, executor, null, null, null);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(getResult(), args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        return methodProxy.invoke(getResult(), objects);
    }

    private synchronized T getResult() throws Throwable {
        if (!isRun) {
            try {
                if (timeOut != null) {
                    result = completableFuture.get(timeOut, timeUnit);
                } else {
                    result = completableFuture.get();
                }
            } catch (TimeoutException e) {
                if (fallback != null) {
                    result = fallback.get();
                } else {
                    result = null;
                }
            } catch (ExecutionException e) {
                throw e.getCause();
            } finally {
                isRun = true;
            }
        }

        return result;
    }
}
