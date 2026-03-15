# Combining Effects

Combine multiple effects into one.

## sequence

Convert a list of effects into an effect of a list:

```java
List<Effect<User>> userEffects = List.of(
    Effects.of(() -> repo.findById(1)),
    Effects.of(() -> repo.findById(2)),
    Effects.of(() -> repo.findById(3))
);

Effect<List<User>> allUsers = Effects.sequence(userEffects);
List<User> users = allUsers.run();
```

## traverse

Map a function over a list and sequence the results:

```java
List<Long> userIds = List.of(1L, 2L, 3L);

Effect<List<User>> users = Effects.traverse(userIds, id ->
    Effects.of(() -> repo.findById(id))
);
```

This is equivalent to:

```java
List<Effect<User>> effects = userIds.stream()
    .map(id -> Effects.of(() -> repo.findById(id)))
    .collect(Collectors.toList());
    
Effect<List<User>> result = Effects.sequence(effects);
```

## parallel

Execute effects in parallel:

```java
List<Effect<User>> effects = List.of(
    Effects.of(() -> serviceA.getUser(1)),
    Effects.of(() -> serviceB.getUser(2)),
    Effects.of(() -> serviceC.getUser(3))
);

Effect<List<User>> parallel = Effects.parallel(effects);
```

All effects run concurrently. Results are collected when all complete.

## race

Return the first effect to complete (winner's result):

```java
Effect<String> winner = Effects.race(
    Effects.of(() -> fastService.getData()),
    Effects.of(() -> slowService.getData())
);
```

The loser is cancelled when the winner completes.

## firstSuccessOf

Return the first effect that succeeds:

```java
Effect<String> result = Effects.firstSuccessOf(
    Effects.of(() -> cache.get(key)),
    Effects.of(() -> database.find(key)),
    Effects.of(() -> api.fetch(key))
);
```

## iff

Conditional effect based on boolean:

```java
Effect<String> result = Effects.iff(
    Effects.success(isAdmin),
    Effects.success("Admin Panel"),
    Effects.success("User Dashboard")
);
```

## when

Conditionally execute an effect:

```java
Effect<Optional<User>> result = Effects.when(
    userExists,
    Effects.of(() -> findUser(id))
);
```

## using

Resource management (try-with-resources pattern):

```java
Effect<String> content = Effects.using(
    Effects.of(() -> new FileInputStream("file.txt")),
    stream -> Effects.of(() -> readAll(stream)),
    stream -> Effects.of(() -> stream.close())
);
```

## Summary

| Method | Description |
|--------|-------------|
| sequence | List of Effects to Effect of List |
| traverse | Map and sequence |
| parallel | Execute concurrently |
| race | First to complete wins |
| firstSuccessOf | First successful wins |
| iff | If-then-else for effects |
| when | Conditional effect |
| using | Resource management |
