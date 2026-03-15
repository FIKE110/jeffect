package io.github.fike110.jeffect.core;

import java.util.function.Function;

/**
 * A successful effect containing a value.
 * 
 * <p>Represents an Effect that has completed successfully with a value.</p>
 * 
 * @param <T> the type of the contained value
 */
public final class Pure<T> implements Effect<T> {
    
    /** The contained value */
    private final T value;

    /**
     * Creates a Pure effect with the given value.
     * 
     * @param value the value to contain
     */
    public Pure(T value) {
        this.value = value;
    }

    /**
     * Returns the contained value.
     * 
     * @return the value
     */
    public T value() {
        return value;
    }

    @Override
    public <R> Effect<R> map(Function<T, R> mapper) {
        try {
            return new Pure<>(mapper.apply(value));
        } catch (Throwable e) {
            return new Fail<>(e);
        }
    }

    @Override
    public <R> Effect<R> flatMap(Function<T, Effect<R>> mapper) {
        try {
            return mapper.apply(value);
        } catch (Throwable e) {
            return new Fail<>(e);
        }
    }

}
