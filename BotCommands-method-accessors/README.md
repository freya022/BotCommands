# BotCommands module - Method accessors
This module provides abstractions to call user methods, it should not be included manually.

## Implementations

### kotlin-reflect
This is the default implementation, as is what the framework originally used,
it only delegates to `callSuspendBy` with pairs of parameter -> value.

### ClassFile-based
This newer implementation takes advantage of [hidden classes](https://www.baeldung.com/java-hidden-classes) and the [Class-File API](https://openjdk.org/jeps/484),
which, for each function, generates a hidden class with instructions optimized to directly call the user method.

This allows for shorter stack traces in exceptions and the debugger, no `InvocationTargetException`s, and better performance.

This can be enabled before starting your bot, by calling `BotCommands.preferClassFileAccessors()`,
note that this will only have an effect if your bot runs on Java 24+.

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

- The custom classes can be as fast as direct calls*
- The overhead of kotlin-reflect

*Only if the JVM can accurately figure out which accessor implementation is called, as our use case can call many different handlers, it cannot be optimized so well, this caveat applies equally to virtual calls and reflection calls
