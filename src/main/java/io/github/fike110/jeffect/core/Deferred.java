package io.github.fike110.jeffect.core;

import io.github.fike110.jeffect.Effects;
import io.github.fike110.jeffect.data.Result;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a deferred effect that waits for an input value to be supplied later.
 * 
 * <p>This is useful when you want to define an effect computation upfront
 * but provide the input value at execution time.</p>
 * 
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * Deferred<User, String> getName = Effects.effectOf(user -> user.getName());
 * String name = getName.run(user);
 * }</pre>
 * 
 * <h2>With Side Effects</h2>
 * <pre>{@code
 * Deferred<User, Void> printUser = Effects.effectOfVoid(user -> System.out.println(user));
 * printUser.run(user);
 * }</pre>
 * 
 * @param <T> the input type
 * @param <R> the result type
 */
public interface Deferred<T, R> {

    /**
     * Creates an Effect by supplying the input value.
     * 
     * @param input the input value
     * @return an Effect containing the result
     */
    Effect<R> supply(T input);

    /**
     * Executes the deferred effect and returns the result.
     * 
     * @param input the input value
     * @return the result value
     * @throws RuntimeException if the effect fails
     */
    default R run(T input) {
        return supply(input).run();
    }

    /**
     * Executes the deferred effect safely, returning a Result.
     * 
     * @param input the input value
     * @return a Result containing the value or error
     */
    default Result<R> runSafe(T input) {
        return supply(input).runSafe();
    }

    /**
     * Transforms the result of this deferred effect.
     * 
     * @param mapper the function to transform the result
     * @param <U> the new result type
     * @return a new Deferred with transformed result
     */
    default <U> Deferred<T, U> map(Function<R, U> mapper) {
        return input -> supply(input).map(mapper);
    }

    /**
     * Chains another deferred effect that depends on the result.
     * 
     * @param mapper the function to chain
     * @param <U> the new result type
     * @return a new Deferred with chained effect
     */
    default <U> Deferred<T, U> flatMap(Function<R, Deferred<T, U>> mapper) {
        return input -> supply(input).flatMap(r -> mapper.apply(r).supply(input));
    }

    /**
     * Executes a side effect with the result without changing it.
     * 
     * @param action the consumer to execute
     * @return a new Deferred with the tap applied
     */
    default Deferred<T, R> tap(Consumer<R> action) {
        return input -> supply(input).tap(action);
    }

    /**
     * Recovers from failures in the deferred effect.
     * 
     * @param handler the error handler function
     * @return a new Deferred with recovery
     */
    default Deferred<T, R> recover(Function<Throwable, R> handler) {
        return input -> supply(input).recover(handler);
    }

    /**
     * Recovers from failures with another deferred effect.
     * 
     * @param handler the error handler returning a Deferred
     * @return a new Deferred with recovery
     */
    default Deferred<T, R> recoverWith(Function<Throwable, Deferred<T, R>> handler) {
        return input -> supply(input).recoverWith(err -> handler.apply(err).supply(input));
    }
}
