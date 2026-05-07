[bc-module-maven-central-shield]: https://img.shields.io/maven-central/v/io.github.freya022/BotCommands-autocomplete-caffeine?label=Maven%20central&logo=apachemaven&versionPrefix=3
[bc-module-maven-central-link]: https://central.sonatype.com/artifact/io.github.freya022/BotCommands-autocomplete-caffeine

# BotCommands add-on - Caffeine-backed autocomplete cache 
Provides a Caffeine-backed autocomplete cache.
This isn't a module to be registered, this dependency only provides more features for the `BotCommands-commands-app` module. 

## Installation
[![BotCommands-autocomplete-caffeine on maven central][bc-module-maven-central-shield] ][bc-module-maven-central-link]
### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-autocomplete-caffeine</artifactId>
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
    implementation("io.github.freya022:BotCommands-autocomplete-caffeine:VERSION")
}
```

### Snapshots

To use the latest, unreleased changes, see [SNAPSHOTS.md](../SNAPSHOTS.md).

## Usage

See ["Autocomplete handlers - Caching"](https://bc.freya02.dev/3.X/using-commands/application-commands/slash-commands/autocomplete-handlers/#caching) on the wiki.
