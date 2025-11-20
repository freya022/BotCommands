[bc-module-maven-central-shield]: https://img.shields.io/maven-central/v/io.github.freya022/BotCommands-method-accessors-classfile?label=Maven%20central&logo=apachemaven&versionPrefix=3
[bc-module-maven-central-link]: https://central.sonatype.com/artifact/io.github.freya022/BotCommands-method-accessors-classfile

# BotCommands module - Method accessors
This module provides abstractions to call user methods.

## Installation
[![BotCommands-method-accessors-classfile on maven central][bc-module-maven-central-shield] ][bc-module-maven-central-link]

You can optionally install this dependency,
it requires Java 24+ and can be enabled with `BotCommand.preferClassFileAccessors()`.

### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-method-accessors-classfile</artifactId>
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
    implementation("io.github.freya022:BotCommands-method-accessors-classfile:VERSION")
}
```

### Snapshots

Alternatively, you can use Jitpack to use **snapshot** versions,
you can refer to [the JDA wiki](https://jda.wiki/using-jda/using-new-features/) for more information.

> [!NOTE]
> As this is a submodule, the group ID is `io.github.freya022.BotCommands`.

## Implementations

### kotlin-reflect
This is the default implementation, as is what the framework originally used,
it only delegates to `callSuspendBy` with pairs of parameter -> value.

### ClassFile-based
This newer implementation takes advantage of [hidden classes](https://www.baeldung.com/java-hidden-classes) and the [Class-File API](https://openjdk.org/jeps/484),
which, for each function, generates a hidden class with instructions optimized to directly call the user method.

This allows for shorter stack traces in exceptions and the debugger, no `InvocationTargetException`s, and better performance.

### Stack trace comparison

#### kotlin-reflect

<img alt="Stack trace of kotlin-reflect call" src="./assets/stack-trace-kotlin-reflect.avif" width="600"/>

#### ClassFile

<img alt="Stack trace of custom caller" src="./assets/stack-trace-classfile.avif" width="600"/>

### Performance comparison

Performance numbers from [MethodAccessorBenchmark](./classfile/src/jmh/kotlin/dev/freya02/botcommands/method/accessors/MethodAccessorBenchmark.kt), baseline is a direct call:

| Function type                                                   |         Baseline         |         ClassFile         |      kotlin-reflect       |
|-----------------------------------------------------------------|:------------------------:|:-------------------------:|:-------------------------:|
| () -> String                                                    | 0.114 µs/op<br/> ± 0,003 | 0.115 µs/op<br/>  ± 0,003 | 0.244 µs/op<br/>  ± 0,056 |
| (a: String, b: Int = 42, c: Double = 3.14159) -> String         | 0.179 µs/op<br/> ± 0,007 | 0.184 µs/op<br/>  ± 0,010 | 0.525 µs/op<br/>  ± 0,039 |
| suspend (a: String, b: Int = 42, c: Double = 3.14159) -> String | 0.183 µs/op<br/> ± 0,006 | 0.179 µs/op<br/>  ± 0,007 | 0.531 µs/op<br/>  ± 0,030 |

Note that each benchmark only use one accessor instance, in the real world there would be many more accessors,
meaning that each virtual call becomes non-trivial (see [megamorphic virtual calls](https://shipilev.net/jvm/anatomy-quarks/16-megamorphic-virtual-calls/)) and thus slower,
therefore this benchmark only shows:

- The custom classes can be as fast as direct calls
- The overhead of kotlin-reflect

### Performance in use-case

Due to the nature of the use case, the accessors will not be as fast as the numbers above may make it look like, but it won't be slow either.

For example, a slash command handler will need to call the appropriate user-defined method, and each method has its own accessor,
meaning that, at the call site (where the method is called), the JVM has no clue which accessor it has to call!

Consequently, it will have to determine which implementation of the accessor it has to call (remember, 1 method = 1 implementation).
Also note that this issue is *due to the use case*, a direct interface call, a custom accessor or a reflection call, would all have the same slowdown.
