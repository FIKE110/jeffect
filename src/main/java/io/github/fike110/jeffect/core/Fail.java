package io.github.fike110.jeffect.core;

import java.util.function.Function;

/**
 * A failed effect containing an error.
 * 
 * <p>Represents an Effect that has failed with an exception.</p>
 * 
 * @param <T> the type parameter (for type consistency - value is never present)
 */
public final class Fail<T> implements Effect<T>{

    /** The error that caused the failure */
    private final Throwable throwable;

    /**
     * Creates a Fail effect with the given error.
     * 
     * @param throwable the error to contain
     */
    public Fail(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * Returns the error that caused the failure.
     * 
     * @return the throwable error
     */
    public Throwable error() {
        return throwable;
    }


    @Override
    public <R> Effect<R> map(Function<T, R> mapper) {
        return new Fail<>(throwable);
    }

    @Override
    public <R> Effect<R> flatMap(Function<T, Effect<R>> mapper) {
        return new Fail<>(throwable);
    }

}
