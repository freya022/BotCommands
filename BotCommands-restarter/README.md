# BotCommands module - Hot restarter
This module enables fast restarts of your bot as you develop it.

When you build changes of your code, it restarts automatically, in the same JVM,
leading to much faster restarts, as it doesn't need to recompile most of the code.

> [!WARNING]
> If you are using Spring, use [`spring-boot-devtools`](https://docs.spring.io/spring-boot/reference/using/devtools.html) instead.

## Installing

### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-restarter</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>
```

### Gradle
```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.freya022:BotCommands-restarter:VERSION")
}
```

### Snapshots

To use the latest, unreleased changes, see [SNAPSHOTS.md](../SNAPSHOTS.md).

## Usage
You can enable the feature by doing so, after which, every build will restart your application.

### Kotlin
```kotlin
fun main(args: Array<out String>) {
    // ...
    BotCommands.create {
        // ...

        @OptIn(ExperimentalRestartApi::class)
        registerRestarter(args) {
            // Optional configuration
        }
    }
}
```

### Java
```java
void main(String[] args) {
    // ...
    BotCommands.create(config -> {
        // ...

        var restarterConfig = RestarterConfig.builder(args)
                // Optional configuration
                .build();
        config.registerModule(restarterConfig);
    });
}
```
