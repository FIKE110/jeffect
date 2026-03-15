package io.github.fike110.jeffect.core;

import java.util.function.Function;

import io.github.fike110.jeffect.Effects;

/**
 * Represents the flatMap (bind) operation on Effects.
 * 
 * <p>Chains two effects together, where the second effect depends on the result of the first.</p>
 * 
 * @param <A> the source effect type
 * @param <B> the result effect type
 */
public final class FlatMap<A, B> implements Effect<B> {
    
    /** The source effect */
    private final Effect<A> source;
    
    /** The function to apply to the source result */
    public final Function<? super A, ? extends Effect<? extends B>> f;

    /**
     * Creates a FlatMap with a source effect and transformation function.
     * 
     * @param source the source effect
     * @param f the function to transform the source result
     */
    public FlatMap(Effect<A> source, Function<? super A, ? extends Effect<? extends B>> f) {
        this.source = source;
        this.f = f;
    }

    /**
     * Returns the source effect.
     * 
     * @return the source Effect
     */
    public Effect<A> source() {
        return source;
    }

    /**
     * Returns the transformation function.
     * 
     * @return the transformation function
     */
    public Function<? super A, ? extends Effect<? extends B>> f() {
        return f;
    }

    @Override
    public <R> Effect<R> map(Function<B, R> mapper) {
        return new FlatMap<>(this, b -> Effects.success(mapper.apply(b)));
    }

    @Override
    public <R> Effect<R> flatMap(Function<B, Effect<R>> mapper) {
        return new FlatMap<>(this, mapper);
    }

}