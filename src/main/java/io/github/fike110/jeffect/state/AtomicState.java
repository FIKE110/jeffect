package io.github.fike110.jeffect.state;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class AtomicState<T> implements State<T> {
    
    private final AtomicReference<T> value;
    private final Set<Consumer<T>> listeners = new HashSet<>();
    
    public AtomicState(T initialValue) {
        this.value = new AtomicReference<>(initialValue);
    }
    
    @Override
    public T get() {
        return value.get();
    }
    
    @Override
    public void set(T newValue) {
        T oldValue = value.getAndSet(newValue);
        if (!equals(oldValue, newValue)) {
            notifyListeners(newValue);
        }
    }
    
    public boolean compareAndSet(T expected, T newValue) {
        boolean updated = value.compareAndSet(expected, newValue);
        if (updated) {
            notifyListeners(newValue);
        }
        return updated;
    }
    
    public T updateAndGet(UnaryOperator<T> updater) {
        T current;
        T newValue;
        do {
            current = value.get();
            newValue = updater.apply(current);
        } while (!value.compareAndSet(current, newValue));
        notifyListeners(newValue);
        return newValue;
    }
    
    public T getAndUpdate(UnaryOperator<T> updater) {
        T current;
        T newValue;
        do {
            current = value.get();
            newValue = updater.apply(current);
        } while (!value.compareAndSet(current, newValue));
        notifyListeners(newValue);
        return current;
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
        return "AtomicState(" + value.get() + ")";
    }
}
