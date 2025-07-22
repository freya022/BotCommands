[bc-module-maven-central-shield]: https://img.shields.io/maven-central/v/io.github.freya022/BotCommands-core?label=Maven%20central&logo=apachemaven&versionPrefix=3
[bc-module-maven-central-link]: https://central.sonatype.com/artifact/io.github.freya022/BotCommands-core

# BotCommands module - Core
This module provides the core set of features required to start making your bot.

## Installation
[![BotCommands-core on maven central][bc-module-maven-central-shield] ][bc-module-maven-central-link]
### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-core</artifactId>
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
    implementation("io.github.freya022:BotCommands-core:VERSION")
}
```

Alternatively, you can use Jitpack to use **snapshot** versions,
you can refer to [the JDA wiki](https://jda.wiki/using-jda/using-new-features/) for more information.
