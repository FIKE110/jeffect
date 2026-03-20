package io.github.fike110.jeffect.state;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FilteredState<T> implements State<T> {
    
    private final State<T> source;
    private final Predicate<T> predicate;
    private final T defaultValue;
    private final Set<Consumer<T>> listeners = new HashSet<>();
    private T currentValue;
    
    public FilteredState(State<T> source, Predicate<T> predicate, T defaultValue) {
        this.source = source;
        this.predicate = predicate;
        this.defaultValue = defaultValue;
        this.currentValue = predicate.test(source.get()) ? source.get() : defaultValue;
        
        source.subscribe(this::onSourceChange);
    }
    
    private void onSourceChange(T newValue) {
        T oldValue = currentValue;
        currentValue = predicate.test(newValue) ? newValue : defaultValue;
        if (!equals(oldValue, currentValue)) {
            notifyListeners(currentValue);
        }
    }
    
    @Override
    public T get() {
        return currentValue;
    }
    
    @Override
    public void set(T value) {
        if (predicate.test(value)) {
            source.set(value);
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
    
    private void notifyListeners(T newValue) {
        for (Consumer<T> listener : listeners) {
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
        return "FilteredState(" + currentValue + ")";
    }
}
