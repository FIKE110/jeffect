# Spring Boot Integration

JEffect integrates seamlessly with Spring Boot.

## Service Layer

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    public Effect<User> findById(Long id) {
        return Effects.fromOptional(
            userRepository.findById(id),
            "User not found: " + id
        );
    }
    
    public Effect<User> createUser(CreateUserRequest req) {
        return Effects.of(() -> {
            User user = new User(req.name(), req.email());
            return userRepository.save(user);
        });
    }
    
    public Effect<User> createUserWithEmail(CreateUserRequest req) {
        return Effects.of(() -> {
            User user = new User(req.name(), req.email());
            return userRepository.save(user);
        }).flatMap(saved -> 
            Effects.of(() -> {
                emailService.sendWelcome(saved);
                return saved;
            })
        );
    }
}
```

## Controller Layer

### Option 1: Run at Boundary

```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .recover(e -> ResponseEntity.notFound().build())
            .run()
            .toOptional()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

### Option 2: Return Effect Directly

```java
@GetMapping("/{id}")
public Effect<ResponseEntity<User>> getUser(@PathVariable Long id) {
    return userService.findById(id)
        .map(ResponseEntity::ok)
        .recover(e -> ResponseEntity.notFound().build());
}
```

## Async with @Async

```java
@Service
public class NotificationService {
    
    private final EmailEffects emailEffects;
    
    @Async
    public CompletableFuture<Void> sendWelcomeAsync(String email, String name) {
        return emailEffects.sendGreeting(email, name)
            .recover(e -> {
                log.warn("Failed to send greeting", e);
                return null;
            })
            .runFuture();
    }
}
```

## Creating Effect Beans

### Simple Services (Recommended)

Inject services and use them in effects:

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final UserService userService;
    private final InventoryService inventoryService;
    
    public Effect<Order> createOrder(CreateOrderRequest req) {
        return userService.findById(req.userId())
            .flatMap(user -> 
                inventoryService.reserveItems(req.items())
            )
            .map(items -> new Order(req.userId(), items));
    }
}
```

### Effect Builders

For complex effects with many dependencies:

```java
@Component
public class UserEffectBuilder {
    
    private final UserRepository userRepository;
    private final CacheService cacheService;
    
    public Effect<User> findById(Long id) {
        return Effects.of(() -> cacheService.getUser(id))
            .recoverWith(e -> 
                Effects.of(() -> userRepository.findById(id))
            );
    }
}
```

## Complete Examples

### User Registration Flow

```java
@Service
@RequiredArgsConstructor
public class RegistrationService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationService verificationService;
    
    public Effect<User> register(RegistrationRequest req) {
        // Validate input
        return Effects.of(() -> req.validate())
            // Check email not taken
            .flatMap(__ -> Effects.fromOptional(
                userRepository.findByEmail(req.getEmail()),
                "Email already exists"
            ))
            // Create user
            .flatMap(__ -> Effects.of(() -> {
                User user = new User(req.getName(), req.getEmail());
                return userRepository.save(user);
            }))
            // Send verification
            .flatMap(user -> Effects.of(() -> {
                String code = verificationService.generate(user);
                emailService.sendVerification(user.getEmail(), code);
                return user;
            }))
            // Recover on any failure
            .recoverWith(e -> {
                log.error("Registration failed", e);
                return Effects.fail(new RegistrationException(e.getMessage()));
            });
    }
}
```

### Shopping Cart Checkout

```java
@Service
@RequiredArgsConstructor
public class CheckoutService {
    
    private final CartRepository cartRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    
    public Effect<Order> checkout(Long userId, PaymentInfo paymentInfo) {
        // Get cart
        return Effects.fromOptional(cartRepository.findByUserId(userId), "Cart not found")
            .filter(cart -> !cart.isEmpty(), () -> new IllegalArgumentException("Cart is empty"))
            // Reserve inventory
            .flatMap(cart -> inventoryService.reserve(cart.getItems())
                .map(reserved -> Pair.of(cart, reserved))
            )
            // Process payment
            .flatMap(pair -> Effects.of(() -> 
                paymentService.charge(paymentInfo, pair.second().getTotal())
            ).map(payment -> Pair.of(pair.first(), payment))
            )
            // Create order
            .flatMap(pair -> Effects.of(() -> {
                Order order = new Order(pair.first().getUserId(), pair.first().getItems(), pair.second());
                return orderRepository.save(order);
            }))
            // Clear cart
            .flatMap(order -> Effects.of(() -> {
                cartRepository.clear(userId);
                return order;
            }))
            // Send confirmation
            .flatMap(order -> Effects.of(() -> {
                emailService.sendOrderConfirmation(order.getUserId(), order);
                return order;
            }))
            // Timeout and rollback
            .recoverWith(e -> {
                log.error("Checkout failed for user " + userId, e);
                return Effects.fail(new CheckoutException(e.getMessage()));
            });
    }
}
```

### Caching Layer

```java
@Service
public class CachedUserService {
    
    private final UserRepository userRepository;
    private final Cache cache;
    
    public Effect<User> findById(Long id) {
        // Try cache first
        return Effects.fromOptional(cache.get("user:" + id))
            .recoverWith(e -> 
                // Fallback to database
                Effects.of(() -> userRepository.findById(id))
                    .flatMap(user -> {
                        // Store in cache for next time
                        return Effects.of(() -> {
                            cache.put("user:" + id, user);
                            return user;
                        });
                    })
            );
    }
    
    public Effect<User> findByEmail(String email) {
        return Effects.of(() -> userRepository.findByEmail(email))
            .recoverWith(e -> Effects.fail(e))
            .cache("user:email:" + email);
    }
}
```

## Transaction Support

Wrap effects in transactions:

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final TransactionTemplate txTemplate;
    private final OrderRepository orderRepository;
    
    public Effect<Order> createOrder(Order order) {
        return Effects.suspend(() -> 
            txTemplate.execute(status -> {
                try {
                    return Effects.success(orderRepository.save(order));
                } catch (Exception e) {
                    return Effects.fail(e);
                }
            })
        );
    }
}
```

## Error Handling in Controllers

### Return 404 on Not Found

```java
@GetMapping("/{id}")
public Effect<ResponseEntity<User>> getUser(@PathVariable Long id) {
    return userService.findById(id)
        .map(ResponseEntity::ok)
        .recover(e -> ResponseEntity.notFound().build());
}
```

### Fold Pattern

```java
@GetMapping("/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    Result<User> result = userService.findById(id).runSafe();
    
    return result.<ResponseEntity<User>>fold(
        error -> ResponseEntity.status(500).build(),
        user -> ResponseEntity.ok(user)
    );
}
```

### With Custom Error Response

```java
@GetMapping("/{id}")
public Effect<ApiResponse<User>> getUser(@PathVariable Long id) {
    return userService.findById(id)
        .map(user -> ApiResponse.success(user))
        .recover(e -> ApiResponse.error(e.getMessage()));
}

public record ApiResponse<T>(T data, String error, boolean success) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null, true);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(null, message, false);
    }
}
```

## Best Practices

1. **Keep effects small** - Create in service methods, execute in controllers
2. **Use `recover` for not-found** - Return meaningful defaults or 404s
3. **Use async for emails** - Don't block HTTP requests
4. **Return `Effect<T>` from services** - Keep execution at the boundary
5. **Use caching** - Effects are great for caching layers

## Dependency Injection

Effects capture dependencies through closures:

```java
@Service
public class UserService {
    
    private final UserRepository repo;  // Injected by Spring
    
    public Effect<User> findById(Long id) {
        return Effects.of(() -> repo.findById(id));  // repo is captured
    }
}
```

The repository is injected once, then captured in the effect's closure when `findById` is called.
