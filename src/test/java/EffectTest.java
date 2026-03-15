import org.junit.jupiter.api.Test;

import io.github.fike110.jeffect.Effects;
import io.github.fike110.jeffect.core.Effect;
import io.github.fike110.jeffect.data.Result;

import java.io.InvalidClassException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class EffectTest {

    @Test
    void testSuccessEffect() {
        Effect<Integer> effect = Effects.success(10);
        assertEquals(10, effect.run());
    }

    @Test
    void testFailEffect() {
        Effect<Integer> effect = Effects.fail(new InvalidClassException("Invalid class"));
        Result<Integer> result = effect.runSafe();
        assertTrue(result.isFailure());
        assertEquals("Invalid class", result.getThrowable().getMessage());
    }

    @Test
    void testOfSupplier() {
        Effect<String> effect = Effects.of(() -> "hello");
        assertEquals("hello", effect.run());
    }

    @Test
    void testOfSupplierThrows() {
        Effect<String> effect = Effects.of(() -> {
            throw new RuntimeException("error");
        });
        Result<String> result = effect.runSafe();
        assertTrue(result.isFailure());
        assertEquals("error", result.getThrowable().getMessage());
    }

    @Test
    void testOfRunnable() {
        AtomicInteger counter = new AtomicInteger(0);
        Effect<Integer> effect = Effects.of(() -> {
            counter.incrementAndGet();
            return 1;
        });
        effect.run();
        assertEquals(1, counter.get());
    }

    @Test
    void testMap() {
        Effect<Integer> effect = Effects.success(5)
            .map(i -> i * 2);
        assertEquals(10, effect.run());
    }

    @Test
    void testMapOnFail() {
        Effect<Integer> effect = Effects.<Integer>fail(new RuntimeException("error"))
            .map(i -> i * 2);
        Result<Integer> result = effect.runSafe();
        assertTrue(result.isFailure());
    }

    @Test
    void testFlatMap() {
        Effect<String> effect = Effects.success(5)
            .flatMap(i -> Effects.success("Number: " + i));
        assertEquals("Number: 5", effect.run());
    }

    @Test
    void testFlatMapWithFail() {
        Effect<String> effect = Effects.<Integer>fail(new RuntimeException("error"))
            .flatMap(i -> Effects.success("Number: " + i));
        Result<String> result = effect.runSafe();
        assertTrue(result.isFailure());
    }

    @Test
    void testRecover() {
        Effect<Integer> effect = Effects.<Integer>fail(new RuntimeException("error"))
            .recover(e -> 42);
        assertEquals(42, effect.run());
    }

    @Test
    void testRecoverWith() {
        Effect<Integer> effect = Effects.<Integer>fail(new RuntimeException("error"))
            .recoverWith(e -> Effects.success(42));
        assertEquals(42, effect.run());
    }

    @Test
    void testOrElse() {
        Effect<Integer> effect = Effects.<Integer>fail(new RuntimeException("error"))
            .orElse(() -> Effects.success(100));
        assertEquals(100, effect.run());
    }

    @Test
    void testOrElseGet() {
        Integer result = Effects.<Integer>fail(new RuntimeException("error"))
            .orElseGet(() -> 100);
        assertEquals(100, result);
    }

    @Test
    void testOnError() {
        AtomicInteger counter = new AtomicInteger(0);
        Effect<Integer> effect = Effects.<Integer>fail(new RuntimeException("error"))
            .onError(e -> counter.incrementAndGet());
        effect.runSafe();
        assertEquals(1, counter.get());
    }

    @Test
    void testSequence() {
        List<Effect<Integer>> effects = List.of(
            Effects.success(1),
            Effects.success(2),
            Effects.success(3)
        );
        Effect<List<Integer>> result = Effects.sequence(effects);
        assertEquals(List.of(1, 2, 3), result.run());
    }

    @Test
    void testSequenceWithFail() {
        List<Effect<Integer>> effects = List.of(
            Effects.success(1),
            Effects.<Integer>fail(new RuntimeException("error")),
            Effects.success(3)
        );
        Effect<List<Integer>> result = Effects.sequence(effects);
        Result<List<Integer>> r = result.runSafe();
        assertTrue(r.isFailure());
    }

    @Test
    void testTraverse() {
        List<Integer> numbers = List.of(1, 2, 3);
        Effect<List<Integer>> result = Effects.traverse(numbers, i -> Effects.success(i * 2));
        assertEquals(List.of(2, 4, 6), result.run());
    }

    @Test
    void testParallel() {
        List<Effect<Integer>> effects = List.of(
            Effects.success(1),
            Effects.success(2),
            Effects.success(3)
        );
        Effect<List<Integer>> result = Effects.parallel(effects);
        List<Integer> list = result.run();
        assertEquals(3, list.size());
        assertTrue(list.contains(1));
        assertTrue(list.contains(2));
        assertTrue(list.contains(3));
    }

    @Test
    void testFromOptional() {
        Effect<String> effect = Effects.fromOptional(Optional.of("hello"));
        assertEquals("hello", effect.run());
    }

    @Test
    void testFromOptionalEmpty() {
        Effect<String> effect = Effects.fromOptional(Optional.empty());
        Result<String> result = effect.runSafe();
        assertTrue(result.isFailure());
    }

    @Test
    void testFromOptionalWithMessage() {
        Effect<String> effect = Effects.fromOptional(Optional.empty(), "Custom error");
        Result<String> result = effect.runSafe();
        assertTrue(result.isFailure());
        assertEquals("Custom error", result.getThrowable().getMessage());
    }

    @Test
    void testFromFuture() {
        CompletableFuture<String> future = CompletableFuture.completedFuture("hello");
        Effect<String> effect = Effects.fromFuture(future);
        assertEquals("hello", effect.run());
    }

    @Test
    void testFromFutureWithException() {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("future error"));
        Effect<String> effect = Effects.fromFuture(future);
        Result<String> result = effect.runSafe();
        assertTrue(result.isFailure());
    }

    @Test
    void testTryCatch() {
        Effect<Integer> effect = Effects.tryCatch(
            () -> 42,
            e -> new RuntimeException("mapped")
        );
        assertEquals(42, effect.run());
    }

    @Test
    void testTryCatchMapsException() {
        Effect<Integer> effect = Effects.tryCatch(
            () -> { throw new IllegalArgumentException("original"); },
            e -> new RuntimeException("mapped")
        );
        Result<Integer> result = effect.runSafe();
        assertTrue(result.isFailure());
        assertEquals("mapped", result.getThrowable().getMessage());
    }

    @Test
    void testWhenTrue() {
        Effect<Optional<Integer>> effect = Effects.when(true, Effects.success(42));
        assertEquals(Optional.of(42), effect.run());
    }

    @Test
    void testWhenFalse() {
        Effect<Optional<Integer>> effect = Effects.when(false, Effects.success(42));
        assertEquals(Optional.empty(), effect.run());
    }

    @Test
    void testFirstSuccessOf() {
        Effect<Integer> effect = Effects.firstSuccessOf(
            Effects.<Integer>fail(new RuntimeException("error1")),
            Effects.success(42),
            Effects.success(100)
        );
        assertEquals(42, effect.run());
    }

    @Test
    void testIffTrue() {
        Effect<String> effect = Effects.iff(
            Effects.success(true),
            Effects.success("yes"),
            Effects.success("no")
        );
        assertEquals("yes", effect.run());
    }

    @Test
    void testIffFalse() {
        Effect<String> effect = Effects.iff(
            Effects.success(false),
            Effects.success("yes"),
            Effects.success("no")
        );
        assertEquals("no", effect.run());
    }

    @Test
    void testCachedFunction() {
        AtomicInteger callCount = new AtomicInteger(0);
        Function<Integer, Effect<String>> f = i -> {
            callCount.incrementAndGet();
            return Effects.success("value: " + i);
        };

        Function<Integer, Effect<String>> cached = Effects.cachedFunction(f);

        assertEquals("value: 5", cached.apply(5).run());
        assertEquals("value: 5", cached.apply(5).run());
        assertEquals("value: 5", cached.apply(5).run());
        assertEquals(1, callCount.get());
    }

    @Test
    void testIgnore() {
        Effect<Integer> successEffect = Effects.success(42);
        Effect<Void> effect = successEffect.ignore();
        assertNull(effect.run());
    }

    @Test
    void testMatch() {
        Effect<String> success = Effects.success(42).match(
            i -> "Number: " + i,
            e -> "Error: " + e.getMessage()
        );
        assertEquals("Number: 42", success.run());

        Effect<String> fail = Effects.<Integer>fail(new RuntimeException("error")).match(
            i -> "Number: " + i,
            e -> "Error: " + e.getMessage()
        );
        assertTrue(fail.run().contains("error"));
    }

    @Test
    void testSuspend() {
        Effect<Integer> effect = Effects.suspend(() -> Effects.success(42));
        assertEquals(42, effect.run());
    }

    @Test
    void testPureValue() {
        Effect<Integer> pure = Effects.success(100);
        assertEquals(100, pure.run());
    }

    @Test
    void testLog() {
        Effect<Integer> effect = Effects.success(42).log();
        assertEquals(42, effect.run());
    }

    @Test
    void testDefer() {
        AtomicInteger cleanup = new AtomicInteger(0);
        Effect<Integer> effect = Effects.success(42)
            .defer(() -> cleanup.incrementAndGet());
        assertEquals(42, effect.run());
        assertEquals(1, cleanup.get());
    }

    @Test
    void testGetOrElse() {
        Effect<Integer> success = Effects.success(42);
        assertEquals(42, success.orElseGet(() -> 100));

        Effect<Integer> fail = Effects.<Integer>fail(new RuntimeException("error"));
        assertEquals(100, fail.orElseGet(() -> 100));
    }
}
