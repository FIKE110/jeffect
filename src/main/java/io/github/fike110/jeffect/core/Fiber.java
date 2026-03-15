package io.github.fike110.jeffect.core;

import io.github.fike110.jeffect.Effects;

/**
 * Represents a lightweight thread-like execution context for Effects.
 * 
 * <p>Fibers are created via {@link Effect#fork()} and allow for
 * concurrent execution of effects. They can be joined to wait for
 * completion or cancelled to interrupt execution.</p>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * // Fork an effect to run concurrently
 * Effect<Fiber<String>> forked = Effects.success("hello").fork();
 * 
 * // Get the fiber and join it
 * String result = forked.run().join().run();
 * 
 * // Or cancel
 * forked.run().cancel().run();
 * }</pre>
 * 
 * <h2>Comparison with Threads</h2>
 * <ul>
 *   <li>Fibers are much lighter weight than threads</li>
 *   <li>Thousands of fibers can run on a single thread</li>
 *   <li>Fibers can be cancelled safely</li>
 * </ul>
 * 
 * @param <T> the type of value produced by the fiber
 */
public interface Fiber<T> {

    /**
     * Waits for the fiber to complete and returns its result.
     * 
     * @return an Effect that completes with the fiber's value
     */
    Effect<T> join();

    /**
     * Cancels the fiber's execution.
     * 
     * @return an Effect that completes when the fiber is cancelled
     */
    Effect<Void> cancel();

}