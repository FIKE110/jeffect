package io.github.fike110.jeffect.state;

import java.util.function.Consumer;

public class Watcher {
    
    private Watcher() {}
    
    public static <T> Runnable watch(State<T> state, Runnable effect) {
        state.subscribe(ignored -> effect.run());
        return effect;
    }
    
    public static <T> Runnable watch(State<T> state, Consumer<T> effect) {
        effect.accept(state.get());
        state.subscribe(effect);
        return () -> effect.accept(state.get());
    }
    
    public static <T, U> Runnable watch(State<T> s1, State<U> s2, Runnable effect) {
        s1.subscribe(ignored -> effect.run());
        s2.subscribe(ignored -> effect.run());
        return effect;
    }
    
    public static <T> DerivedState<T, T> derive(State<T> state) {
        return new DerivedState<>(state, java.util.function.Function.identity());
    }
}
