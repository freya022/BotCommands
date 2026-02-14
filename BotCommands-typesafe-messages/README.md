# BotCommands module - Typesafe messages
This module allows you to define functions which retrieves translated messages,
without having to implement anything, alongside a few other benefits:
- Checks if the templates exists in your bundles, ensuring your content can always be displayed
- Checks if function parameters exists in your template's arguments, meaning all parameters map to an argument
- Checks if template arguments map to function parameters, so all arguments have values
- Removes the need for magic strings (for the arguments), improving type safety and making regressions appear immediately

## Usage

See ["Responding with type-safe messages"](https://bc.freya02.dev/3.X/using-botcommands/localization/typesafe-messages/) in the wiki.

## Installation
There are different dependencies based on which dependency injection you use:

![](https://img.shields.io/maven-central/v/io.github.freya022/BotCommands-typesafe-messages-core?versionPrefix=3)

<details>
<summary>Built-in</summary>

### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-typesafe-messages-core</artifactId>
    <version>VERSION</version>
  </dependency>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-typesafe-messages-bc</artifactId>
    <version>VERSION</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

### Gradle
```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.freya022:BotCommands-typesafe-messages-core:VERSION")
    runtimeOnly("io.github.freya022:BotCommands-typesafe-messages-bc:VERSION")
}
```

</details>

<details>
<summary>Spring</summary>

### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-typesafe-messages-core</artifactId>
    <version>VERSION</version>
  </dependency>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-typesafe-messages-spring</artifactId>
    <version>VERSION</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

### Gradle
```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.freya022:BotCommands-typesafe-messages-core:VERSION")
    runtimeOnly("io.github.freya022:BotCommands-typesafe-messages-spring:VERSION")
}
```

</details>

### Snapshots

To use the latest, unreleased changes, see [SNAPSHOTS.md](../SNAPSHOTS.md).
