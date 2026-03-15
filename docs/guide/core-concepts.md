# Core Concepts

Understanding the core types is essential for using JEffect effectively.

## Effect Types

JEffect uses a sealed interface with five implementations:

| Type | Description | Represents |
|------|-------------|------------|
| `Pure<T>` | Contains a value | Success |
| `Fail<T>` | Contains an error | Failure |
| `Suspend<T>` | Defers evaluation | Deferred computation |
| `FlatMap<A, B>` | Chains effects | Composition |
| `Recover<T>` | Error recovery | Error handling |

## The Effect Algebra

```
Effect<T>
├── Pure<T>     → Success with value
├── Fail<T>     → Failure with error  
├── Suspend<T>  → Deferred evaluation
├── FlatMap     → Chain of effects
└── Recover     → Error recovery
```

## How Effects Work

### Lazy Evaluation

Effects are **lazy** - creating an effect doesn't execute anything:

```java
// This does NOT execute the database call!
Effect<User> userEffect = Effects.of(() -> userRepository.findById(1));

// Only when you run it...
User user = userEffect.run(); // Now it executes
```

### Error Accumulation

Errors are carried through the effect chain:

```java
Effect<String> result = Effects.<Integer>fail(new RuntimeException("error"))
    .map(i -> i * 2)  // Not executed - effect already failed
    .map(i -> "Number: " + i); // Not executed

result.runSafe(); // Returns Failure with original error
```

### Short-Circuit on Failure

Once an effect fails, transformations are skipped:

```java
Effects.<String>fail(new RuntimeException("db error"))
    .map(String::toUpperCase)  // Skipped
    .flatMap(s -> ...)         // Skipped
    .recover(e -> "fallback")  // This runs!
```

## Pure Values vs Suspended

### Pure - Immediate Value

```java
Effect<String> effect = Effects.success("hello");
// The value "hello" is already stored
```

### Suspend - Deferred Value

```java
Effect<String> effect = Effects.of(() -> {
    // This runs ONLY when .run() is called
    return expensiveComputation();
});
```

Use `Pure` when you have an immediate value. Use `Suspend`/`of` when you need lazy evaluation.

## Next Steps

- [Creating Effects](/guide/creating-effects) - All ways to build effects
- [Transforming](/guide/transforming) - map, flatMap, etc.
- [Executing](/guide/executing) - Running effects
