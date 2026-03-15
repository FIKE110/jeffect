package io.github.fike110.jeffect.core;

import java.util.function.Function;

import io.github.fike110.jeffect.data.Result;
import io.github.fike110.jeffect.data.Results;

public final class EffectRuntime {

    public static <T> Result<T> run(Effect<T> effect) {
        while (true) {
            if (effect instanceof Pure<T> pure) {
                return Results.success(pure.value());
            } else if (effect instanceof Fail<T> fail) {
                return Results.failure(fail.error());
            } else if (effect instanceof Suspend<T> suspend) {
                effect = suspend.thunk().get();
            } else if (effect instanceof FlatMap<?, ?> flat) {
                Effect<Object> source = (Effect<Object>) flat.source();
                Function<Object, Effect<T>> f = (Function<Object, Effect<T>>) flat.f();

                if (source instanceof Suspend<Object> s) {
                    source = s.thunk().get();
                }

                if (source instanceof Pure<Object> p) {
                    effect = f.apply(p.value());
                } else if (source instanceof Fail<Object> failSource) {
                    return Results.failure(failSource.error());
                } else {
                    effect = new FlatMap<>(source, f);
                }
            }
            else if (effect instanceof Recover<T> recover) {
                Result<T> result = run(recover.source());

                if (result.isSuccess()) {
                    return result;
                }

                try {
                    effect = recover.handler().apply(result.getThrowable());
                } catch (Throwable t) {
                    return Results.failure(t);
                }
            }
        }
    }

}