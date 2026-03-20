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
---

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fike110/jeffect.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.fike110/jeffect)
[![javadoc](https://javadoc.io/badge2/io.github.fike110/jeffect/0.1.3.svg)](https://javadoc.io/doc/io.github.fike110/jeffect/0.1.3)

## Quick Example

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

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.fike110</groupId>
    <artifactId>jeffect</artifactId>
    <version>0.1.3</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.fike110:jeffect:0.1.3'
```

## API Reference

For detailed API documentation, see:

- [Effects Factory](/api/effects-factory) - Factory methods for creating effects
- [Effect Interface](/api/effect-interface) - Core Effect interface methods
- [Result Type](/api/result-type) - Result wrapper for effect execution
