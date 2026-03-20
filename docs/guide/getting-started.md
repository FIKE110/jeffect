# Getting Started

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fike110/jeffect.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.fike110/jeffect)
[![javadoc](https://javadoc.io/badge2/io.github.fike110/jeffect/0.1.3.svg)](https://javadoc.io/doc/io.github.fike110/jeffect/0.1.3)

Welcome to JEffect! This guide will help you get up and running quickly.

## What is JEffect?

JEffect is a functional programming library for Java that provides:

- **Deferred computation** - Effects are lazy and only execute when you want them to
- **Error handling** - Built-in recovery mechanisms that compose elegantly
- **Concurrency** - Parallel execution, racing, and async support
- **Spring integration** - First-class support for Spring Boot applications

## Requirements

- Java 17 or higher
- Maven 3.6+ (or Gradle)

## Installation

Add the dependency to your project:

::: code-group

```xml [Maven]
<dependency>
    <groupId>io.github.fike110</groupId>
    <artifactId>jeffect</artifactId>
    <version>0.1.3</version>
</dependency>
```

```groovy [Gradle]
implementation 'io.github.fike110:jeffect:0.1.3'
```

:::

## Your First Effect

```java
import io.github.fike110.jeffect.Effects;
import io.github.fike110.jeffect.core.Effect;

public class Example {
    public static void main(String[] args) {
        // Create a successful effect
        Effect<String> hello = Effects.success("Hello, World!");
        
        // Execute it
        String result = hello.run();
        
        System.out.println(result); // "Hello, World!"
    }
}
```

## Why Use Effects?

### Before: Nested Try-Catch

```java
public User findUser(Long id) {
    try {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        
        try {
            Order order = orderRepository.findByUserId(user.getId());
            user.setOrder(order);
        } catch (Exception e) {
            // Ignore order errors
        }
        
        return user;
    } catch (UserNotFoundException e) {
        log.warn("User not found: " + id);
        return User.anonymous();
    }
}
```

### After: With JEffect

```java
public Effect<User> findUser(Long id) {
    return Effects.fromOptional(userRepository.findById(id), "User not found")
        .onError(e -> log.warn("User not found: " + id))
        .recover(e -> User.anonymous());
}
```

The effect-based approach:
- Separates **what** to compute from **when** to execute it
- Makes error handling composable
- Enables powerful transformations before execution

## Before vs After: Complete Examples

### Example 1: Database Call with Fallback

**Before (Traditional):**
```java
public User findUser(Long id) {
    try {
        return cache.get("user:" + id)
            .orElseGet(() -> database.findById(id));
    } catch (Exception e) {
        log.error("Failed to fetch user", e);
        return database.findById(id);
    }
}
```

**After (With Effects):**
```java
public Effect<User> findUser(Long id) {
    return Effects.fromOptional(cache.get("user:" + id))
        .recoverWith(e -> Effects.fromOptional(database.findById(id)));
}
```

---

### Example 2: Chained API Calls

**Before (Traditional):**
```java
public OrderConfirmation createOrder(CreateOrderRequest req) {
    try {
        // Validate user
        User user = userService.findById(req.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        // Validate inventory
        List<Item> items = inventoryService.reserve(req.getItems());
        
        // Create order
        Order order = orderService.create(user, items);
        
        // Send confirmation
        emailService.sendConfirmation(user.getEmail(), order);
        
        return new OrderConfirmation(order);
    } catch (UserNotFoundException e) {
        return new OrderConfirmation(null, "User not found");
    } catch (InventoryException e) {
        return new OrderConfirmation(null, "Items unavailable");
    } catch (Exception e) {
        return new OrderConfirmation(null, "Order failed");
    }
}
```

**After (With Effects):**
```java
public Effect<OrderConfirmation> createOrder(CreateOrderRequest req) {
    return Effects.of(() -> userService.findById(req.getUserId()))
        .recoverWith(e -> Effects.fail(new IllegalArgumentException("User not found")))
        .flatMap(user -> Effects.of(() -> inventoryService.reserve(req.getItems()))
            .map(items -> new Order(user, items))
        )
        .flatMap(order -> Effects.of(() -> {
            Order saved = orderService.create(order);
            emailService.sendConfirmation(order.getUser().getEmail(), saved);
            return saved;
        }))
        .map(OrderConfirmation::new)
        .recover(e -> new OrderConfirmation(null, e.getMessage()));
}
```

---

### Example 3: Parallel Data Fetching

**Before (Traditional):**
```java
public UserDashboard getDashboard(Long userId) throws ExecutionException, InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(4);
    
    Future<User> userFuture = executor.submit(() -> userService.findById(userId));
    Future<List<Order>> ordersFuture = executor.submit(() -> orderService.findByUserId(userId));
    Future<List<Notification>> notificationsFuture = executor.submit(() -> notificationService.findByUserId(userId));
    Future<Cart> cartFuture = executor.submit(() -> cartService.findByUserId(userId));
    
    User user = userFuture.get();
    List<Order> orders = ordersFuture.get();
    List<Notification> notifications = notificationsFuture.get();
    Cart cart = cartFuture.get();
    
    executor.shutdown();
    return new UserDashboard(user, orders, notifications, cart);
}
```

**After (With Effects):**
```java
public Effect<UserDashboard> getDashboard(Long userId) {
    List<Effect<?>> effects = List.of(
        Effects.of(() -> userService.findById(userId)),
        Effects.of(() -> orderService.findByUserId(userId)),
        Effects.of(() -> notificationService.findByUserId(userId)),
        Effects.of(() -> cartService.findByUserId(userId))
    );
    
    return Effects.parallel(effects).map(results -> 
        new UserDashboard(
            (User) results.get(0),
            (List<Order>) results.get(1),
            (List<Notification>) results.get(2),
            (Cart) results.get(3)
        )
    );
}
```

---

### Example 4: Retry Logic

**Before (Traditional):**
```java
public Data fetchData(String key) {
    int maxRetries = 3;
    int attempt = 0;
    Exception lastException = null;
    
    while (attempt < maxRetries) {
        try {
            return api.fetch(key);
        } catch (Exception e) {
            lastException = e;
            attempt++;
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
    }
    
    throw new RuntimeException("Failed after " + maxRetries + " attempts", lastException);
}
```

**After (With Effects):**
```java
public Effect<Data> fetchData(String key) {
    return Effects.of(() -> api.fetch(key))
        .retry(3);
}
```

---

### Example 5: Optional to Effect

**Before (Traditional):**
```java
public User findAdmin() {
    Optional<User> admin = userRepository.findByRole("ADMIN");
    
    if (admin.isPresent()) {
        return admin.get();
    }
    
    // Try moderator
    Optional<User> moderator = userRepository.findByRole("MODERATOR");
    if (moderator.isPresent()) {
        return moderator.get();
    }
    
    // Try any user
    return userRepository.findAll().stream()
        .findFirst()
        .orElseThrow(() -> new UserNotFoundException("No users found"));
}
```

**After (With Effects):**
```java
public Effect<User> findAdmin() {
    return Effects.firstSuccessOf(
        Effects.fromOptional(userRepository.findByRole("ADMIN")),
        Effects.fromOptional(userRepository.findByRole("MODERATOR")),
        Effects.fromOptional(userRepository.findAll().stream().findFirst())
    );
}
```

---

### Example 6: Validation Chain

**Before (Traditional):**
```java
public ValidationResult validateOrder(Order order) {
    List<String> errors = new ArrayList<>();
    
    if (order.getItems() == null || order.getItems().isEmpty()) {
        errors.add("Order must have items");
    }
    
    if (order.getUserId() == null) {
        errors.add("User is required");
    } else {
        try {
            User user = userService.findById(order.getUserId());
            if (!user.isActive()) {
                errors.add("User account is not active");
            }
            if (user.isBanned()) {
                errors.add("User is banned");
            }
        } catch (Exception e) {
            errors.add("User not found");
        }
    }
    
    if (order.getTotal() == null || order.getTotal() <= 0) {
        errors.add("Invalid order total");
    }
    
    return errors.isEmpty() 
        ? ValidationResult.valid() 
        : ValidationResult.invalid(errors);
}
```

**After (With Effects):**
```java
public Effect<ValidationResult> validateOrder(Order order) {
    List<String> errors = new ArrayList<>();
    
    Effect<ValidationResult> effect = Effects.success(true)
        .filter(__ -> order.getItems() != null && !order.getItems().isEmpty(),
            () -> new IllegalArgumentException("Order must have items"))
        .filter(__ -> order.getUserId() != null,
            () -> new IllegalArgumentException("User is required"))
        .flatMap(__ -> Effects.of(() -> userService.findById(order.getUserId()))
            .filter(User::isActive, () -> new IllegalArgumentException("User not active"))
            .filter(u -> !u.isBanned(), () -> new IllegalArgumentException("User banned"))
            .map(__ -> true)
        )
        .filter(__ -> order.getTotal() != null && order.getTotal() > 0,
            () -> new IllegalArgumentException("Invalid total"));
    
    return effect
        .map(__ -> ValidationResult.valid())
        .recover(e -> ValidationResult.invalid(List.of(e.getMessage())));
}
```

## Next Steps

- [Core Concepts](/guide/core-concepts) - Understand the Effect types
- [Creating Effects](/guide/creating-effects) - Learn all ways to create effects
- [Transforming Effects](/guide/transforming) - Map, flatMap, and more
- [Error Handling](/guide/error-handling) - Recovery patterns
