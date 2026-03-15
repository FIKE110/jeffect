# Effects Factory

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

### suspend

```java
public static <T> Effect<T> suspend(Supplier<Effect<T>> supplier)
```

Creates a suspended effect that evaluates the supplier when executed.

**Parameters:**
- `supplier` - The supplier that produces an Effect

**Returns:** A suspended Effect

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
