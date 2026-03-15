package io.github.fike110.jeffect.data;

public final class Results {
    private Results() {}

    public static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    public static <T> Result<T> failure(Throwable throwable) {
        return new Failure<>(throwable);
    }
}
