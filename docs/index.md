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

## Quick Example

```java
import com.github.fike110.jeffect.Effects;
import com.github.fike110.jeffect.core.Effect;

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
    <groupId>com.github.fike110</groupId>
    <artifactId>jeffect</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.github.fike110:jeffect:1.0.0'
```
