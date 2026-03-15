# JEffect

A functional effect library for Java that provides deferred computation with error handling.

## Installation

### Maven

```xml
<dependency>
    <groupId>com.github.fike110</groupId>
    <artifactId>jeffect</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.github.fike110:jeffect:1.0.0'
```

## Quick Start

```java
import com.github.fike110.jeffect.Effects;
import com.github.fike110.jeffect.core.Effect;

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

### From Suppliers (Deferred Execution)

```java
// From a supplier - exceptions are captured as failures
Effect<User> user = Effects.of(() -> userRepository.findById(id));

// From a runnable (for side effects)
Effect<Void> email = Effects.of(() -> sendWelcomeEmail());
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

### run() - Throws on Failure

```java
String result = effect.run();  // Returns value or throws RuntimeException
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
```

### Race (First to Complete)

```java
Effect<String> winner = Effects.race(
    Effects.of(() -> serviceA.getData()),
    Effects.of(() -> serviceB.getData())
);
```

### First Success

```java
Effect<String> result = Effects.firstSuccessOf(
    Effects.of(() -> primary.get()),
    Effects.of(() -> secondary.get()),
    Effects.of(() -> cache.get())
);
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
| `of(Supplier<T>)` | Creates from supplier, catches exceptions |
| `of(Runnable)` | Creates void effect from runnable |
| `suspend(Supplier<Effect<T>>)` | Creates suspended effect |
| `tryCatch(Callable, Function)` | Creates with error mapping |
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
| `run()` | Execute and return value or throw |
| `runSafe()` | Execute and return Result |
| `runAsync()` | Execute asynchronously (fire-and-forget) |
| `runFuture()` | Execute and return CompletableFuture |
| `map(Function)` | Transform the value |
| `flatMap(Function)` | Chain effects |
| `recover(Function)` | Handle error with fallback value |
| `recoverWith(Function)` | Handle error with new Effect |
| `orElse(Supplier<Effect<T>>)` | Provide fallback effect |
| `orElseGet(Supplier<T>)` | Provide fallback value |
| `onError(Consumer)` | Side-effect on error |
| `retry(int)` | Retry on failure |
| `delay(Duration)` | Delay execution |
| `log()`, `logInfo()`, etc. | Logging variants |
| `ignore()` | Discard value, return void |

## License

MIT License - see [LICENSE](LICENSE) for details.
