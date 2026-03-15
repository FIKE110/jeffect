package io.github.fike110.jeffect.factory;

import java.util.function.Function;
import java.util.function.Supplier;

import io.github.fike110.jeffect.core.Effect;
import io.github.fike110.jeffect.core.Fail;
import io.github.fike110.jeffect.core.Pure;
import io.github.fike110.jeffect.core.Suspend;

import java.util.concurrent.Callable;

public final class EffectFactory {

    private EffectFactory() {}

    public static <T> Effect<T> pure(T value) {
        return new Pure<>(value);
    }

    public static <T> Effect<T> success(T value) {
        return new Pure<>(value);
    }

    public static <T> Effect<T> fail(Throwable throwable) {
        return new Fail<>(throwable);
    }

    public static <T> Effect<T> suspend(Supplier<Effect<T>> supplier) {
        return new Suspend<>(supplier);
    }

    public static <T> Effect<T> of(Supplier<T> supplier) {
        return new Suspend<>(() -> {
            try {
                return new Pure<>(supplier.get());
            } catch (Throwable e) {
                return new Fail<>(e);
            }
        });
    }

    public static Effect<Void> of(Runnable runnable) {
        return new Suspend<>(() -> {
            try {
                runnable.run();
                return new Pure<>(null);
            } catch (Throwable e) {
                return new Fail<>(e);
            }
        });
    }

    public static <T> Effect<T> tryCatch(Callable<T> action, Function<Throwable, ? extends Throwable> mapper) {
        return new Suspend<>(() -> {
            try {
                return new Pure<>(action.call());
            } catch (Throwable e) {
                return new Fail<>(mapper.apply(e));
            }
        });
    }
}
