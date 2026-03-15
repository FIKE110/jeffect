package io.github.fike110.jeffect.core;

import java.util.function.Function;

/**
 * Represents error recovery for an Effect.
 * 
 * <p>Wraps an effect with error handling capabilities.</p>
 * 
 * @param <T> the type of the effect value
 */
public final class Recover<T> implements Effect<T> {

    /** The source effect to recover from */
    private final Effect<T> source;
    
    /** The handler function for errors */
    private final Function<Throwable, Effect<T>> handler;

    /**
     * Creates a Recover effect with a source and error handler.
     * 
     * @param source the source effect
     * @param handler the error recovery handler
     */
    public Recover(
            Effect<T> source,
            Function<Throwable, Effect<T>> handler
    ) {
        this.source = source;
        this.handler = handler;
    }

    /**
     * Returns the source effect.
     * 
     * @return the source Effect
     */
    public Effect<T> source() {
        return source;
    }

    /**
     * Returns the error handler function.
     * 
     * @return the handler function
     */
    public Function<Throwable, Effect<T>> handler() {
        return handler;
    }

    @Override
    public <R> Effect<R> flatMap(Function<T, Effect<R>> mapper) {
        return null;
    }
}