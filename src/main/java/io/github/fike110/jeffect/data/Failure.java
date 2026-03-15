package io.github.fike110.jeffect.data;

import java.util.Optional;
import java.util.function.Function;

public final class Failure<T> implements Result<T>{

    private final Throwable error;

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
