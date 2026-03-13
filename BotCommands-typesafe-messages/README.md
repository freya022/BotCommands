[bc-module-maven-central-shield]: https://img.shields.io/maven-central/v/io.github.freya022/BotCommands-typesafe-messages-core?label=Maven%20central&logo=apachemaven&versionPrefix=3
[bc-module-maven-central-link]: https://central.sonatype.com/artifact/io.github.freya022/BotCommands-typesafe-messages-core

# BotCommands module - Typesafe messages
This module allows you to define functions which retrieves (optionally localized) messages,
without having to implement anything, providing a few benefits:
- Checks if the templates exists in your bundles, ensuring your content can always be displayed
- Checks if function parameters exists in your template's arguments, meaning all parameters map to an argument
- Checks if template arguments map to function parameters, so all arguments have values

⇒ No more magic strings in your command's code! Template paths and argument names are centralized and checked when your bot loads,
improving safety and making mistakes/regressions appear immediately.

## Usage

See ["Responding with type-safe messages"](https://bc.freya02.dev/3.X/using-botcommands/localization/typesafe-messages/) in the wiki.

## Installation
[![BotCommands-typesafe-messages-core on maven central][bc-module-maven-central-shield] ][bc-module-maven-central-link]

There are different dependencies based on which dependency injection you use:

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
