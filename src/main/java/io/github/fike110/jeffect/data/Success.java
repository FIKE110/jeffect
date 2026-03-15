package io.github.fike110.jeffect.data;

import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a successful result containing a value.
 * 
 * <p>Success is one of the two variants of {@link Result}, representing
 * a computation that completed successfully with a value.</p>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * Result<String> result = Results.success("hello");
 * if (result.isSuccess()) {
 *     System.out.println(result.get()); // prints "hello"
 * }
 * }</pre>
 * 
 * @param <T> the type of the success value
 */
public final class Success<T> implements Result<T> {
    
    /** The success value */
    private final T value;

    /**
     * Creates a Success with the given value.
     * 
     * @param value the success value
     */
    public Success(T value) {
        this.value = value;
    }


    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public T getOrThrow() {
        return null;
    }

    @Override
    public T getOrElse(T defaultValue) {
        return value;
    }

    @Override
    public <R> Result<R> map(Function<T, R> mapper) {
        try{
            return new Success<>(mapper.apply(value));
        }
        catch (Throwable e) {
            return new Failure<>(e);
        }
    }

    @Override
    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        try {
            return mapper.apply(value);
        } catch (Throwable e) {
            return new Failure<>(e);
        }
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public Optional<T> optional(){
        return Optional.ofNullable(value);
    }

    @Override
    public <R> R fold(Function<Throwable, R> onFailure, Function<T, R> onSuccess) {
        return onSuccess.apply(value);
    }

    @Override
    public String toString() {
        return "Success(" + value + ")";
    }
}
