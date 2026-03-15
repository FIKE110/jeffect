# Creating Effects

JEffect provides many ways to create effects depending on your use case.

## From Values

### success

Creates a successful effect with a value:

```java
Effect<String> hello = Effects.success("Hello");
Effect<Integer> number = Effects.success(42);
Effect<User> user = Effects.success(new User("john"));
```

### fail

Creates a failed effect with an error:

```java
Effect<User> error = Effects.fail(new UserNotFoundException("User 1 not found"));
```

## attempt

`attempt` captures exceptions from normal Java code and turns them into an Effect.

Normally in Java, code throws immediately:

```java
int x = Integer.parseInt("abc"); // throws exception
```

But in an effect system, we do not execute immediately. We capture the computation so it runs later:

```java
Effect<Integer> effect = Effects.attempt(() -> Integer.parseInt("abc"));
```

The exception is inside the effect, not thrown yet. When you run it:

```java
effect.runSync();
```

Then the error appears.

### Why attempt is important

It lets you safely wrap any Java code that might throw:

```java
Effect<String> readFile = Effects.attempt(() -> 
    Files.readString(Path.of("file.txt"))
);
```

Then you can handle errors functionally:

```java
Effect<String> safe = Effects.attempt(() -> Files.readString(Path.of("file.txt")))
    .recover(e -> "default");
```

### Conceptual signature

```
attempt : () -> T  →  Effect<T>
```

## unit

`unit` represents an effect that does nothing and returns nothing. It is equivalent to `Effect<Void>`:

```java
public static Effect<Void> unit() {
    return Effects.success(null);
}
```

### Why unit exists

Sometimes you need an effect just for sequencing, not for returning a value:

```java
Effect<Void> program = Effects.run(() -> System.out.println("Hello"))
    .then(Effects.unit());
```

Or inside loops:

```java
Effect<Void> repeat = Effects.unit().repeat(10);
```

### Real example

```java
Effect<Void> program = Effects.attempt(() -> Integer.parseInt("123"))
    .tap(System.out::println)
    .asUnit();

program.runSync();
// Output: 123
```

### Rule of thumb

- Use `attempt` → when wrapping throwing code
- Use `unit` → when you just need an empty effect

## From Suppliers

### of(Supplier)

Creates a deferred effect from a supplier. Exceptions are caught and stored as failures:

```java
Effect<User> user = Effects.of(() -> userRepository.findById(id));

Effect<Void> action = Effects.of(() -> {
    sendEmail();
    log("Email sent");
});
```

### suspend

Creates a suspended effect that returns another effect:

```java
Effect<User> user = Effects.suspend(() -> 
    Effects.of(() -> userRepository.findById(id))
);
```

## From Callables

### tryCatch

Creates an effect with custom error mapping:

```java
Effect<Data> data = Effects.tryCatch(
    () -> fetchData(),
    e -> new DataFetchException("Failed to fetch", e)
);
```

### fromCallable

Similar to `of(Supplier)` but from a Callable:

```java
Effect<String> result = Effects.fromCallable(() -> "value");
```

## From Optional

### fromOptional

Converts Optional to Effect:

```java
Effect<String> effect = Effects.fromOptional(Optional.of("hello"));
Effect<String> empty = Effects.fromOptional(Optional.empty()); // Fails
```

With custom error message:

```java
Effect<User> user = Effects.fromOptional(
    userRepository.findById(id),
    "User not found"
);
```

## From Future

### fromFuture

Converts CompletableFuture to Effect:

```java
CompletableFuture<User> future = userService.fetchUserAsync(id);
Effect<User> effect = Effects.fromFuture(future);
```

## From Runnable

### of(Runnable) / fromRunnable

Creates a void effect from a Runnable:

```java
Effect<Void> effect = Effects.of(() -> {
    System.out.println("Side effect!");
});
```

## Factory Methods Summary

| Method | Use Case |
|--------|----------|
| `success(T)` | Immediate successful value |
| `fail(Throwable)` | Immediate failure |
| `attempt(Supplier<T>)` | Wrap risky code, capture exceptions |
| `of(Supplier<T>)` | Deferred execution, auto-catches exceptions |
| `suspend(Supplier<Effect<T>>)` | Suspended effect |
| `tryCatch(Callable, Function)` | With custom error mapping |
| `fromOptional(Optional)` | From Optional |
| `fromFuture(CompletableFuture)` | From async future |
| `fromRunnable(Runnable)` | From side-effect |
| `fromCallable(Callable)` | From Callable |
| `unit()` | Effect that does nothing |

## Best Practices

1. **Use `attempt` for throwing code** - Wraps exceptions into the effect
2. **Use `of(Supplier)` for I/O** - It automatically catches exceptions
3. **Use `success/fail` for known values** - No deferral overhead
4. **Use `tryCatch` for error mapping** - When you need custom exceptions
5. **Use `unit()` for sequencing** - When you need an empty effect
