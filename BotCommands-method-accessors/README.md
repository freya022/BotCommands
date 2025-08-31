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

Performance numbers from [MethodAccessorBenchmark](./classfile/src/jmh/kotlin/dev/freya02/botcommands/method/accessors/MethodAccessorBenchmark.kt)

| Function type                                                   | Baseline    | ClassFile   | kotlin-reflect |
|-----------------------------------------------------------------|-------------|-------------|----------------|
| () -> String                                                    | 0.110 µs/op | 0.115 µs/op | 0.227 µs/op    |
| (a: String, b: Int = 42, c: Double = 3.14159) -> String         | 0.177 µs/op | 0.287 µs/op | 0.514 µs/op    |
| suspend (a: String, b: Int = 42, c: Double = 3.14159) -> String | 0.180 µs/op | 0.288 µs/op | 0.535 µs/op    |
