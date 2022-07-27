
package net.noboard.asnyproxy;


import jakarta.validation.constraints.NotNull;

@FunctionalInterface
public interface Support<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    @NotNull
    T get();
}
