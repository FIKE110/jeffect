# Deferred<T, R>

The `Deferred` interface represents a deferred effect that waits for an input value to be supplied later.

## Overview

`Deferred` is useful when you want to define an effect computation upfront but provide the input value at execution time. This is similar to partial application in functional programming.

## Creating Deferred Effects

### effectOf

```java
Deferred<User, String> getName = Effects.effectOf(user -> user.getName());
String name = getName.run(user);
```

### effectOfVoid

```java
Deferred<User, Void> printUser = Effects.effectOfVoid(user -> System.out.println(user));
printUser.run(user);
```

## Methods

### supply

```java
Effect<R> supply(T input)
```

Creates an Effect by supplying the input value.

### run

```java
default R run(T input)
```

Executes the deferred effect and returns the result. Throws on failure.

### runSafe

```java
default Result<R> runSafe(T input)
```

Executes the deferred effect safely, returning a Result.

### map

```java
default <U> Deferred<T, U> map(Function<R, U> mapper)
```

Transforms the result of the deferred effect.

### flatMap

```java
default <U> Deferred<T, U> flatMap(Function<R, Deferred<T, U>> mapper)
```

Chains another deferred effect that depends on the result.

### tap

```java
default Deferred<T, R> tap(Consumer<R> action)
```

Executes a side effect with the result without changing it.

### recover

```java
default Deferred<T, R> recover(Function<Throwable, R> handler)
```

Recovers from failures in the deferred effect.

### recoverWith

```java
default Deferred<T, R> recoverWith(Function<Throwable, Deferred<T, R>> handler)
```

Recovers from failures with another deferred effect.

## Examples

### Basic Usage

```java
Deferred<String, Integer> length = Effects.effectOf(String::length);
assertEquals(5, length.run("hello"));
```

### With Side Effects

```java
AtomicInteger counter = new AtomicInteger(0);
Deferred<String, Void> increment = Effects.effectOfVoid(s -> counter.incrementAndGet());
increment.run("test");
assertEquals(1, counter.get());
```

### Chaining

```java
Deferred<String, Integer> length = Effects.effectOf(String::length)
    .map(len -> len * 2);
    
assertEquals(10, length.run("hello")); // "hello".length() = 5 * 2 = 10
```

### Error Recovery

```java
Deferred<String, Integer> safeLength = Effects.<String, Integer>effectOf(s -> {
    if (s == null) throw new RuntimeException("null not allowed");
    return s.length();
}).recover(e -> 0);

assertEquals(5, safeLength.run("hello"));
assertEquals(0, safeLength.run(null));
```
