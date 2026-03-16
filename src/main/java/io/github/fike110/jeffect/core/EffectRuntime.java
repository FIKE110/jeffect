package io.github.fike110.jeffect.core;

import java.util.function.Function;

import io.github.fike110.jeffect.data.Result;
import io.github.fike110.jeffect.data.Results;

/**
 * Runtime engine for executing Effects.
 * 
 * <p>This class handles the evaluation of Effect expressions by pattern
 * matching on the different effect types (Pure, Fail, Suspend, FlatMap, Recover).</p>
 * 
 * <h2>How it works</h2>
 * <ul>
 *   <li>{@link Pure} - returns the contained value</li>
 *   <li>{@link Fail} - returns a failure result</li>
 *   <li>{@link Suspend} - evaluates the thunk to get the next effect</li>
 *   <li>{@link FlatMap} - chains effects together</li>
 *   <li>{@link Recover} - handles errors and potentially recovers</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * Effect<String> effect = Effects.success("hello");
 * Result<String> result = EffectRuntime.run(effect);
 * }</pre>
 */
public final class EffectRuntime {

    /**
     * Runs an effect and returns the result.
     * 
     * <p>This is the main entry point for effect execution.</p>
     * 
     * @param <T> the type of the effect value
     * @param effect the effect to run
     * @return a Result containing either the value or an error
     */
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

                while (source instanceof Suspend<Object> s) {
                    source = s.thunk().get();
                }

                if (source instanceof Pure<Object> p) {
                    effect = f.apply(p.value());
                } else if (source instanceof Fail<Object> failSource) {
                    return Results.failure(failSource.error());
                } else if (source instanceof FlatMap<?, ?> nestedFlat) {
                    Effect<Object> nestedSource = (Effect<Object>) nestedFlat.source();
                    Function<Object, Effect<Object>> nestedF = (Function<Object, Effect<Object>>) nestedFlat.f();
                    effect = new FlatMap<>(nestedSource, x -> nestedF.apply(x).flatMap(f));
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