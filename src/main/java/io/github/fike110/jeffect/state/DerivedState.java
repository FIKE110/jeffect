package io.github.fike110.jeffect.state;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class DerivedState<T, U> implements State<U> {
    
    private final State<T> source;
    private final Function<T, U> mapper;
    private final Set<Consumer<U>> listeners = new HashSet<>();
    private U currentValue;
    
    public DerivedState(State<T> source, Function<T, U> mapper) {
        this.source = source;
        this.mapper = mapper;
        this.currentValue = mapper.apply(source.get());
        
        source.subscribe(this::onSourceChange);
    }
    
    private void onSourceChange(T newValue) {
        U oldValue = currentValue;
        currentValue = mapper.apply(newValue);
        if (!equals(oldValue, currentValue)) {
            notifyListeners(currentValue);
        }
    }
    
    @Override
    public U get() {
        return currentValue;
    }
    
    @Override
    public void set(U value) {
        throw new UnsupportedOperationException(
            "Cannot set a derived state directly. Update the source state instead."
        );
    }
    
    @Override
    public void subscribe(Consumer<U> listener) {
        listeners.add(listener);
    }
    
    @Override
    public void unsubscribe(Consumer<U> listener) {
        listeners.remove(listener);
    }
    
    @Override
    public int subscriberCount() {
        return listeners.size();
    }
    
    private void notifyListeners(U newValue) {
        for (Consumer<U> listener : listeners) {
            try {
                listener.accept(newValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
    
    @Override
    public String toString() {
        return "DerivedState(" + currentValue + ")";
    }
}
