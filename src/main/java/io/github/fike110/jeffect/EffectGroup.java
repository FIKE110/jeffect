package io.github.fike110.jeffect;

import java.util.ArrayList;
import java.util.List;

import io.github.fike110.jeffect.core.Effect;
import io.github.fike110.jeffect.core.Fiber;

public class EffectGroup implements AutoCloseable {
    private final List<Fiber<?>> fibers = new ArrayList<>();

    public <T> Fiber<T> add(Effect<T> effect) {
        Fiber<T> fiber = effect.fork().run();
        fibers.add(fiber);
        return fiber;
    }

    public Effect<Void> disposeAll() {
        return Effects.of(() -> {
            for (Fiber<?> fiber : fibers) {
                fiber.cancel().run();
            }
            fibers.clear();
            return null;
        });
    }

    public Effect<Void> joinAll() {
        return Effects.of(() -> {
            for (Fiber<?> fiber : fibers) {
                fiber.join().run();
            }
            return null;
        });
    }

    public int size() {
        return fibers.size();
    }

    public void clear() {
        fibers.clear();
    }

    @Override
    public void close() {
        disposeAll().run();
    }
}