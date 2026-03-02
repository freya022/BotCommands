[bc-module-maven-central-shield]: https://img.shields.io/maven-central/v/io.github.freya022/BotCommands-restarter?label=Maven%20central&logo=apachemaven&versionPrefix=3
[bc-module-maven-central-link]: https://central.sonatype.com/artifact/io.github.freya022/BotCommands-restarter

# BotCommands module - Hot restarter
This module enables fast restarts of your bot as you develop it.

When you build changes of your code, it restarts automatically, in the same JVM,
leading to much faster restarts, as it doesn't need to recompile most of the code.

> [!WARNING]
> If you are using Spring, use [`spring-boot-devtools`](https://docs.spring.io/spring-boot/reference/using/devtools.html) instead.

## Installing
[![BotCommands-restarter on maven central][bc-module-maven-central-shield] ][bc-module-maven-central-link]

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

You should minimize the amount of code executed before calling `BotCommands.create`,
as it will run twice on startup, then everytime it is restarted.

> [!IMPORTANT]
> You must only use this feature during development, here are a few ways to do so:
> - Using a program argument like `--dev` then reading it from `args`
> - Using a configuration file with a `IS_DEV` property
> - Using an environment variable

### Kotlin
```kotlin
fun main(args: Array<out String>) {
    // ...
    BotCommands.create {
        // ...

        // You should enable this only during development
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

        // You should enable this only during development
        var restarterConfig = RestarterConfig.builder(args)
                // Optional configuration
                .build();
        config.registerModule(restarterConfig);
    });
}
```
