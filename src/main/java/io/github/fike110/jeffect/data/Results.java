package io.github.fike110.jeffect.data;

/**
 * Factory for creating Result instances.
 * 
 * <p>Provides static methods to create successful or failed results.</p>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * Result<String> success = Results.success("hello");
 * Result<String> failure = Results.failure(new RuntimeException("error"));
 * }</pre>
 */
public final class Results {
    
    private Results() {}

    /**
     * Creates a successful result with a value.
     * 
     * @param <T> the type of the value
     * @param value the success value
     * @return a Success result
     */
    public static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed result with an error.
     * 
     * @param <T> the type parameter
     * @param throwable the error
     * @return a Failure result
     */
    public static <T> Result<T> failure(Throwable throwable) {
        return new Failure<>(throwable);
    }
}
