package io.github.fike110.jeffect.state;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface State<T> {
    
    T get();
    
    void set(T value);
    
    void subscribe(Consumer<T> listener);
    
    void unsubscribe(Consumer<T> listener);
    
    default <U> State<U> map(Function<T, U> mapper) {
        return new DerivedState<>(this, mapper);
    }
    
    default <U> State<U> flatMap(Function<T, State<U>> mapper) {
        return new FlatMappedState<>(this, mapper);
    }
    
    default State<T> filter(Predicate<T> predicate, T defaultValue) {
        return new FilteredState<>(this, predicate, defaultValue);
    }
    
    default int subscriberCount() {
        return 0;
    }
    
    static <T> State<T> of(T initialValue) {
        return new SimpleState<>(initialValue);
    }
    
    static <T> State<T> atomically(T initialValue) {
        return new AtomicState<>(initialValue);
    }
}
