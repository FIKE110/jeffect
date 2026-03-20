---
layout: home

hero:
  name: "JEffect"
  text: "Functional Effects for Java"
  tagline: "A powerful effect library with deferred computation and elegant error handling"
  actions:
    - theme: brand
      text: Get Started
      link: /guide/getting-started
    - theme: alt
      text: View API
      link: /api/effects-factory

features:
  - title: Deferred Execution
    details: Effects are lazy by default - they only run when you explicitly execute them. Perfect for building pipelines.
  - title: Error Handling
    details: Built-in error recovery with recover, recoverWith, and powerful composition operators.
  - title: Concurrency
    details: Parallel execution, racing effects, and async support out of the box.
  - title: Type Safety
    details: Full generic type safety with Java's type system. No runtime type errors.
  - title: Spring Integration
    details: First-class support for Spring Boot with dependency injection and async execution.
  - title: Functional Style
    details: Map, flatMap, and monadic composition for elegant, readable code.
  - title: Reactive State
    details: Observable state with automatic change notification. Perfect for UI state and real-time data.
---

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fike110/jeffect/0.1.4.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.fike110/jeffect/0.1.4)
[![javadoc](https://javadoc.io/badge2/io.github.fike110/jeffect/0.1.4.svg)](https://javadoc.io/doc/io.github.fike110/jeffect/0.1.4)

## Quick Example

### Effects - Deferred Computation

```java
import io.github.fike110.jeffect.Effects;
import io.github.fike110.jeffect.core.Effect;

// Create and transform
Effect<String> result = Effects.of(() -> fetchUser(id))
    .map(user -> user.getName())
    .recover(e -> "Guest");

// Execute
String name = result.run();
```

### State - Reactive Values

```java
import io.github.fike110.jeffect.state.State;
import io.github.fike110.jeffect.state.Watcher;

// Observable state
State<Integer> count = State.of(0);

// Subscribe to changes
count.subscribe(value -> System.out.println("Count: " + value));

// Derived state (auto-updates)
State<String> label = count.map(n -> "Count: " + n);

// Auto-run when state changes
Watcher.watch(count, () -> System.out.println("Changed: " + count.get()));

count.set(1); // prints "Count: 1", "Changed: 1"
count.set(2); // prints "Count: 2", "Changed: 2"
```

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.fike110</groupId>
    <artifactId>jeffect</artifactId>
    <version>0.1.4</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.fike110:jeffect:0.1.4'
```

## API Reference

For detailed API documentation, see:

- [Effects Factory](/api/effects-factory) - Factory methods for creating effects
- [Effect Interface](/api/effect-interface) - Core Effect interface methods
- [Result Type](/api/result-type) - Result wrapper for effect execution
- [State Module](/api/state-type) - Reactive observable state management
