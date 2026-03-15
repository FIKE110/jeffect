package io.github.fike110.jeffect.factory;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.fike110.jeffect.core.Effect;
import io.github.fike110.jeffect.core.Fail;
import io.github.fike110.jeffect.core.Pure;
import io.github.fike110.jeffect.core.Suspend;

/**
 * Low-level factory for creating Effect instances.
 * 
 * <p>This class provides direct constructors for the core Effect types:
 * Pure, Fail, and Suspend. For most use cases, prefer {@link io.github.fike110.jeffect.Effects}.</p>
 * 
 * <h2>When to use this class</h2>
 * <ul>
 *   <li>When you need fine-grained control over effect construction</li>
 *   <li>When performance is critical and you want to avoid overhead</li>
 * </ul>
 * 
 * <h2>Factory Methods</h2>
 * <ul>
 *   <li>{@link #success(Object)} - Creates a successful effect</li>
 *   <li>{@link #fail(Throwable)} - Creates a failed effect</li>
 *   <li>{@link #suspend(Supplier)} - Creates a suspended effect</li>
 *   <li>{@link #of(Supplier)} - Creates a deferred effect from a supplier</li>
 *   <li>{@link #of(Runnable)} - Creates an effect from a runnable</li>
 *   <li>{@link #tryCatch(Callable, Function)} - Creates an effect with error mapping</li>
 * </ul>
 */
public final class EffectFactory {

    private EffectFactory() {}

    /**
     * Creates a successful effect.
     * 
     * @param <T> the type of value
     * @param value the value
     * @return a successful effect
     */
    public static <T> Effect<T> success(T value) {
        return new Pure<>(value);
    }

    /**
     * Creates a failed effect with an error.
     * 
     * @param <T> the type parameter
     * @param throwable the error
     * @return a failed effect
     */
    public static <T> Effect<T> fail(Throwable throwable) {
        return new Fail<>(throwable);
    }

    /**
     * Creates a suspended effect.
     * 
     * @param <T> the type of value
     * @param supplier the supplier that produces the effect
     * @return a suspended effect
     */
    public static <T> Effect<T> suspend(Supplier<Effect<T>> supplier) {
        return new Suspend<>(supplier);
    }

    /**
     * Creates a deferred effect from a supplier.
     * 
     * @param <T> the type of value
     * @param supplier the supplier
     * @return a deferred effect
     */
    public static <T> Effect<T> of(Supplier<T> supplier) {
        return new Suspend<>(() -> {
            try {
                return new Pure<>(supplier.get());
            } catch (Throwable e) {
                return new Fail<>(e);
            }
        });
    }

    /**
     * Creates an effect from a runnable.
     * 
     * @param runnable the runnable
     * @return an effect that completes when the runnable finishes
     */
    public static Effect<Void> of(Runnable runnable) {
        return new Suspend<>(() -> {
            try {
                runnable.run();
                return new Pure<>(null);
            } catch (Throwable e) {
                return new Fail<>(e);
            }
        });
    }

    /**
     * Creates an effect with custom error mapping.
     * 
     * @param <T> the type of value
     * @param action the callable to execute
     * @param mapper function to map exceptions
     * @return an effect with mapped errors
     */
    public static <T> Effect<T> tryCatch(Callable<T> action, Function<Throwable, ? extends Throwable> mapper) {
        return new Suspend<>(() -> {
            try {
                return new Pure<>(action.call());
            } catch (Throwable e) {
                return new Fail<>(mapper.apply(e));
            }
        });
    }
}
