package io.github.fike110.jeffect.core;

import io.github.fike110.jeffect.Effects;

public interface Fiber<T> {

    Effect<T> join();

    Effect<Void> cancel();

}