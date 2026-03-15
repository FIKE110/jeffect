package io.github.fike110.jeffect.core;

import java.util.function.Function;
import java.util.function.Supplier;

import io.github.fike110.jeffect.Effects;

/**
 * A suspended effect that defers evaluation until executed.
 * 
 * <p>The supplier is not called until the effect is run, enabling lazy evaluation.</p>
 * 
 * @param <T> the type of the effect value
 */
public final class Suspend<T> implements Effect<T> {
    
    /** The supplier that produces the effect when evaluated */
    private final Supplier<Effect<T>> supplier;

    /**
     * Creates a Suspend effect with the given supplier.
     * 
     * @param supplier the supplier that produces an Effect
     */
    public Suspend(Supplier<Effect<T>> supplier) {
        this.supplier = supplier;
    }

    /**
     * Returns the supplier that produces the effect.
     * 
     * @return the supplier
     */
    public Supplier<Effect<T>> thunk() {
        return supplier;
    }

    @Override
    public <R> Effect<R> map(Function<T, R> mapper) {
        return new FlatMap<>(this, v -> Effects.success(mapper.apply(v)));
    }

    @Override
    public <R> Effect<R> flatMap(Function<T, Effect<R>> mapper) {
        return new FlatMap<>(this, mapper);
    }

}
