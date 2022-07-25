
package net.noboard.asnyproxy;

@FunctionalInterface
public interface FallbackSupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get(Throwable e);
}
