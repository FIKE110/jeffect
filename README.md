# JEffect

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fike110/jeffect.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.fike110/jeffect)
[![javadoc](https://javadoc.io/badge2/io.github.fike110/jeffect/javadoc.svg)](https://javadoc.io/doc/io.github.fike110/jeffect)

A functional effect library for Java that provides deferred computation with elegant error handling.

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.fike110</groupId>
    <artifactId>jeffect</artifactId>
    <version>0.1.1</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.fike110:jeffect:0.1.1'
```

## Quick Start

```java
import io.github.fike110.jeffect.Effects;
import io.github.fike110.jeffect.core.Effect;

// Create a successful effect
Effect<String> hello = Effects.success("Hello, World!");

// Execute the effect
String result = hello.run(); // "Hello, World!"
```

## Core Concepts

### What is an Effect?

An `Effect<T>` represents a deferred computation that may succeed with a value of type `T` or fail with an exception. It's similar to `Either<Throwable, T>` but with monadic operations like `map`, `flatMap`, and `recover`.

### Effect Types

| Type | Description |
|------|-------------|
| `Pure<T>` | A successful effect containing a value |
| `Fail<T>` | A failed effect containing an error |
| `Suspend<T>` | A deferred effect that evaluates when executed |
| `FlatMap<A, B>` | Chains two effects together |
| `Recover<T>` | Wraps an effect with error recovery |

## Creating Effects

### From Values

```java
// Success
Effect<String> success = Effects.success("hello");

// Failure
Effect<String> fail = Effects.fail(new RuntimeException("error"));
```

### attempt - Wrap Throwing Code

```java
// Captures exceptions into the effect
Effect<Integer> effect = Effects.attempt(() -> Integer.parseInt("abc"));
```

### From Suppliers (Deferred Execution)

```java
// From a supplier - exceptions are captured as failures
Effect<User> user = Effects.of(() -> userRepository.findById(id));

// From a runnable (for side effects)
Effect<Void> email = Effects.of(() -> sendWelcomeEmail());

// Alternative: run(Runnable)
Effect<Void> runnable = Effects.run(() -> System.out.println("Hello"));
```

### unit - Empty Effect

```java
// Effect that does nothing, returns null
Effect<Void> unit = Effects.unit();
```

### From Callables

```java
// With error mapping
Effect<Data> data = Effects.tryCatch(
    () -> fetchData(),
    e -> new CustomException("Failed to fetch", e)
);
```

### From Optional, Future, etc.

```java
// From Optional
Effect<String> fromOptional = Effects.fromOptional(Optional.of("hello"));
Effect<String> fromEmpty = Effects.fromOptional(Optional.empty());

// From CompletableFuture
Effect<String> fromFuture = Effects.fromFuture(CompletableFuture.completedFuture("hello"));
```

### Async Operations

```java
// Async with callback
Effect<String> async = Effects.async(callback -> {
    someAsyncOperation(result -> {
        callback.accept(result, null);
    });
});

// Async with timeout
Effect<String> withTimeout = Effects.async(callback -> {
    someAsyncOperation(result -> callback.accept(result, null));
}, Duration.ofSeconds(5));
```

### Sleep

```java
// Sleep for specified duration
Effect<Void> slept = Effects.sleep(Duration.ofSeconds(1));
```

## Transforming Effects

### map

Transform the value without changing the error context:

```java
Effect<Integer> result = Effects.success(5)
    .map(i -> i * 2);  // Effect<Integer> containing 10
```

### flatMap

Chain effects where each step depends on the previous result:

```java
Effect<String> result = Effects.success(5)
    .flatMap(i -> Effects.success("Number: " + i));  // "Number: 5"
```

### tap

Execute side effects without changing the value:

```java
Effect<String> result = Effects.success("hello")
    .tap(System.out::println);  // Prints "hello", returns "hello"
```

### as / asUnit

```java
// Replace the value
Effect<String> replaced = Effects.success(5).as("done"); // Effect<String>

// Convert to unit (void)
Effect<Void> unit = Effects.success("hello").asUnit(); // Effect<Void>
```

### recover

Handle errors and provide fallback values:

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .recover(e -> User.anonymous());  // Returns default user on failure
```

### recoverWith

Handle errors with another Effect:

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .recoverWith(e -> Effects.success(User.anonymous()));
```

## Executing Effects

### run() / runSync() - Throws on Failure

```java
String result = effect.run();    // Returns value or throws RuntimeException
String result2 = effect.runSync(); // Alias for run()
```

### runSafe() - No Exceptions

```java
Result<String> result = effect.runSafe();

if (result.isSuccess()) {
    String value = result.get();
} else {
    Throwable error = result.getThrowable();
}
```

### runAsync() - Fire and Forget

```java
effect.runAsync();  // Runs in ForkJoinPool.commonPool()
```

### runFuture() - Returns CompletableFuture

```java
CompletableFuture<String> future = effect.runFuture();
String result = future.get();
```

## Combining Effects

### Sequence

```java
List<Effect<User>> userEffects = ids.stream()
    .map(id -> Effects.of(() -> repo.findById(id)))
    .toList();

Effect<List<User>> allUsers = Effects.sequence(userEffects);

// Or use Effect.collect()
Effect<List<User>> collected = Effect.collect(userEffects);
```

### Traverse

```java
Effect<List<User>> users = Effects.traverse(userIds, id -> 
    Effects.of(() -> repo.findById(id))
);
```

### Parallel Execution

```java
List<Effect<User>> effects = List.of(
    Effects.of(() -> fetchUser(1)),
    Effects.of(() -> fetchUser(2)),
    Effects.of(() -> fetchUser(3))
);

Effect<List<User>> parallel = Effects.parallel(effects);

// Or use Effect.parAll()
Effect<List<User>> paralled = Effect.parAll(effects);
```

### Race (First to Complete)

```java
Effect<String> winner = Effects.race(
    Effects.of(() -> serviceA.getData()),
    Effects.of(() -> serviceB.getData())
);

// Instance method version
Effect<String> winner2 = Effects.success("fast")
    .race(Effects.success("slow"));
```

### First Success

```java
Effect<String> result = Effects.firstSuccessOf(
    Effects.of(() -> primary.get()),
    Effects.of(() -> secondary.get()),
    Effects.of(() -> cache.get())
);
```

### zip / zipWith

```java
Effect<Pair<String, Integer>> zipped = Effects.success("hello")
    .zip(Effects.success(42));

Effect<String> combined = Effects.success("hello")
    .zipWith(Effects.success(42), (a, b) -> a + b);
```

## Concurrency with Fibers

### fork / join

```java
// Fork an effect to run concurrently
Effect<Fiber<String>> forked = Effects.success("hello").fork();

// Join to get the result
String result = forked.run().join().run();
```

### cancel / interrupt

```java
// Cancel a fiber
forked.run().cancel().run();

// Or use interrupt on Effect
Effect<Void> interrupted = effect.fork().run().interrupt();
```

### forever / repeat

```java
// Repeat forever
Effect<Void> forever = Effects.unit().forever();

// Repeat N times
Effect<Void> repeated = Effects.unit().repeat(5);
```

## Conditional Effects

### if-else

```java
Effect<String> result = Effects.iff(
    Effects.success(isAdmin),
    Effects.success("Admin panel"),
    Effects.success("User dashboard")
);
```

### when (Optional)

```java
Effect<Optional<User>> result = Effects.when(userExists, Effects.success(user));
```

## Resource Management

### using (Try-with-resources pattern)

```java
Effect<File> fileEffect = Effects.using(
    Effects.of(() -> new FileInputStream("data.txt")),  // acquire
    stream -> Effects.of(() -> readData(stream)),       // use
    stream -> Effects.of(() -> stream.close())          // release
);
```

## Error Handling Patterns

### Logging

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .log()        // Print value to stdout
    .logInfo()    // Print with [INFO] prefix
    .logWarn()    // Print with [WARN] prefix
    .logError();  // Print with [ERROR] prefix
```

### Retry

```java
Effect<User> user = Effects.of(() -> fetchUser(id))
    .retry(3);  // Retry up to 3 times on failure
```

### Timeout

```java
Effect<User> user = Effects.of(() -> fetchUser(id))
    .timeout(Duration.ofSeconds(5))
    .recover(e -> User.anonymous());
```

## Integration Examples

### Spring Boot Service

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    public Effect<User> createUser(CreateUserRequest req) {
        return Effects.of(() -> {
            User user = new User(req.name(), req.email());
            return userRepository.save(user);
        }).flatMap(saved -> {
            return Effects.of(() -> {
                emailService.sendWelcome(saved);
                return saved;
            });
        });
    }
    
    public Effect<User> findById(Long id) {
        return Effects.fromOptional(userRepository.findById(id), "User not found");
    }
}
```

### Spring Boot Controller

```java
@RestController
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userService.findById(id)
            .map(user -> ResponseEntity.ok(user))
            .recover(e -> ResponseEntity.notFound().build())
            .run()  // Execute and return
            .toOptional()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

### Async Execution

```java
@Service
public class NotificationService {
    
    private final EmailEffects emailEffects;
    
    @Async
    public CompletableFuture<Void> sendWelcomeAsync(String email, String name) {
        return emailEffects.sendGreeting(email, name)
            .recover(e -> log.warn("Failed to send greeting", e))
            .runFuture();  // Returns CompletableFuture
    }
}
```

## API Reference

### Effects Factory Methods

| Method | Description |
|--------|-------------|
| `success(T value)` | Creates a successful effect |
| `fail(Throwable error)` | Creates a failed effect |
| `attempt(Supplier<T>)` | Wrap throwing code, capture exceptions |
| `of(Supplier<T>)` | Creates from supplier, catches exceptions |
| `of(Runnable)` | Creates void effect from runnable |
| `run(Runnable)` | Execute runnable, return Effect |
| `suspend(Supplier<Effect<T>>)` | Creates suspended effect |
| `unit()` | Creates empty effect |
| `tryCatch(Callable, Function)` | Creates with error mapping |
| `sleep(Duration)` | Sleep for duration |
| `async(Callback)` | Async with callback |
| `schedule(Duration, Supplier)` | Schedule delayed execution |
| `sequence(List<Effect<T>>)` | Sequences list of effects |
| `traverse(List, Function)` | Maps and sequences |
| `parallel(List<Effect<T>>)` | Executes in parallel |
| `race(Effect, Effect)` | Returns first to complete |
| `firstSuccessOf(Effect...)` | Returns first success |
| `iff(Effect<Boolean>, Effect, Effect)` | Conditional |
| `when(boolean, Effect)` | Conditional returning Optional |
| `using(Effect, Function, Function)` | Resource management |
| `cachedFunction(Function)` | Memoizes function results |

### Effect Instance Methods

| Method | Description |
|--------|-------------|
| **Execution** | |
| `run()` / `runSync()` | Execute and return value or throw |
| `runSafe()` | Execute and return Result |
| `runAsync()` | Execute asynchronously (fire-and-forget) |
| `runFuture()` | Execute and return CompletableFuture |
| **Transformation** | |
| `map(Function)` | Transform the value |
| `flatMap(Function)` | Chain effects |
| `tap(Consumer)` | Side-effect without changing value |
| `as(R)` | Replace the value |
| `asUnit()` | Convert to Effect<Void> |
| **Error Handling** | |
| `recover(Function)` | Handle error with fallback value |
| `recoverWith(Function)` | Handle error with new Effect |
| `orElse(Supplier<Effect<T>>)` | Provide fallback effect |
| `orElseGet(Supplier<T>)` | Provide fallback value |
| `onError(Consumer)` | Side-effect on error |
| **Looping** | |
| `retry(int)` | Retry on failure |
| `repeat(int)` | Repeat N times |
| `forever()` | Repeat forever |
| **Time** | |
| `delay(Duration)` | Delay execution |
| `sleep(Duration)` | Sleep before execution |
| `timeout(Duration)` | Timeout execution |
| **Logging** | |
| `log()` | Print value to stdout |
| `logInfo()`, `logWarn()`, `logError()` | Logging variants |
| **Other** | |
| `ignore()` | Discard value, return void |
| `peek(Consumer)` | Side-effect with execution |
| `then(Effect)` | Sequence effects |
| `zip(Effect)` | Combine two effects |
| `zipWith(Effect, BiFunction)` | Combine with function |
| `race(Effect)` | Race with another effect |

## License

MIT License - see [LICENSE](LICENSE) for details.
