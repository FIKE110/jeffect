# Effect Interface

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fike110/jeffect/0.1.4.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.fike110/jeffect/0.1.4)
[![javadoc](https://javadoc.io/badge2/io.github.fike110/jeffect/0.1.4.svg)](https://javadoc.io/doc/io.github.fike110/jeffect/0.1.4)

The `Effect` interface is the core type in JEffect. It represents a deferred computation that may succeed with a value or fail with an exception.

## Transformation

### map

Transform the value:

```java
Effect<Integer> result = Effects.success(5)
    .map(i -> i * 2);  // Effect containing 10
```

### flatMap

Chain effects:

```java
Effect<String> result = Effects.success(5)
    .flatMap(i -> Effects.success("Number: " + i));
```

### tap

Execute side effects:

```java
Effect<String> result = Effects.success("hello")
    .tap(System.out::println);  // Prints "hello"
```

### as

Replace the value:

```java
Effect<String> result = Effects.success(5).as("done");
```

### asUnit

Convert to void:

```java
Effect<Void> result = Effects.success("hello").asUnit();
```

## Execution

### run / runSync

Execute and return value or throw:

```java
String result = effect.run();
```

### runSafe

Execute and return Result:

```java
Result<String> result = effect.runSafe();
```

### runAsync

Execute asynchronously:

```java
effect.runAsync();
```

### runFuture

Execute and return CompletableFuture:

```java
CompletableFuture<String> future = effect.runFuture();
```

## Error Handling

### recover

Handle errors with fallback:

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .recover(e -> User.anonymous());
```

### recoverWith

Handle errors with new Effect:

```java
Effect<User> user = Effects.of(() -> findUser(id))
    .recoverWith(e -> Effects.success(User.anonymous()));
```

### orElse

Provide fallback:

```java
Effect<String> result = effect.orElse(Effects.success("default"));
```

### onError

Side effect on error:

```java
Effect<String> result = effect.onError(e -> log.error("Error", e));
```

## Looping

### retry

Retry on failure:

```java
Effect<User> user = Effects.of(() -> fetchUser(id))
    .retry(3);
```

### repeat

Repeat N times:

```java
Effect<Void> repeated = Effects.unit().repeat(5);
```

### forever

Repeat forever:

```java
Effect<Void> forever = Effects.unit().forever();
```

## Time

### delay

Delay execution:

```java
Effect<String> delayed = Effects.success("hello")
    .delay(Duration.ofSeconds(2));
```

### sleep

Sleep before execution:

```java
Effect<String> slept = effect.sleep(Duration.ofSeconds(1));
```

### timeout

Timeout execution:

```java
Effect<User> user = Effects.of(() -> fetchUser(id))
    .timeout(Duration.ofSeconds(5));
```

## Concurrency

### fork

Fork to run concurrently:

```java
Effect<Fiber<String>> forked = effect.fork();
```

### join

Join a forked fiber:

```java
String result = forked.run().join().run();
```

### interrupt

Interrupt a fiber:

```java
effect.fork().run().interrupt();
```

### race

Race with another effect:

```java
Effect<String> winner = Effects.success("fast")
    .race(Effects.success("slow"));
```

## Combination

### then

Sequence effects:

```java
Effect<Void> program = Effects.success("hello")
    .then(Effects.success("world"));
```

### zip

Combine two effects:

```java
Effect<Pair<String, Integer>> zipped = Effects.success("hello")
    .zip(Effects.success(42));
```

### zipWith

Combine with function:

```java
Effect<String> combined = Effects.success("hello")
    .zipWith(Effects.success(42), (a, b) -> a + b);
```

## Logging

### log / logInfo / logWarn / logError

Log values:

```java
Effect<String> logged = effect.logInfo();
```

## Other

### peek

Side effect:

```java
Effect<String> peeked = effect.peek(v -> System.out.println("Value: " + v));
```

### ignore

Discard value:

```java
Effect<Void> ignored = effect.ignore();
```

### memoize / cached

Cache result:

```java
Effect<T> cached = effect.memoize();
```

### defer

Run cleanup after:

```java
Effect<T> deferred = effect.defer(() -> cleanup());
```

## Static Methods

### collect / all

Sequence list of effects:

```java
Effect<List<T>> collected = Effect.collect(effects);
```

### parAll

Parallel execution:

```java
Effect<List<T>> parallel = Effect.parAll(effects);
```
