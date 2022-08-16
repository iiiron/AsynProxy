
package net.noboard.asnyproxy;


import jakarta.validation.constraints.NotNull;

@FunctionalInterface
public interface FallbackSupport<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    @NotNull
    T get(Throwable e);
}
