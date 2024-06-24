Option resolvers help you support other types for your command options, such as `TimeUnit`, or any object of your own.

Slash command option resolvers specify which option type will be used on Discord,
and will handle the conversion from the Discord value to the corresponding object.

The class implementing the resolver, or the function returning a resolver, must be annotated with `#!java @Resolver`.

!!! note
    `#!java @Resolver` is one of the annotations that are considered as a [service annotation](../../using-botcommands/dependency-injection/creating-services.md).
    This means that it behaves exactly the same as if you had used `@BService`,
    except here the annotation is more meaningful.

### Implementation

For that, you need a class annotated with `#!java @Resolver` extending `ClassParameterResolver`,
and implementing `SlashParameterResolver`.

The first type parameter is the type of your resolver implementation, and the second type is what the resolver returns.

!!! example "A `TimeUnit` resolver"
    === "Kotlin"
        ```kotlin
        --8<-- "wiki/resolvers/TimeUnitResolver.kt:time_unit_resolver-detailed-kotlin"
        ```

    === "Java"
        ```java
        --8<-- "wiki/java/resolvers/TimeUnitResolver.java:time_unit_resolver-detailed-java"
        ```
    
    As you can see, this defines the slash command's option to be a string, 
    and provides predefined choices, letting you easily use them in your commands.

### Built-in resolver generators

The framework also provides functions in `Resolvers` to do most of the work for some types,
all you need to do is declare a service factory with `#!java @Resolver` and use the provided methods.

!!! note
    Currently there is only a factory for enum resolvers, but others might be added in the future.

!!! example "How to easily make a resolver for an enum type"
    === "Kotlin"
        ```kotlin
        object TimeUnitResolverSimplified {
        --8<-- "wiki/resolvers/TimeUnitResolver.kt:time_unit_resolver-simplified-kotlin"
        ```
        As this functions as a service factory, the method needs to be in an `object` or have a no-arg constructor.

    === "Java"
        ```java
        public class TimeUnitResolverSimplifiedJava {
        --8<-- "wiki/java/resolvers/TimeUnitResolverSimplified.java:time_unit_resolver-simplified-java"
        ```
        As this functions as a service factory, the method needs to be static.
