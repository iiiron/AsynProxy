
package net.noboard.asnyproxy;


import org.springframework.lang.NonNull;

@FunctionalInterface
public interface NullableFallbackSupport<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    @NonNull
    T get(Throwable e);
}
