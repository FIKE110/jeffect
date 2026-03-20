# State Module

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fike110/jeffect/0.1.4.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.fike110/jeffect/0.1.4)
[![javadoc](https://javadoc.io/badge2/io.github.fike110/jeffect/0.1.4.svg)](https://javadoc.io/doc/io.github.fike110/jeffect/0.1.4)

The `State` module provides reactive, observable state management for JEffect. It enables automatic propagation of changes to dependent computations.

## Why State?

### The Problem with Plain Variables

With plain variables, Effects don't automatically re-run when values change:

```java
int count = 0;

Effect<String> effect = Effects.of(() -> "Count: " + count);

effect.run(); // "Count: 0" ✅

count = 1;    // Variable changed...
effect.run(); // ...but you must call it manually ❌
```

### The State Solution

`State<T>` wraps a variable and notifies subscribers automatically:

```java
State<Integer> count = State.of(0);

// Automatically re-runs when count changes
count.subscribe(value -> System.out.println("Count changed: " + value));

count.set(1); // prints "Count changed: 1" ✅
count.set(2); // prints "Count changed: 2" ✅
```

---

## Core Concepts

### State<T> Interface

```java
public interface State<T> {
    T get();                           // Get current value
    void set(T value);                 // Set new value
    void subscribe(Consumer<T> listener);   // Subscribe to changes
    void unsubscribe(Consumer<T> listener); // Unsubscribe
}
```

### Implementations

| Class | Thread-Safe | Use Case |
|-------|-------------|----------|
| `SimpleState<T>` | No | Single-threaded, UI applications |
| `AtomicState<T>` | Yes | Multi-threaded, concurrent access |

---

## Creating State

### Basic Creation

```java
import io.github.fike110.jeffect.state.State;

// Simple state (not thread-safe)
State<Integer> counter = State.of(0);
State<String> name = State.of("Alice");

// Atomic state (thread-safe)
State<Integer> atomicCounter = State.atomically(0);
```

### Initial Values

```java
State<List<String>> items = State.of(new ArrayList<>());
State<User> currentUser = State.of(null);
State<Status> status = State.atomically(Status.IDLE);
```

---

## Subscribing to Changes

### Basic Subscription

```java
State<Integer> count = State.of(0);

count.subscribe(value -> {
    System.out.println("Count is now: " + value);
});

count.set(1); // prints "Count is now: 1"
count.set(5); // prints "Count is now: 5"
```

### Unsubscribe

```java
Consumer<Integer> listener = value -> System.out.println(value);
count.subscribe(listener);

// Later...
count.unsubscribe(listener);
```

### Multiple Subscribers

```java
State<String> username = State.of("");

username.subscribe(value -> updateHeader(value));
username.subscribe(value -> saveToCache(value));
username.subscribe(value -> logUserAction(value));
```

---

## Derived State

Create computed values that automatically update:

### map - Transform State

```java
State<Integer> count = State.of(5);
State<String> label = count.map(n -> "Count: " + n);
State<Integer> doubled = count.map(n -> n * 2);

System.out.println(count.get());  // 5
System.out.println(label.get());  // "Count: 5"
System.out.println(doubled.get()); // 10

count.set(10);

System.out.println(label.get());  // "Count: 10" (auto-updated!)
System.out.println(doubled.get()); // 20 (auto-updated!)
```

### flatMap - Chain State

```java
State<Integer> userId = State.of(1);
State<User> user = userId.flatMap(id -> State.of(database.findUser(id)));
```

### filter - Conditional State

```java
State<Integer> score = State.of(75);
State<Integer> passing = score.filter(
    s -> s >= 60,    // predicate
    0                // default when filtered out
);

System.out.println(passing.get()); // 75

score.set(50);
System.out.println(passing.get()); // 0 (filtered out!)

score.set(85);
System.out.println(passing.get()); // 85 (passes filter!)
```

---

## Integration with Effect

### Using Watcher

The `Watcher` class bridges State with Effect:

```java
import io.github.fike110.jeffect.state.State;
import io.github.fike110.jeffect.state.Watcher;

// Watch a single state
State<Integer> count = State.of(0);

Watcher.watch(count, () -> {
    System.out.println("Effect triggered! Count is now: " + count.get());
});

count.set(1); // Effect runs automatically
count.set(2); // Effect runs again
```

### Watching Multiple States

```java
State<String> username = State.of("");
State<Boolean> isLoggedIn = State.of(false);

// Re-runs when ANY watched state changes
Watcher.watch(username, isLoggedIn, () -> {
    if (isLoggedIn.get()) {
        showDashboard(username.get());
    } else {
        showLogin();
    }
});
```

### With Consumer

```java
State<String> searchQuery = State.of("");

Watcher.watch(searchQuery, query -> {
    if (!query.isEmpty()) {
        search(query);
    }
});
```

---

## Real-World Use Cases

### 1. UI State Management

```java
State<String> currentView = State.of("home");
State<User> currentUser = State.of(null);
State<List<Notification>> notifications = State.of(List.of());

Watcher.watch(currentView, () -> renderView(currentView.get()));
Watcher.watch(currentUser, () -> updateHeader(currentUser.get()));
Watcher.watch(notifications, () -> showBadge(notifications.get().size()));

// Navigation
currentView.set("profile");
currentUser.set(fetchUser());
notifications.set(List.of(new Notification("New message!")));
```

### 2. Form Validation

```java
State<String> email = State.of("");
State<String> password = State.of("");
State<Boolean> isValid = State.of(false);

email.subscribe(e -> validateForm());
password.subscribe(p -> validateForm());

void validateForm() {
    boolean valid = isValidEmail(email.get()) && isValidPassword(password.get());
    isValid.set(valid);
    submitButton.setEnabled(valid);
}
```

### 3. Shopping Cart

```java
State<List<CartItem>> cart = State.of(new ArrayList<>());
State<Double> total = cart.map(items -> 
    items.stream().mapToDouble(CartItem::getPrice).sum()
);
State<Integer> itemCount = cart.map(List::size);

cart.set(List.of(new CartItem("Laptop", 999.99)));
System.out.println(total.get());    // 999.99
System.out.println(itemCount.get()); // 1
```

### 4. Real-time Data Sync

```java
State<ServerStatus> serverStatus = State.atomically(ServerStatus.UNKNOWN);

// Multiple components react to status changes
serverStatus.subscribe(status -> updateConnectionIndicator(status));
serverStatus.subscribe(status -> logServerHealth(status));
serverStatus.subscribe(status -> updateDashboard(status));

// When server status changes from WebSocket event
websocket.onMessage(event -> {
    serverStatus.set(parseStatus(event));
});
```

### 5. Feature Flags

```java
State<Boolean> darkMode = State.of(false);
State<Boolean> newUI = State.of(true);

darkMode.subscribe(enabled -> applyTheme(enabled ? DARK : LIGHT));
newUI.subscribe(enabled -> showNewInterface(enabled));

// Admin toggles
adminPanel.onToggle("dark_mode", () -> darkMode.set(!darkMode.get()));
adminPanel.onToggle("new_ui", () -> newUI.set(!newUI.get()));
```

### 6. Concurrency-Safe Counters

```java
AtomicState<Integer> counter = (AtomicState<Integer>) State.atomically(0);

// Thread-safe atomic operations
counter.updateAndGet(n -> n + 1);
counter.updateAndGet(n -> n * 2);

// Compare and set
counter.compareAndSet(5, 10);

// Multiple threads can safely update
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    executor.submit(() -> counter.updateAndGet(n -> n + 1));
}
```

---

## Thread Safety

### SimpleState (Not Thread-Safe)

```java
// Use in single-threaded contexts like UI
State<String> inputField = State.of("");
inputField.set("Hello"); // Only from UI thread
```

### AtomicState (Thread-Safe)

```java
// Use when multiple threads access state
AtomicState<List<String>> sharedList = (AtomicState<List<String>>) State.atomically(new ArrayList<>());

// Safe from multiple threads
producerThread.start();  // sharedList.set(...)
consumerThread.start();  // sharedList.subscribe(...)
```

---

## Comparison with Effect

| Aspect | Effect<T> | State<T> |
|--------|-----------|----------|
| **Purpose** | Deferred computation | Observable variable |
| **Evaluation** | Lazy (runs on demand) | Eager (notifies on change) |
| **Error Handling** | Built-in with `recover` | No built-in errors |
| **Thread-Safety** | Depends on usage | `AtomicState` for threads |
| **Best For** | Composable operations | Reactive state |

### When to Use Each

```java
// Use Effect for computations
Effect<String> userData = Effects.of(() -> fetchUser(id))
    .map(User::getName)
    .recover(e -> "Anonymous");

// Use State for reactive values
State<Integer> clickCount = State.of(0);
clickCount.subscribe(count -> analytics.track(count));
```

---

## Best Practices

### 1. Prefer Immutability

```java
// Good: Create new list
cart.set(List.of(new Item("Book", 19.99)));

// Avoid: Mutating existing list
cart.get().add(new Item("Book", 19.99));
cart.set(cart.get());
```

### 2. Use Derived State

```java
// Good: Derived state auto-updates
State<List<String>> items = State.of(List.of());
State<Integer> count = items.map(List::size);

// Avoid: Manual synchronization
State<Integer> count = State.of(0);
items.subscribe(list -> count.set(list.size()));
```

### 3. Clean Up Subscriptions

```java
Consumer<String> listener = value -> doSomething(value);
state.subscribe(listener);

// When done:
state.unsubscribe(listener);

// Or use scoped subscriptions
try (var scope = new SubscriptionScope()) {
    scope.subscribe(state, listener);
} // Automatically unsubscribes
```

### 4. Thread-Safety for Shared State

```java
// Multiple threads = use AtomicState
State<List<Request>> pendingRequests = State.atomically(new ArrayList<>());

// Single thread = SimpleState is fine
State<Point> cursorPosition = State.of(new Point(0, 0));
```

---

## API Reference

### State Interface Methods

| Method | Description |
|--------|-------------|
| `T get()` | Returns current value |
| `void set(T value)` | Sets new value, notifies subscribers |
| `void subscribe(Consumer<T>)` | Registers change listener |
| `void unsubscribe(Consumer<T>)` | Removes listener |
| `<U> State<U> map(Function<T,U>)` | Creates derived state |
| `<U> State<U> flatMap(Function<T,State<U>>)` | Creates chained state |
| `State<T> filter(Predicate<T>, T)` | Creates filtered state |
| `int subscriberCount()` | Returns number of subscribers |

### AtomicState Additional Methods

| Method | Description |
|--------|-------------|
| `boolean compareAndSet(T, T)` | Atomic compare-and-set |
| `T updateAndGet(UnaryOperator<T>)` | Atomic update and get |
| `T getAndUpdate(UnaryOperator<T>)` | Get and update atomically |

### Watcher Methods

| Method | Description |
|--------|-------------|
| `Watcher.watch(State<T>, Runnable)` | Watch single state |
| `Watcher.watch(State<T>, Consumer<T>)` | Watch with value consumer |
| `Watcher.watch(State<T>, State<U>, Runnable)` | Watch multiple states |
| `DerivedState<T,T> Watcher.derive(State<T>)` | Create identity derivation |
