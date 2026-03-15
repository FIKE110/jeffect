package io.github.fike110.jeffect;

@FunctionalInterface
public interface SupplierThrowing<T> {
    T get() throws Exception;
}