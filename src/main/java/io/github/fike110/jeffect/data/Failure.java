package io.github.fike110.jeffect.data;

import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a failed result containing an error.
 * 
 * <p>Failure is one of the two variants of {@link Result}, representing
 * a computation that failed with an exception.</p>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * Result<String> result = Results.failure(new RuntimeException("error"));
 * if (result.isFailure()) {
 *     System.out.println(result.getThrowable()); // prints the error
 * }
 * }</pre>
 * 
 * @param <T> the type parameter (value is never present)
 */
public final class Failure<T> implements Result<T>{

    /** The error that caused the failure */
    private final Throwable error;

    /**
     * Creates a Failure with the given error.
     * 
     * @param error the error that caused the failure
     */
    public Failure(Throwable error) {
        this.error = error;
    }

    @Override
    public Throwable getThrowable() {
        return error;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public T get() {
        return null;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public Optional<T> optional(){
        return Optional.empty();
    }


    @Override
    public T getOrThrow() {
        throw new RuntimeException(error);
    }

    @Override
    public T getOrElse(T defaultValue) {
        return defaultValue;
    }

    @Override
    public <R> Result<R> map(Function<T, R> mapper) {
        return new Failure<>(error);
    }

    @Override
    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        return new Failure<>(error);
    }

    @Override
    public <R> R fold(Function<Throwable, R> onFailure, Function<T, R> onSuccess) {
        return onFailure.apply(error);
    }

    @Override
    public String toString() {
        return "Fail(error=" + error + ")";
    }
}
