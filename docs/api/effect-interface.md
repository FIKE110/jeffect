# Effect Interface

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fike110/jeffect.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.fike110/jeffect)
[![javadoc](https://javadoc.io/badge2/io.github.fike110/jeffect/javadoc.svg)](https://javadoc.io/doc/io.github.fike110/jeffect)

The `Effect&lt;T&gt;` interface is the core type in JEffect. It represents a deferred computation that may succeed with a value of type `T` or fail with an exception.

## Interface Methods

### Transformation

#### map

```java
<R> Effect<R> map(Function<T, R> mapper)
```

Transforms the effect's value using the provided function. If the effect fails, the mapper is not applied.

```java
Effect<Integer> result = Effects.success(5)
    .map(i -> i * 2);  // Effect<Integer> containing 10
```

#### flatMap

```java
<R> Effect<R> flatMap(Function<T, Effect<R>> mapper)
```

Chains two effects together, where the second effect depends on the result of the first.

```java
Effect<String> result = Effects.success(5)
    .flatMap(i -> Effects.success("Number: " + i));
```

#### tap

```java
Effect<T> tap(Consumer<T> action)
```

Executes a side effect without changing the value.

```java
Effect<String> result = Effects.success("hello")
    .tap(System.out::println);  // Prints "hello"
```

#### as

```java
<R> Effect<R> as(R value)
```

Replaces the effect's value with a new value.

```java
Effect<String> result = Effects.success(5).as("done");
```

#### asUnit

```java
Effect<Void> asUnit()
```

Converts the effect to a unit effect (Effect<Void>).

```java
Effect<Void> result = Effects.success("hello").asUnit();
```

### Execution

#### run / runSync

```java
T run()
T runSync()
```

Executes the effect and returns the result, throwing if failed.

```java
String result = effect.run();
```

#### runSafe

```java
Result<T> runSafe()
```

Executes the effect and returns a Result without throwing.

```java
Result<String> result = effect.runSafe();
```

#### runAsync

```java
void runAsync()
void runAsync(Executor executor)
```

Executes the effect asynchronously using the default executor or a custom executor.

```java
effect.runAsync();
```

#### runFuture

```java
CompletableFuture<T> runFuture()
CompletableFuture<T> runFuture(Executor executor)
```

Executes the effect asynchronously and returns a CompletableFuture.

```java
CompletableFuture<String> future = effect.runFuture();
```

### Error Handling

#### recover

```java
Effect<T> recover(Function<Throwable, T> handler)
```

Recovers from a failure by applying a handler function to the error.

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .recover(e -> User.anonymous());
```

#### recoverWith

```java
Effect<T> recoverWith(Function<Throwable, Effect<T>> handler)
```

Recovers from a failure by applying a handler function that returns an Effect.

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .recoverWith(e -> Effects.success(User.anonymous()));
```

#### orElse

```java
Effect<T> orElse(Supplier<Effect<T>> fallback)
T orElseGet(Supplier<T> defaultValue)
```

Returns this effect if successful, otherwise returns the fallback effect.

```java
Effect<String> result = effect.orElse(Effects.success("default"));
```

#### onError

```java
Effect<T> onError(Consumer<Throwable> action)
```

Executes a side effect when an error occurs.

```java
Effect<String> result = effect.onError(e -> log.error("Error", e));
```

### Looping

#### retry

```java
Effect<T> retry(int retries)
```

Retries the effect up to N times on failure.

```java
Effect<User> user = Effects.of(() -> fetchUser(id))
    .retry(3);
```

#### repeat

```java
Effect<Void> repeat(int times)
```

Repeats the effect N times.

```java
Effect<Void> repeated = Effects.unit().repeat(5);
```

#### forever

```java
Effect<T> forever()
```

Repeats the effect forever.

```java
Effect<Void> forever = Effects.unit().forever();
```

### Time

#### delay

```java
Effect<T> delay(Duration duration)
```

Delays execution of this effect by the specified duration.

```java
Effect<String> delayed = Effects.success("hello")
    .delay(Duration.ofSeconds(2));
```

#### sleep

```java
Effect<T> sleep(Duration duration)
```

Sleeps for the specified duration then returns the result.

```java
Effect<String> slept = effect.sleep(Duration.ofSeconds(1));
```

#### timeout

```java
Effect<T> timeout(Duration duration)
```

Times out the effect if it takes too long.

```java
Effect<User> user = Effects.of(() -> fetchUser(id))
    .timeout(Duration.ofSeconds(5));
```

### Concurrency

#### fork

```java
Effect<Fiber<T>> fork()
```

Forks the effect to run concurrently, returning a Fiber.

```java
Effect<Fiber<String>> forked = effect.fork();
```

#### join

```java
Effect<T> join(Fiber<T> fiber)
```

Joins the result of a forked fiber.

```java
String result = forked.run().join().run();
```

#### interrupt

```java
Effect<Void> interrupt(Fiber<?> fiber)
```

Interrupts a running fiber.

```java
effect.fork().run().interrupt();
```

#### race

```java
Effect<T> race(Effect<T> other)
```

Runs this effect and another effect in parallel, returning the first to succeed.

```java
Effect<String> winner = Effects.success("fast")
    .race(Effects.success("slow"));
```

### Combination

#### then

```java
<R> Effect<R> then(Effect<R> next)
```

Sequences effects - runs this effect then the next.

```java
Effect<Void> program = Effects.success("hello")
    .then(Effects.success("world"));
```

#### zip

```java
Effect<Pair<T, U>> zip(Effect<U> other)
```

Combines two effects into one containing a Pair.

```java
Effect<Pair<String, Integer>> zipped = Effects.success("hello")
    .zip(Effects.success(42));
```

#### zipWith

```java
<U, R> Effect<R> zipWith(Effect<U> other, BiFunction<T, U, R> combiner)
```

Combines two effects using a function.

```java
Effect<String> combined = Effects.success("hello")
    .zipWith(Effects.success(42), (a, b) -> a + b);
```

### Logging

#### log / logInfo / logWarn / logError

```java
Effect<T> log()
Effect<T> logDebug()
Effect<T> logInfo()
Effect<T> logWarn()
Effect<T> logError()
Effect<T> logFatal()
```

Logs the value with optional prefixes.

```java
Effect<String> logged = effect.logInfo();
```

### Other

#### peek

```java
Effect<T> peek(Consumer<T> action)
```

Executes a side effect and returns the original value.

```java
Effect<String> peeked = effect.peek(v -> System.out.println("Value: " + v));
```

#### ignore

```java
Effect<Void> ignore()
```

Discards the value and returns void.

```java
Effect<Void> ignored = effect.ignore();
```

#### memoize / cached

```java
Effect<T> memoize()
Effect<T> cached()
```

Caches the result of the effect.

```java
Effect<T> cached = effect.memoize();
```

#### defer

```java
Effect<T> defer(Runnable cleanup)
```

Runs cleanup after the effect completes.

```java
Effect<T> deferred = effect.defer(() -> cleanup());
```

### Static Methods

#### collect / all

```java
static <T> Effect<List<T>> collect(List<Effect<T>> effects)
static <T> Effect<List<T>> all(List<Effect<T>> effects)
```

Sequences a list of effects into a single effect containing a list.

#### parAll

```java
static <T> Effect<List<T>> parAll(List<Effect<T>> effects)
```

Runs a list of effects in parallel.

#### race

```java
static <T> Effect<T> race(Effect<T> a, Effect<T> b)
```

Races two effects, returning the first to complete.
