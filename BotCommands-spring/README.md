[bc-module-maven-central-shield]: https://img.shields.io/maven-central/v/io.github.freya022/BotCommands-spring?label=Maven%20central&logo=apachemaven&versionPrefix=3
[bc-module-maven-central-link]: https://central.sonatype.com/artifact/io.github.freya022/BotCommands-spring

# BotCommands module - Spring Boot support
This module enables you to use Spring Boot with this framework.

> [!NOTE]
> While this is built against Spring Boot 3, Spring Boot 4 is supported.

## Installation
[![BotCommands-spring on maven central][bc-module-maven-central-shield] ][bc-module-maven-central-link]
### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-spring</artifactId>
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
    implementation("io.github.freya022:BotCommands-spring:VERSION")
}
```

### Snapshots

To use the latest, unreleased changes, see [SNAPSHOTS.md](../SNAPSHOTS.md).

## Usage

To get started, read [the wiki](https://bc.freya02.dev/3.X/setup/getting-started-spring-boot/).
