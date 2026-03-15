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
| `of(Supplier<T>)` | Deferred execution, auto-catches exceptions |
| `suspend(Supplier<Effect<T>>)` | Suspended effect |
| `tryCatch(Callable, Function)` | With custom error mapping |
| `fromOptional(Optional)` | From Optional |
| `fromFuture(CompletableFuture)` | From async future |
| `fromRunnable(Runnable)` | From side-effect |
| `fromCallable(Callable)` | From Callable |

## Best Practices

1. **Use `of(Supplier)` for I/O** - It automatically catches exceptions
2. **Use `success/fail` for known values** - No deferral overhead
3. **Use `tryCatch` for error mapping** - When you need custom exceptions
