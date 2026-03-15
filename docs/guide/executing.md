# Executing Effects

Effects are lazy - they need to be executed to get a result.

## run

Execute and return the value, throw on failure:

```java
String result = effect.run();
// Returns value or throws RuntimeException
```

**Note:** This throws RuntimeException on failure. Use `runSafe` for safe execution.

## runSafe

Execute and get a Result without exceptions:

```java
Result<String> result = effect.runSafe();

if (result.isSuccess()) {
    String value = result.get();
} else {
    Throwable error = result.getThrowable();
}
```

## runAsync

Fire-and-forget execution:

```java
effect.runAsync(); // Runs in ForkJoinPool.commonPool()
```

Errors are silently ignored. Use with caution.

## runFuture

Execute and return a CompletableFuture:

```java
CompletableFuture<String> future = effect.runFuture();

String result = future.get(); // Block until complete
```

With custom executor:

```java
ExecutorService executor = Executors.newFixedThreadPool(4);
CompletableFuture<String> future = effect.runFuture(executor);
```

## runSafeFuture

Execute and return a safe future:

```java
CompletableFuture<Result<String>> future = effect.runSafeFuture();
```

## Execution Comparison

| Method | Returns | Throws | Async |
|--------|---------|--------|-------|
| run() | Value | Yes | No |
| runSafe() | Result | No | No |
| runAsync() | void | No | Yes |
| runFuture() | CompletableFuture | Yes | Yes |
| runSafeFuture() | CompletableFuture | No | Yes |

## When to Use Each

### Use run() when:
- You want the value or propagate the error
- In service methods that can throw

### Use runSafe() when:
- You need to handle errors explicitly
- In controllers or entry points

### Use runAsync() when:
- You don't care about the result
- Fire-and-forget notifications, logging

### Use runFuture() when:
- You need async execution with result handling
- Integration with other async code

### Use runSafeFuture() when:
- You need async plus explicit error handling
