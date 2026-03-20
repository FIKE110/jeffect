package io.github.fike110.jeffect.state;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class FlatMappedState<T, U> implements State<U> {
    
    private final State<T> source;
    private final Function<T, State<U>> mapper;
    private final Set<Consumer<U>> listeners = new HashSet<>();
    
    private State<U> currentState;
    private U currentValue;
    
    public FlatMappedState(State<T> source, Function<T, State<U>> mapper) {
        this.source = source;
        this.mapper = mapper;
        this.currentState = mapper.apply(source.get());
        this.currentValue = currentState.get();
        
        currentState.subscribe(this::onInnerStateChange);
        source.subscribe(newValue -> onSourceChange(newValue));
    }
    
    private void onSourceChange(T newValue) {
        State<U> newState = mapper.apply(newValue);
        U oldValue = currentValue;
        currentState.unsubscribe(this::onInnerStateChange);
        currentState = newState;
        currentValue = currentState.get();
        currentState.subscribe(this::onInnerStateChange);
        
        if (!equals(oldValue, currentValue)) {
            notifyListeners(currentValue);
        }
    }
    
    private void onInnerStateChange(U newValue) {
        U oldValue = currentValue;
        currentValue = newValue;
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
        currentState.set(value);
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
        return "FlatMappedState(" + currentValue + ")";
    }
}
