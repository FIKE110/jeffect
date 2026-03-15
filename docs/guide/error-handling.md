# Error Handling

JEffect provides powerful error handling through recovery mechanisms.

## Basic Recovery

### recover

Provide a fallback value on failure:

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .recover(e -> User.anonymous());
```

### recoverWith

Provide a fallback effect on failure:

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .recoverWith(e -> Effects.success(User.anonymous()));
```

Useful when the recovery itself might fail:

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .recoverWith(e -> Effects.of(() -> findFromCache(id)));
```

## Common Patterns

### Database Fallback

```java
public Effect<User> findUser(Long id) {
    return Effects.of(() -> database.findById(id))
        .recoverWith(e -> Effects.of(() -> cache.get("user:" + id))
        .recoverWith(e -> Effects.fail(new UserNotFoundException(id)));
}
```

### Default Value

```java
Effect<Config> config = Effects.of(() -> loadConfig())
    .recover(e -> Config.defaults());

Effect<List<String>> items = Effects.of(() -> fetchItems())
    .recover(e -> List.of());
```

### Try Multiple Sources

```java
Effect<Data> fetchData(String key) {
    return Effects.firstSuccessOf(
        Effects.of(() -> cache.get(key)),
        Effects.of(() -> database.find(key)),
        Effects.of(() -> api.fetch(key))
    );
}
```

### Logging and Recovering

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .onError(e -> log.warn("User not found, using anonymous", e))
    .recover(e -> User.anonymous());
```

### Retry with Fallback

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .retry(3)
    .recoverWith(e -> Effects.of(() -> findFromCache(id)));
```

### Validate and Transform

```java
Effect<User> validUser = Effects.of(() -> findUser(id))
    .filter(u -> u.isActive(), () -> new InactiveUserException(id))
    .recover(e -> User.anonymous());
```

## Advanced Patterns

### Circuit Breaker Pattern

```java
public Effect<T> withCircuitBreaker(Effect<T> effect) {
    return effect.recoverWith(e -> {
        if (isCircuitOpen()) {
            return Effects.fail(new CircuitBreakerException());
        }
        recordFailure();
        return Effects.fail(e);
    });
}
```

### Timeout with Fallback

```java
Effect<Data> data = Effects.of(() -> fetchData())
    .timeout(Duration.ofSeconds(5))
    .recoverWith(e -> Effects.of(() -> getCachedData()));
```

### Validation Errors

```java
public Effect<ValidationResult> validateOrder(Order order) {
    List<String> errors = new ArrayList<>();
    
    Effect<ValidationResult> effect = Effects.success(order)
        .filter(o -> o.getItems() != null, () -> errors.add("Items required"))
        .filter(o -> o.getTotal() > 0, () -> errors.add("Invalid total"))
        .recover(e -> {
            errors.add(e.getMessage());
            return null;
        });
    
    return effect.map(__ -> 
        errors.isEmpty() 
            ? ValidationResult.valid() 
            : ValidationResult.invalid(errors)
    );
}
```

### Partial Failure

When some operations can fail without affecting others:

```java
Effect<User> userEffect = Effects.of(() -> findUser(id));

Effect<Void> emailEffect = userEffect
    .flatMap(user -> Effects.of(() -> sendEmail(user)))
    .recover(e -> {
        log.warn("Email failed", e);
        return null;
    });

Effect<Order> orderEffect = userEffect
    .flatMap(user -> Effects.of(() -> createOrder(user)));

// Combine - user is fetched once
Effect<User> finalEffect = userEffect.flatMap(user -> 
    Effects.of(() -> {
        emailEffect.run();
        return orderEffect.run();
    })
);
```

## Error Handling Patterns

### Logging Levels

```java
// Debug level
Effect<T> debug = effect.onError(e -> log.debug("Failed", e));

// Info level
Effect<T> info = effect.onError(e -> log.info("Operation failed: {}", e.getMessage()));

// Error level
Effect<T> error = effect.onError(e -> log.error("Critical failure", e));

// Warn level
Effect<T> warn = effect.onError(e -> log.warn("Recoverable error", e));
```

### Error Classification

```java
Effect<T> classified = effect.recoverWith(e -> {
    if (e instanceof NotFoundException) {
        return Effects.fail(e);  // Don't recover not found
    } else if (e instanceof TimeoutException) {
        return fallback();  // Recover from timeout
    } else if (e instanceof ValidationException) {
        return Effects.fail(e);  // Don't recover validation errors
    } else {
        return defaultValue();  // Recover from others
    }
});
```

### Composite Error Handling

```java
Effect<Result> combined = Effects.of(() -> operation1())
    .flatMap(r1 -> Effects.of(() -> operation2())
        .map(r2 -> combine(r1, r2))
    )
    .recoverWith(e -> {
        // Check what failed
        if (e instanceof Operation1Exception) {
            return Effects.success(Result.fromOperation1Default());
        } else if (e instanceof Operation2Exception) {
            return Effects.success(Result.fromOperation2Default());
        }
        return Effects.fail(e);
    });
```

## Summary

| Method | Use Case |
|--------|----------|
| `recover` | Provide fallback value |
| `recoverWith` | Provide fallback effect |
| `orElse` | Alternative effect |
| `orElseGet` | Alternative value |
| `onError` | Side-effect on failure |
| `ignore` | Discard value |
| `retry` | Retry on failure |
| `timeout` | Time limit |
| `match` | Handle both cases |
| `fold` | Fold to single value |
| `filter` | Validate and fail |
| `firstSuccessOf` | Try multiple sources |
