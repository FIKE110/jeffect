# Effects Factory

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fike110/jeffect.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.fike110/jeffect)
[![javadoc](https://javadoc.io/badge2/io.github.fike110/jeffect/javadoc.svg)](https://javadoc.io/doc/io.github.fike110/jeffect)

The `Effects` class provides factory methods for creating Effect instances.

## Creation Methods

### success

```java
public static <T> Effect<T> success(T value)
```

Creates a successful effect containing the given value.

**Parameters:**
- `value` - The value to wrap

**Returns:** A successful Effect containing the value

**Example:**
```java
Effect<String> hello = Effects.success("Hello");
```

---

### fail

```java
public static <T> Effect<T> fail(Throwable throwable)
```

Creates a failed effect containing the given error.

**Parameters:**
- `throwable` - The error to wrap

**Returns:** A failed Effect containing the error

**Example:**
```java
Effect<User> error = Effects.fail(new RuntimeException("Not found"));
```

---

### attempt

```java
public static <T> Effect<T> attempt(Supplier<T> supplier)
```

Captures exceptions from normal Java code and turns them into an Effect.

**Parameters:**
- `supplier` - The throwing supplier to wrap

**Returns:** An Effect containing the supplier's value or an error

**Example:**
```java
Effect<Integer> effect = Effects.attempt(() -> Integer.parseInt("abc"));
```

---

### of(Supplier)

```java
public static <T> Effect<T> of(Supplier<T> supplier)
```

Creates a deferred effect from a Supplier. Exceptions are captured as failures.

**Parameters:**
- `supplier` - The supplier to execute

**Returns:** An Effect containing the supplier's value or an error

**Example:**
```java
Effect<User> user = Effects.of(() -> repository.findById(id));
```

---

### of(Runnable)

```java
public static Effect<Void> of(Runnable runnable)
```

Creates a deferred effect from a Runnable for side effects.

**Parameters:**
- `runnable` - The runnable to execute

**Returns:** An Effect containing void (null)

**Example:**
```java
Effect<Void> effect = Effects.of(() -> sendEmail());
```

---

### run

```java
public static Effect<Void> run(Runnable runnable)
```

Executes a Runnable and returns an Effect that completes when done.

**Parameters:**
- `runnable` - The runnable to execute

**Returns:** An Effect that completes when the runnable finishes

---

### suspend

```java
public static <T> Effect<T> suspend(Supplier<Effect<T>> supplier)
```

Creates a suspended effect that evaluates the supplier when executed.

**Parameters:**
- `supplier` - The supplier that produces an Effect

**Returns:** A suspended Effect

---

### unit

```java
public static Effect<Void> unit()
```

Creates an effect that does nothing and returns nothing. Equivalent to `Effect<Void>`.

**Returns:** A unit effect

**Example:**
```java
Effect<Void> program = Effects.unit();
```

---

### sleep

```java
public static Effect<Void> sleep(Duration duration)
```

Creates an effect that sleeps for the specified duration.

**Parameters:**
- `duration` - The duration to sleep

**Returns:** An Effect that completes after the duration

---

### async

```java
public static <T> Effect<T> async(Consumer<Callback<T>> callback)
public static <T> Effect<T> async(Consumer<Callback<T>> callback, Duration timeout)
```

Creates an async effect that runs a callback-based operation.

**Parameters:**
- `callback` - The consumer that receives a callback to complete the effect
- `timeout` - (optional) Timeout duration

**Returns:** An Effect containing the async result

---

### schedule

```java
public static <T> Effect<T> schedule(Duration delay, Supplier<Effect<T>> effect)
```

Creates an effect that runs on the scheduler after a delay.

**Parameters:**
- `delay` - The delay before execution
- `effect` - The effect to run

**Returns:** An Effect that runs after the delay

---

### tryCatch

```java
public static <T> Effect<T> tryCatch(
    Callable<T> action, 
    Function<Throwable, ? extends Throwable> mapper
)
```

Creates an effect from a Callable with custom error mapping.

**Parameters:**
- `action` - The callable to execute
- `mapper` - Function to map exceptions

**Returns:** An Effect containing the value or a mapped error

---

### fromOptional

```java
public static <T> Effect<T> fromOptional(Optional<T> optional)
public static <T> Effect<T> fromOptional(Optional<T> optional, String message)
```

Creates an effect from an Optional.

**Parameters:**
- `optional` - The optional to convert
- `message` - (optional) Custom error message

**Returns:** An Effect containing the value or NoSuchElementException

---

### fromFuture

```java
public static <T> Effect<T> fromFuture(CompletableFuture<T> future)
```

Creates an effect from a CompletableFuture.

**Parameters:**
- `future` - The future to convert

**Returns:** An Effect containing the future's value or an error

---

## Combination Methods

### sequence

```java
public static <T> Effect<List<T>> sequence(List<Effect<T>> effects)
```

Sequences a list of Effects into a single Effect containing a list.

**Parameters:**
- `effects` - The list of effects to sequence

**Returns:** An Effect containing a list of all values

---

### traverse

```java
public static <T, R> Effect<List<R>> traverse(List<T> list, Function<T, Effect<R>> f)
```

Maps a list to a list of Effects, then sequences them.

**Parameters:**
- `list` - The input list
- `f` - The function to map each element to an Effect

**Returns:** An Effect containing a list of mapped values

---

### parallel

```java
public static <T> Effect<List<T>> parallel(List<Effect<T>> effects)
```

Executes effects in parallel.

**Parameters:**
- `effects` - The list of effects

**Returns:** An Effect containing a list of all results

---

### race

```java
public static <T> Effect<T> race(Effect<T> a, Effect<T> b)
```

Races two effects, returning the first to complete.

**Parameters:**
- `a` - The first effect
- `b` - The second effect

**Returns:** An Effect containing the first result to complete

---

### firstSuccessOf

```java
@SafeVarargs
public static <T> Effect<T> firstSuccessOf(Effect<T>... effects)
```

Returns the first effect that succeeds.

**Parameters:**
- `effects` - The effects to try

**Returns:** The first successful effect, or all fail if none succeed

---

### using

```java
public static <R, T> Effect<T> using(
    Effect<R> acquire,
    Function<R, Effect<T>> use,
    Function<R, Effect<Void>> release
)
```

Resource acquisition with automatic cleanup (try-with-resources pattern).

**Parameters:**
- `acquire` - Effect to acquire the resource
- `use` - Function that uses the resource
- `release` - Function to release the resource

**Returns:** An Effect with automatic resource cleanup

---

### iff

```java
public static <T> Effect<T> iff(
    Effect<Boolean> condition, 
    Effect<T> onTrue, 
    Effect<T> onFalse
)
```

Conditional effect - evaluates either onTrue or onFalse.

**Parameters:**
- `condition` - The boolean condition effect
- `onTrue` - Effect to run if condition is true
- `onFalse` - Effect to run if condition is false

**Returns:** The selected effect

---

### when

```java
public static <T> Effect<Optional<T>> when(boolean condition, Effect<T> effect)
```

Conditionally executes an effect.

**Parameters:**
- `condition` - Whether to execute the effect
- `effect` - The effect to execute if true

**Returns:** Optional.of(value) or Optional.empty

---

## Utility Methods

### run

```java
public static <T> T run(Effect<T> effect)
```

Executes an effect and returns the result, throwing if failed.

**Parameters:**
- `effect` - The effect to execute

**Returns:** The result value

**Throws:** RuntimeException if the effect fails

---

### runSafe

```java
public static <T> Result<T> runSafe(Effect<T> effect)
```

Executes an effect and returns a Result without throwing.

**Parameters:**
- `effect` - The effect to execute

**Returns:** A Result containing the value or the error

---

### cachedFunction

```java
public static <A, B> Function<A, Effect<B>> cachedFunction(Function<A, Effect<B>> f)
```

Creates a cached version of a function.

**Parameters:**
- `f` - The function to cache

**Returns:** A cached version of the function
