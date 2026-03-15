package io.github.fike110.jeffect;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.github.fike110.jeffect.core.Effect;
import io.github.fike110.jeffect.core.EffectRuntime;
import io.github.fike110.jeffect.core.Fail;
import io.github.fike110.jeffect.core.Fiber;
import io.github.fike110.jeffect.core.Pure;
import io.github.fike110.jeffect.core.Suspend;
import io.github.fike110.jeffect.data.Result;

/**
 * Factory for creating Effect instances.
 * 
 * <h2>Creating Effects</h2>
 * <pre>{@code
 * // From a value
 * Effect<String> success = Effects.success("hello");
 * 
 * // From a supplier (deferred execution)
 * Effect<User> user = Effects.of(() -> userRepository.findById(id));
 * 
 * // From a runnable (for side effects)
 * Effect<Void> email = Effects.of(() -> sendEmail());
 * 
 * // From a callable with error mapping
 * Effect<Data> data = Effects.tryCatch(
 *     () -> fetchData(),
 *     e -> new CustomException("Failed", e)
 * );
 * }</pre>
 * 
 * <h2>Sequencing</h2>
 * <pre>{@code
 * List<Effect<User>> users = userIds.stream()
 *     .map(id -> Effects.of(() -> repo.findById(id)))
 *     .collect(Collectors.toList());
 * 
 * Effect<List<User>> allUsers = Effects.sequence(users);
 * }</pre>
 */
public final class Effects {
    private Effects() {}

    /**
     * The default scheduler for async operations.
     */
    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    /**
     * Returns the default scheduler.
     * @return the default ScheduledExecutorService
     */
    public static ScheduledExecutorService defaultScheduler() {
        return scheduler;
    }

    /**
     * Sets a custom scheduler for async operations.
     * @param newScheduler the scheduler to use
     */
    public static void setScheduler(ScheduledExecutorService newScheduler) {
        scheduler = newScheduler;
    }

    /**
     * Creates a new scheduler with the specified number of threads.
     * @param threads the number of threads
     * @return a new ScheduledExecutorService
     */
    public static ScheduledExecutorService createScheduler(int threads) {
        return Executors.newScheduledThreadPool(threads);
    }

    /**
     * Shuts down the default scheduler.
     */
    public static void shutdownScheduler() {
        scheduler.shutdown();
    }

    /**
     * Sequences a list of Effects into a single Effect containing a list.
     * 
     * @param effects the list of effects to sequence
     * @param <T> the type of each effect's value
     * @return an Effect containing a list of all values
     */
    public static <T> Effect<List<T>> sequence(List<Effect<T>> effects) {
        Effect<List<T>> acc = Effects.success(new ArrayList<>());

        for (Effect<T> e : effects) {
            acc = acc.flatMap(list ->
                    e.map(v -> {
                        list.add(v);
                        return list;
                    })
            );
        }

        return acc;
    }

    /**
     * Maps a list to a list of Effects, then sequences them.
     * 
     * @param list the input list
     * @param f the function to map each element to an Effect
     * @param <T> the input type
     * @param <R> the output effect type
     * @return an Effect containing a list of mapped values
     */
    public static <T, R> Effect<List<R>> traverse(List<T> list, Function<T, Effect<R>> f) {
        List<Effect<R>> effects = list.stream().map(f).collect(Collectors.toList());
        return sequence(effects);
    }

    /**
     * Creates a deferred effect from a Supplier.
     * Exceptions thrown by the supplier are captured as failures.
     * 
     * @param supplier the supplier that produces the value
     * @param <T> the type of value produced
     * @return an Effect containing the supplier's value or an error
     */
    public static <T> Effect<T> of(Supplier<T> supplier) {
        return new Suspend<>(() -> {
            try {
                return Effects.success(supplier.get());
            } catch (Throwable e) {
                return Effects.fail(e);
            }
        });
    }

    /**
     * Creates a deferred effect from a Runnable.
     * Useful for side effects that don't return a value.
     * 
     * @param runnable the runnable to execute
     * @return an Effect containing void (null)
     */
    public static Effect<Void> of(Runnable runnable) {
        return new Suspend<>(() -> {
            try {
                runnable.run();
                return Effects.success(null);
            } catch (Throwable e) {
                return Effects.fail(e);
            }
        });
    }


    /**
     * Creates an effect from a Callable with custom error mapping.
     * 
     * @param action the callable to execute
     * @param mapper function to map exceptions to custom errors
     * @param <T> the type of value produced
     * @return an Effect containing the callable's value or a mapped error
     * @throws NullPointerException if mapper returns null
     */
    public static <T> Effect<T> tryCatch(Callable<T> action, Function<Throwable, ? extends Throwable> mapper) {
        return new Suspend<>(() -> {
            try {
                return Effects.success(action.call());
            } catch (Throwable e) {
                return Effects.fail(mapper.apply(e));
            }
        });
    }


    /**
     * Creates a successful effect containing the given value.
     * 
     * @param value the value to wrap in an Effect
     * @param <T> the type of the value
     * @return a successful Effect containing the value
     */
    public static <T> Effect<T> success(T value) {
        return new Pure<>(value);
    }


    /**
     * Creates a failed effect containing the given error.
     * 
     * @param throwable the error to wrap in an Effect
     * @param <T> the type parameter (for type consistency)
     * @return a failed Effect containing the error
     */
    public static <T> Effect<T> fail(Throwable throwable) {
        return new Fail<>(throwable);
    }

    /**
     * Creates a suspended effect that evaluates the supplier when executed.
     * 
     * @param supplier the supplier that produces an Effect
     * @param <T> the type of the effect value
     * @return a suspended Effect
     */
    public static <T> Effect<T> suspend(Supplier<Effect<T>> supplier) {
        return new Suspend<>(supplier);
    }

    /**
     * Executes an effect and returns the result, throwing if failed.
     * 
     * @param effect the effect to execute
     * @param <T> the type of the effect value
     * @return the result value
     * @throws RuntimeException if the effect fails
     */
    public static <T> T run(Effect<T> effect)  {
        return effect.run();
    }

    /**
     * Executes an effect and returns a Result without throwing.
     * 
     * @param effect the effect to execute
     * @param <T> the type of the effect value
     * @return a Result containing the value or the error
     */
    public static <T> Result<T> runSafe(Effect<T> effect) {
        return effect.runSafe();
    }

    /**
     * Creates a deferred effect from a Callable.
     * 
     * @param callable the callable to execute
     * @param <T> the type of value produced
     * @return an Effect containing the callable's value or an error
     */
    public static <T> Effect<T> fromCallable(Callable<T> callable) {
        return new Suspend<>(() -> {
            try {
                return success(callable.call());
            } catch (Throwable e) {
                return fail(e);
            }
        });
    }

    /**
     * Creates an effect from an Optional, failing if empty.
     * 
     * @param optional the optional to convert
     * @param <T> the type of the optional value
     * @return an Effect containing the value or NoSuchElementException
     */
    public static <T> Effect<T> fromOptional(Optional<T> optional) {
        return new Suspend<>(() ->
                optional
                        .map(Effects::success)
                        .orElseGet(() ->
                                fail(new NoSuchElementException("Optional is empty"))
                        )
        );
    }

    /**
     * Creates an effect from an Optional with a custom error message.
     * 
     * @param optional the optional to convert
     * @param message the error message if empty
     * @param <T> the type of the optional value
     * @return an Effect containing the value or the custom error
     */
    public static <T> Effect<T> fromOptional(Optional<T> optional, String message) {
        return new Suspend<>(() ->
                optional
                        .map(Effects::success)
                        .orElseGet(() ->
                                Effects.fail(new NoSuchElementException(message))
                        )
        );
    }

    /**
     * Creates a deferred effect from a SupplierThrowing.
     * 
     * @param supplier the throwing supplier to execute
     * @param <T> the type of value produced
     * @return an Effect containing the supplier's value or an error
     */
    public static <T> Effect<T> fromSupplierThrowing(SupplierThrowing<T> supplier) {
        return new Suspend<>(() -> {
            try {
                return Effects.success(supplier.get());
            } catch (Throwable e) {
                return Effects.fail(e);
            }
        });
    }

    /**
     * Creates a deferred effect from a Runnable.
     * 
     * @param runnable the runnable to execute
     * @return an Effect containing void (null)
     */
    public static Effect<Void> fromRunnable(Runnable runnable) {
        return new Suspend<>(() -> {
            try {
                runnable.run();
                return Effects.success(null);
            } catch (Throwable e) {
                return Effects.fail(e);
            }
        });
    }


    /**
     * Creates an effect from a CompletableFuture.
     * 
     * @param future the future to convert
     * @param <T> the type of the future value
     * @return an Effect containing the future's value or an error
     */
    public static <T> Effect<T> fromFuture(CompletableFuture<T> future) {
        return new Suspend<>(() -> {
            try {
                return Effects.success(future.join());
            } catch (Throwable e) {
                return Effects.fail(e);
            }
        });
    }

    /**
     * Races two effects, returning the first one to complete.
     * The loser is cancelled when the winner completes.
     * 
     * @param a the first effect
     * @param b the second effect
     * @param <T> the type of the effect values
     * @return an Effect containing the first result to complete
     */
    public static <T> Effect<T> race(Effect<T> a, Effect<T> b) {
        return of(() -> {
            Fiber<T> fiberA = a.fork().run();
            Fiber<T> fiberB = b.fork().run();

            try {
                // Wait for either fiber to complete
                CompletableFuture<T> first =
                        CompletableFuture.anyOf(
                                fiberA.join().runFuture(),
                                fiberB.join().runFuture()
                        ).thenApply(obj -> (T) obj);

                return first.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                // Cancel the loser
                fiberA.cancel().run();
                fiberB.cancel().run();
            }
        });
    }

    /**
     * Creates a resource acquisition effect with automatic cleanup (try-with-resources pattern).
     * 
     * @param acquire effect to acquire the resource
     * @param use function that uses the resource
     * @param release function to release the resource
     * @param <R> the resource type
     * @param <T> the result type
     * @return an Effect with automatic resource cleanup
     */
    public static <R, T> Effect<T> using(
            Effect<R> acquire,
            Function<R, Effect<T>> use,
            Function<R, Effect<Void>> release
    ) {
        return acquire.flatMap(resource ->
                use.apply(resource)
                        .tapError(e -> release.apply(resource).run())
                        .flatMap(result -> release.apply(resource).map(v -> result))
        );
    }

    /**
     * Conditional effect - evaluates either onTrue or onFalse based on condition.
     * 
     * @param condition the boolean condition effect
     * @param onTrue effect to run if condition is true
     * @param onFalse effect to run if condition is false
     * @param <T> the type of the effect values
     * @return the selected effect
     */
    public static <T> Effect<T> iff(Effect<Boolean> condition, Effect<T> onTrue, Effect<T> onFalse) {
        return of(() -> condition.run() ? onTrue.run() : onFalse.run());
    }

    /**
     * Conditionally executes an effect, returning Optional.empty if condition is false.
     * 
     * @param condition whether to execute the effect
     * @param effect the effect to execute if true
     * @param <T> the type of the effect value
     * @return an Effect containing Optional.of(value) or Optional.empty
     */
    public static <T> Effect<Optional<T>> when(boolean condition, Effect<T> effect) {
        return of(() -> condition ? Optional.of(effect.run()) : Optional.empty());
    }

    /**
     * Conditionally executes an effect based on another effect's boolean result.
     * 
     * @param condition the condition effect (must return true/false)
     * @param effect the effect to execute if condition is true
     * @param <T> the type of the effect value
     * @return an Effect containing Optional.of(value) or Optional.empty
     */
    public static <T> Effect<Optional<T>> whenEffect(Effect<Boolean> condition, Effect<T> effect) {
        return of(() -> condition.run() ? Optional.of(effect.run()) : Optional.empty());
    }

    /**
     * Executes a list of effects in parallel, returning all results.
     * 
     * @param effects the list of effects to run in parallel
     * @param <T> the type of the effect values
     * @return an Effect containing a list of all results
     */
    public static <T> Effect<List<T>> parallel(List<Effect<T>> effects) {
        return of(() -> {
            List<Fiber<T>> fibers = effects.stream()
                    .map(effect -> effect.fork().run())
                    .toList();


            return fibers.stream()
                    .map(fiber -> fiber.join().run())
                    .toList();
        });
    }

    /**
     * Returns the first effect that succeeds, trying each in order.
     * 
     * @param effects the effects to try
     * @param <T> the type of the effect values
     * @return the first successful effect, or all fail if none succeed
     */
    @SafeVarargs
    public static <T> Effect<T> firstSuccessOf(Effect<T>... effects) {
        Effect<T> acc = Effects.fail(new RuntimeException("All effects failed"));
        for (Effect<T> e : effects) {
            acc = acc.recoverWith(err -> e);
        }
        return acc;
    }

    /**
     * Creates a cached version of a function that returns Effects.
     * Results are memoized based on the input parameter.
     * 
     * @param f the function to cache
     * @param <A> the input type
     * @param <B> the output type
     * @return a cached version of the function
     */
    public static <A, B> Function<A, Effect<B>> cachedFunction(Function<A, Effect<B>> f) {
        class Cache {
            final java.util.Map<A, B> map = new java.util.HashMap<>();
            synchronized B compute(A key) {
                if (!map.containsKey(key)) {
                    Result<B> result = EffectRuntime.run(f.apply(key));
                    if (result.isFailure()) {
                        throw new RuntimeException(result.getThrowable());
                    }
                    map.put(key, result.get());
                }
                return map.get(key);
            }
        }
        final Cache cache = new Cache();
        return arg -> Effects.of(() -> cache.compute(arg));
    }

}
