package io.github.fike110.jeffect.core;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.fike110.jeffect.Effects;
import io.github.fike110.jeffect.data.Pair;
import io.github.fike110.jeffect.data.Result;

/**
 * An Effect represents a deferred computation that may succeed or fail.
 * 
 * <p>Effects are similar to Either&lt;Throwable, T&gt; but with monadic operations
 * like map, flatMap, recover, and convenient execution methods.</p>
 * 
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * Effect<String> effect = Effects.of(() -> "Hello");
 * String result = effect.run(); // returns "Hello"
 * }</pre>
 * 
 * <h2>Error Handling</h2>
 * <pre>{@code
 * Effect<User> effect = Effects.of(() -> userRepository.findById(id))
 *     .recover(e -> User.anonymous());
 * }</pre>
 * 
 * <h2>Sequencing</h2>
 * <pre>{@code
 * Effect<User> userEffect = Effects.of(() -> saveUser(req));
 * Effect<Email> emailEffect = Effects.of(() -> sendWelcomeEmail(userEffect.run()));
 * }</pre>
 * 
 * @param <T> the type of value produced by the effect
 */
public sealed interface Effect<T> permits Pure,Fail,Suspend,FlatMap,Recover{

    /**
     * Creates a suspended effect that evaluates the supplier when executed.
     * 
     * @param supplier the supplier that produces the effect
     * @param <T> the type of the effect value
     * @return a new Effect
     */
    static <T> Effect<T> suspend(Supplier<Effect<T>> supplier) {
        return new Suspend<>(supplier);
    }


    /**
     * Transforms the effect's value using the provided function.
     * 
     * <p>If the effect fails, the mapper is not applied.</p>
     * 
     * <pre>{@code
     * Effect<String> effect = Effects.success(5)
     *     .map(i -> "Number: " + i);  // Effect<String> containing "Number: 5"
     * }</pre>
     * 
     * @param mapper the function to transform the value
     * @param <R> the new result type
     * @return a new Effect with the transformed value
     */
    <R> Effect<R> flatMap(Function<T, Effect<R>> mapper);

    /**
     * Maps the effect's value to another value.
     * 
     * <p>Shortcut for {@code flatMap(v -> Effects.success(mapper.apply(v)))}</p>
     * 
     * <pre>{@code
     * Effect<Integer> num = Effects.success(10);
     * Effect<String> str = num.map(n -> "Value: " + n);
     * }</pre>
     * 
     * @param mapper the transformation function
     * @param <R> the new type
     * @return a new Effect with the mapped value
     */
    default <R> Effect<R> map(Function<T, R> mapper) {
        return this.flatMap(v -> Effects.success(mapper.apply(v)));
    }

    /**
     * Executes the effect and returns the result or throws if failed.
     * 
     * <pre>{@code
     * try {
     *     String result = effect.run();
     * } catch (RuntimeException e) {
     *     // handle error
     * }
     * }</pre>
     * 
     * @return the result value
     * @throws RuntimeException if the effect fails
     */
    default T run() {
        try {
            Result<T> result= EffectRuntime.run(this);
            if (result.isFailure()) throw result.getThrowable();
            return result.get();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delays execution of this effect by the specified duration.
     * 
     * <pre>{@code
     * Effect<String> delayed = Effects.success("hello")
     *     .delay(Duration.ofSeconds(2));
     * }</pre>
     * 
     * @param duration the delay duration
     * @return a new Effect with the delay applied
     */
    default Effect<T> delay(Duration duration) {
        return new Suspend<>(() -> {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                return Effects.fail(e);
            }
            return Effects.success(null);
        }).flatMap(v -> this);
    }

    /**
     * Sleeps for the specified duration then returns the result of this effect.
     * 
     * @param duration sleep duration
     * @return the effect result after sleeping
     */
    default Effect<T> sleep(Duration duration) {
        return Effects.of(() -> {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return this.run();
        });
    }

    /**
     * Logs the value to stdout and returns the same value.
     * Useful for debugging effect chains.
     * 
     * @return the effect with logging side-effect
     */
    default Effect<T> log() {
        return map(v -> {
            System.out.println(v);
            return v;
        });
    }

    default Effect<T> logDebug() {
        return map(v -> {
            System.out.println("[DEBUG] " + v);
            return v;
        });
    }

    default Effect<T> logInfo() {
        return map(v -> {
            System.out.println("[INFO] " + v);
            return v;
        });
    }

    default Effect<T> logWarn() {
        return map(v -> {
            System.out.println("[WARN] " + v);
            return v;
        });
    }

    default Effect<T> logError() {
        return map(v -> {
            System.err.println("[ERROR] " + v);
            return v;
        });
    }

    default Effect<T> logFatal() {
        return map(v -> {
            System.err.println("[FATAL] " + v);
            return v;
        });
    }

    default <R> Effect<R> match(Function<T, R> onSuccess, Function<Throwable, R> onFailure) {
        return Effects.of(() -> {
            try {
                T value = this.run();
                return onSuccess.apply(value);
            } catch (Throwable e) {
                return onFailure.apply(e);
            }
        });
    }

    default Effect<Void> ignore() {
        return Effects.of(() -> {
            try {
                this.run();
            } catch (Throwable ignored) {}
            return null;
        });
    }

    default Effect<T> defer(Runnable cleanup) {
        return this.map(v -> {
            cleanup.run();
            return v;
        }).recoverWith(err -> {
            cleanup.run();
            return Effects.fail(err);
        });
    }

    default Effect<T> once() {
        class Holder {
            T value;
            boolean computed = false;
            synchronized T compute() {
                if (!computed) {
                    Result<T> result = EffectRuntime.run(Effect.this);
                    if (result.isFailure()) {
                        throw new RuntimeException(result.getThrowable());
                    }
                    value = result.get();
                    computed = true;
                }
                return value;
            }
        }
        final Holder holder = new Holder();
        return Effects.of(() -> holder.compute());
    }

    default Effect<T> memoize() {
        return once();
    }

    default Effect<T> cached() {
        return once();
    }

    default Effect<T> cachedWithTTL(Duration ttl) {
        class TTLHolder {
            T value;
            long expiresAt = 0;
            synchronized T compute() {
                long now = System.currentTimeMillis();
                if (expiresAt == 0 || now > expiresAt) {
                    Result<T> result = EffectRuntime.run(Effect.this);
                    if (result.isFailure()) {
                        throw new RuntimeException(result.getThrowable());
                    }
                    value = result.get();
                    expiresAt = System.currentTimeMillis() + ttl.toMillis();
                }
                return value;
            }
        }
        final TTLHolder holder = new TTLHolder();
        return Effects.of(() -> holder.compute());
    }

    default Pair<Effect<T>, Effect<Void>> cachedWithInvalidate(Duration ttl) {
        class TTLHolder {
            T value;
            long expiresAt = 0;
            synchronized T compute() {
                long now = System.currentTimeMillis();
                if (expiresAt == 0 || now > expiresAt) {
                    Result<T> result = EffectRuntime.run(Effect.this);
                    if (result.isFailure()) {
                        throw new RuntimeException(result.getThrowable());
                    }
                    value = result.get();
                    expiresAt = System.currentTimeMillis() + ttl.toMillis();
                }
                return value;
            }
            synchronized void invalidate() {
                expiresAt = 0;
            }
        }
        final TTLHolder holder = new TTLHolder();
        Effect<T> cached = Effects.of(() -> holder.compute());
        Effect<Void> invalidate = Effects.of(() -> {
            holder.invalidate();
            return null;
        });
        return new Pair<>(cached, invalidate);
    }


    default Effect<T> timeout(Duration duration) {
        return Effects.of(() -> {
            // Fork the effect
            Fiber<T> fiber = this.fork().run();

            // Create a timeout future
            CompletableFuture<T> result = new CompletableFuture<>();
            CompletableFuture<Void> timeout = CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(duration.toMillis());
                    result.completeExceptionally(new TimeoutException("Effect timed out after " + duration));
                    fiber.cancel().run(); // cancel the running effect
                } catch (InterruptedException e) {
                    result.completeExceptionally(e);
                }
            });

            fiber.join().runAsync();

            try {
                return result.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Executes the effect and returns a Result without throwing exceptions.
     * 
     * <pre>{@code
     * Result<User> result = effect.runSafe();
     * if (result.isSuccess()) {
     *     User user = result.get();
     * } else {
     *     Throwable error = result.getThrowable();
     * }
     * }</pre>
     * 
     * @return a Result containing the value or the error
     */
    default Result<T> runSafe()  {
        return EffectRuntime.run(this);
    }

    /**
     * Executes the effect asynchronously using the default executor.
     * Fire-and-forget - errors are silently ignored.
     */
    default void runAsync() {
        CompletableFuture.runAsync(() -> {
            EffectRuntime.run(this);
        });
    }

    default void runAsync(Executor executor) {
        executor.execute(() -> EffectRuntime.run(this));
    }

    /**
     * Executes the effect asynchronously and returns a CompletableFuture.
     * 
     * @return a CompletableFuture containing the result
     */
    default CompletableFuture<T> runFuture() {
        return CompletableFuture.supplyAsync(() -> {
            Result<T> result = EffectRuntime.run(this);
            if (result.isFailure()) {
                throw new RuntimeException(result.getThrowable());
            }
            return result.get();
        });
    }

    default CompletableFuture<T> runFuture(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Result<T> result = EffectRuntime.run(this);
            if (result.isFailure()) {
                throw new RuntimeException(result.getThrowable());
            }
            return result.get();
        }, executor);
    }

    default CompletableFuture<Result<T>> runSafeFuture() {
        return CompletableFuture.supplyAsync(() -> {
            return EffectRuntime.run(this);
        });
    }

    default CompletableFuture<Result<T>> runSafeFuture(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            return EffectRuntime.run(this);
        }, executor);
    }

    default <R> Effect<R> then(Effect<R> next) {
        return flatMap(v -> next);
    }

    default <U> Effect<Pair<T,U>> zip(Effect<U> other) {
        return zipWith(other, Pair::new);
    }

    default <U,R> Effect<R> zipWith(
            Effect<U> other,
            BiFunction<T,U,R> combiner) {

        return this.flatMap(a ->
                other.map(b -> combiner.apply(a,b))
        );
    }

    /**
     * Recovers from a failure by applying a handler function to the error.
     * 
     * <pre>{@code
     * Effect<User> effect = Effects.of(() -> findUser(id))
     *     .recover(e -> User.anonymous());  // returns default user on failure
     * }</pre>
     * 
     * @param handler the function to handle the error and produce a recovery value
     * @return a new Effect that recovers from failures
     */
    default Effect<T> recover(Function<Throwable, T> handler) {
        return recoverWith(err -> Effects.success(handler.apply(err)));
    }

    /**
     * Recovers from a failure by applying a handler function that returns an Effect.
     * 
     * <pre>{@code
     * Effect<User> effect = Effects.of(() -> findUser(id))
     *     .recoverWith(e -> Effects.success(User.anonymous()));
     * }</pre>
     * 
     * @param handler the function to handle the error and produce a new Effect
     * @return a new Effect that recovers from failures
     */
    default Effect<T> recoverWith(
            Function<Throwable, Effect<T>> handler
    ) {
        return new Recover<>(this, handler);
    }

    /**
     * Returns this effect if successful, otherwise returns the fallback effect.
     * 
     * @param fallback supplier of the fallback effect
     * @return this effect or the fallback
     */
    default Effect<T> orElse(Supplier<Effect<T>> fallback) {
        return this.recoverWith(err -> fallback.get());
    }

    default T orElseGet(Supplier<T> defaultValue) {
        return this.recoverWith(err -> Effects.success(defaultValue.get())).run();
    }

    default Effect<T> onError(Consumer<Throwable> action) {
        return recoverWith(err -> {
            action.accept(err);
            return Effects.fail(err);
        });
    }

    default Effect<T> retry(int retries) {
        return recoverWith(err ->
                retries > 0
                        ? new Suspend<>(() -> this.retry(retries - 1))
                        : Effects.fail(err)
        );
    }

    default Effect<Void> repeat(int times) {
        if (times <= 0) {
            return Effects.success(null);
        }

        return this.flatMap(v ->
            new Suspend<>(() -> this.repeat(times - 1))
        );
    }

    default Effect<T> repeatWhile(Function<T, Boolean> predicate) {
        return this.flatMap(value -> {
            if (predicate.apply(value)) {
                return new Suspend<>(() -> this.repeatWhile(predicate));
            }
            return Effects.success(value);
        });
    }

    default Effect<T> repeatUntil(Function<T, Boolean> predicate) {
        return this.flatMap(value -> {
            if (predicate.apply(value)) {
                return Effects.success(value);
            }
            return new Suspend<>(() -> this.repeatUntil(predicate));
        });
    }

    default Effect<T> tap(Consumer<T> action) {
        return map(v -> {
            action.accept(v);
            return v;
        });
    }

    default Effect<T> tapError(Consumer<Throwable> action) {
        return recoverWith(err -> {
            action.accept(err);
            return Effects.fail(err);
        });
    }
    default Effect<T> peek(Consumer<T> action) {
        return Effects.of(() -> {
            T result = this.run(); // execute the effect
            action.accept(result);     // run side effect
            return result;             // return original value unchanged
        });
    }

    default Fiber<T> schedule(Duration initialDelay, Duration interval) {
        return schedule(Effects.scheduler, initialDelay, interval);
    }

    default Fiber<T> schedule(ScheduledExecutorService scheduler, Duration initialDelay, Duration interval) {

        CompletableFuture<T> future = new CompletableFuture<>();

        Runnable task = () -> {
            try {
                T result = this.run();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        };

        var scheduled = scheduler.scheduleAtFixedRate(
                task,
                initialDelay.toMillis(),
                interval.toMillis(),
                TimeUnit.MILLISECONDS
        );

        return new Fiber<>() {
            @Override
            public Effect<T> join() {
                return Effects.of(()-> {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            public Effect<Void> cancel() {
                return Effects.of(() -> {
                    scheduled.cancel(true);
                    return null;
                });
            }
        };
    }


    public default Effect<Fiber<T>> fork() {
        Effect<T> self = this;
        return Effects.of(() -> {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return EffectRuntime.run(self).get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            return new Fiber<T>() {

                @Override
                public Effect<T> join() {
                    return Effects.of(() -> {
                        try {
                            return future.get();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (java.util.concurrent.ExecutionException e) {
                            throw new RuntimeException(e.getCause());
                        }
                    });
                }

                @Override
                public Effect<Void> cancel() {
                    return Effects.of(() -> {
                        future.cancel(true);
                        return null;
                    });
                }
            };
        });
    }

}
