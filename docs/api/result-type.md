# Result Type

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fike110/jeffect/0.1.4.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.fike110/jeffect/0.1.4)
[![javadoc](https://javadoc.io/badge2/io.github.fike110/jeffect/0.1.4.svg)](https://javadoc.io/doc/io.github.fike110/jeffect/0.1.4)

The `Result<T>` sealed interface represents the outcome of executing an Effect - either success or failure.

## Interface Methods

### isSuccess / isFailure

```java
boolean isSuccess()
boolean isFailure()
```

Check if the result is successful or failed.

```java
Result<String> result = effect.runSafe();
if (result.isSuccess()) {
    System.out.println(result.get());
}
```

### get

```java
T get()
```

Returns the value or null if failed.

```java
String value = result.get();
```

### getOrThrow

```java
T getOrThrow()
```

Returns the value or throws the contained exception.

```java
String value = result.getOrThrow();
```

### getOrElse

```java
T getOrElse(T defaultValue)
```

Returns the value or a default if failed.

```java
String value = result.getOrElse("default");
```

### optional

```java
Optional<T> optional()
```

Converts to Optional - empty if failed.

```java
Optional<String> optional = result.optional();
```

### getThrowable

```java
Throwable getThrowable()
```

Returns the contained error.

```java
Throwable error = result.getThrowable();
```

### map

```java
<R> Result<R> map(Function<T, R> mapper)
```

Maps the success value to another value.

```java
Result<Integer> mapped = result.map(String::length);
```

### flatMap

```java
<R> Result<R> flatMap(Function<T, Result<R>> mapper)
```

FlatMaps the success value to another Result.

```java
Result<Integer> flatMapped = result.flatMap(v -> Result.success(v * 2));
```

### fold

```java
<R> R fold(Function<Throwable, R> onFailure, Function<T, R> onSuccess)
```

Folds both success and failure cases into a single value.

```java
String text = result.fold(
    err -> "Error: " + err.getMessage(),
    val -> "Success: " + val
);
```

## Subtypes

### Success

Represents a successful result containing a value.

```java
Result<String> success = Results.success("hello");
```

### Failure

Represents a failed result containing an error.

```java
Result<String> failure = Results.failure(new RuntimeException("error"));
```

## Usage with Effects

The `runSafe()` method returns a Result:

```java
Result<User> result = Effects.of(() -> findUser(id)).runSafe();

User user = result.fold(
    err -> User.anonymous(),  // on failure
    user -> user              // on success
);
```
