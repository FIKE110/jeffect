package io.github.fike110.jeffect.data;

import java.util.Optional;

/**
 * Represents the result of an effect execution - either success or failure.
 * 
 * <p>Similar to Either&lt;Throwable, T&gt; but with convenient methods for handling both cases.</p>
 * 
 * @param <T> the type of the success value
 */
public sealed interface Result<T>
        permits Success, Failure {

    /**
     * Checks if this result is successful.
     * 
     * @return true if success, false if failure
     */
    boolean isSuccess();

    /**
     * Checks if this result is a failure.
     * 
     * @return true if failure, false if success
     */
    boolean isFailure();

    /**
     * Returns the value or throws the contained exception.
     * 
     * @return the value
     * @throws Throwable the contained error
     */
    T getOrThrow();

    /**
     * Returns the value or throws RuntimeException if failed.
     * 
     * @return the value
     * @throws RuntimeException if failed
     */
    T get();

    /**
     * Converts to Optional - empty if failed.
     * 
     * @return Optional containing the value or empty
     */
    Optional<T> optional();

    /**
     * Returns the contained error.
     * 
     * @return the throwable error
     */
    Throwable getThrowable();

    /**
     * Returns the value or a default if failed.
     * 
     * @param defaultValue the value to return on failure
     * @return the result value or default
     */
    T getOrElse(T defaultValue);

    /**
     * Maps the success value to another value.
     * 
     * @param mapper the transformation function
     * @param <R> the new type
     * @return a new Result with the mapped value
     */
    <R> Result<R> map(java.util.function.Function<T, R> mapper);

    /**
     * FlatMaps the success value to another Result.
     * 
     * @param mapper the transformation function
     * @param <R> the new type
     * @return a new Result
     */
    <R> Result<R> flatMap(java.util.function.Function<T, Result<R>> mapper);

    /**
     * Folds both success and failure cases into a single value.
     * 
     * @param onFailure function to handle failure
     * @param onSuccess function to handle success
     * @param <R> the result type
     * @return the folded value
     */
    <R> R fold(
            java.util.function.Function<Throwable, R> onFailure,
            java.util.function.Function<T, R> onSuccess
    );

}