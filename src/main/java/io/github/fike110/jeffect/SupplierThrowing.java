package io.github.fike110.jeffect;

/**
 * A supplier that can throw an exception.
 * 
 * <p>Similar to {@link java.util.function.Supplier} but can throw
 * checked exceptions. Used with {@link Effects#fromSupplierThrowing}.</p>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * SupplierThrowing<String> throwingSupplier = () -> {
 *     if (Math.random() > 0.5) {
 *         throw new IOException("Random failure");
 *     }
 *     return "success";
 * };
 * 
 * Effect<String> effect = Effects.fromSupplierThrowing(throwingSupplier);
 * }</pre>
 * 
 * @param <T> the type of value returned
 */
@FunctionalInterface
public interface SupplierThrowing<T> {
    /**
     * Gets a result or throws an exception.
     * 
     * @return a result
     * @throws Exception if an error occurs
     */
    T get() throws Exception;
}