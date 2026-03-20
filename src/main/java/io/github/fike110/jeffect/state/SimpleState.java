package io.github.fike110.jeffect.state;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SimpleState<T> implements State<T> {
    
    private T value;
    private final Set<Consumer<T>> listeners = new HashSet<>();
    
    public SimpleState(T initialValue) {
        this.value = initialValue;
    }
    
    @Override
    public T get() {
        return value;
    }
    
    @Override
    public void set(T newValue) {
        boolean changed = (value == null) ? (newValue != null) : !value.equals(newValue);
        this.value = newValue;
        if (changed) {
            notifyListeners();
        }
    }
    
    @Override
    public void subscribe(Consumer<T> listener) {
        listeners.add(listener);
    }
    
    @Override
    public void unsubscribe(Consumer<T> listener) {
        listeners.remove(listener);
    }
    
    @Override
    public int subscriberCount() {
        return listeners.size();
    }
    
    private void notifyListeners() {
        for (Consumer<T> listener : listeners) {
            try {
                listener.accept(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public String toString() {
        return "SimpleState(" + value + ")";
    }
}
