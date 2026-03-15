# Transforming Effects

Transform effects using map, flatMap, and other combinators.

## map

Transform the value without changing error context:

```java
Effect<Integer> result = Effects.success(5)
    .map(i -> i * 2);  // Effect<Integer> containing 10
```

On failure, map is skipped:

```java
Effects.<Integer>fail(new RuntimeException("error"))
    .map(i -> i * 2)  // Not executed
    .runSafe()        // Returns Failure
```

## flatMap

Chain effects where each step depends on the previous result:

```java
Effect<String> result = Effects.success(5)
    .flatMap(i -> Effects.success("Number: " + i));
// Result: "Number: 5"
```

Common use case - dependent operations:

```java
Effect<Order> orderEffect = Effects.of(() -> findUser(id))
    .flatMap(user -> Effects.of(() -> findOrderForUser(user.getId())));
```

## andThen

Execute effects in sequence:

```java
Effects.of(() -> validate(input))
    .andThen(() -> process(input))
    .andThen(() -> sendResponse());
```

## tap / peek

Side-effect without changing the value:

```java
Effect<User> logged = Effects.of(() -> findUser(id))
    .tap(user -> System.out.println("Found: " + user));
    // Returns the same User, but logs during execution
```

## filter

Filter the value, fail if predicate doesn't match:

```java
Effect<User> adult = Effects.success(user)
    .filter(u -> u.getAge() >= 18, 
            () -> new IllegalArgumentException("User must be adult"));
```

## transform

Transform both success and failure:

```java
Effect<String> result = Effects.success(42).transform(
    i -> "Number: " + i,           // on success
    e -> "Error: " + e.getMessage() // on failure
);
```

## Method Chaining

Combine multiple transformations:

```java
Effect<String> result = Effects.of(() -> fetchUser(id))
    .map(User::getName)                    // User -> String
    .map(String::toUpperCase)              // uppercase
    .recover(e -> "Guest")                  // fallback on error
    .map(s -> "Welcome, " + s);             // add prefix
```

## Summary

| Method | Purpose |
|--------|---------|
| `map` | Transform value |
| `flatMap` | Chain dependent effects |
| `andThen` | Sequence side-effects |
| `tap` | Side-effect without changing value |
| `filter` | Validate and fail if invalid |
| `transform` | Handle both success and failure |
