# Creating slash commands

Slash commands are the new way of defining commands, even though there are limitations with them, 
we do have some advantages such as being easier to fill in, choices and auto-completion.

## Defining the command method

!!! tip
    Make sure you read the [common command requirements](../common-command-details.md#defining-the-command-method) first!

In addition to the common requirements, the first parameter must be `GlobalSlashEvent` for global commands
or `GuildSlashEvent` for guild commands, or guild-only global commands (default).

## Annotated commands

Annotated command methods must be annotated with `#!java @JDASlashCommand`,
where you can set the scope, name, description, etc..., 
while the declaring class must extend `ApplicationCommand`.

!!! question "Why do I need to extend `ApplicationCommand`?"

    As a limitation of annotated commands, 
    you are required to extend this class as it allows the framework to ask your commands for stuff,
    like what guilds a command should be pushed to, getting a value generator for one of their options,
    and also getting choices.

[//]: # (TODO add tip with live template)

!!! example
    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashPing.kt:ping-kotlin"
        ```

    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashPing.java:ping-java"
        ```

### Adding options

Options can be added with a parameter annotated with `#!java @SlashOption`.

All supported types are documented under `ParameterResolver`, and [other types can be added](#adding-option-resolvers).

!!! example
    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashSay.kt:say-kotlin"
        ```

    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashSay.java:say-java"
        ```

!!! tip "Inferred option names"
    Display names of options can be set on the annotation,
    but can also be deduced from the parameter name, this is natively supported in Kotlin,
    but for Java, you will need to [enable parameter names](../Inferred-option-names.md) on the Java compiler.

[//]: # (TODO add @MentionsString)

#### Using choices

You must override `getOptionChoices` in order to return a list of choices, 
be careful to check against the command path as well as the option's display name.

!!! example
    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashConvert.kt:convert-kotlin"
        ```

    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashConvert.java:convert-java"
        ```

As you can see, despite the short choice list, 
the method is quite lengthy and causes duplications with multiple commands.
This issue is solved with [predefined choices](#using-predefined-choices).

### Generated values

Generated values are a command parameter which gets their values computed by a lambda everytime a command is run, 
given by `ApplicationCommand#getGeneratedValueSupplier`, which you must override, 
similarly to adding choices.

As always, make sure to check against the command path as well as the option's display name.

!!! example
    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashCreateTime.kt:create_time-kotlin"
        ```

    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashCreateTime.java:create_time-java"
        ```

## DSL commands (Kotlin)

Commands can be DSL-declared in a public method annotated with `#!java @AppDeclaration`,
where the first parameter is a `GlobalApplicationCommandManager` (for global / guild-only global) commands, 
or `GuildApplicationCommandManager` for guild commands.

You can then use the `slashCommand` method, give it the command name, the command method, 
and then configure your command.

!!! tip
    You are allowed to not add any command at all, for example, 
    if the `guild` in `GuildApplicationCommandManager` isn't a guild you want the command to appear in.

[//]: # (TODO add tip with live template)

!!! example
    ```kotlin
    --8<-- "wiki/commands/slash/SlashPing.kt:ping-kotlin_dsl"
    ```

### Adding options

Options can be added with a parameter and declaring it using `option` in your command builder,
where the `declaredName` is the name of your parameter, the block will let you change the description, choices, etc.

All supported types are documented under `ParameterResolver`, and [other types can be added](#adding-option-resolvers).

!!! example
    ```kotlin
    --8<-- "wiki/commands/slash/SlashSay.kt:say-kotlin_dsl"
    ```

!!! tip
    You can override the option name by setting `optionName` in the option declaration:
    ```kotlin
    option("content", optionName = "sentence") {
        ...
    }
    ```

#### Using choices

Adding choices is very straight forward, you only have to give a list of choices to the `choice` property.

!!! example
    ```kotlin
    --8<-- "wiki/commands/slash/SlashConvert.kt:convert-kotlin_dsl"
    ```

As you can see, despite the short choice list, this causes duplications with multiple commands.
This issue is solved with [predefined choices](#using-predefined-choices).

### Generated values

Generated values are a command parameter that gets their values computed by the given block everytime the command run.

Contrary to the annotated commands, no checks are required, as this is tied to the currently built command.

!!! example
    ```kotlin
    --8<-- "wiki/commands/slash/SlashCreateTime.kt:create_time-kotlin_dsl"
    ```

## Default description

You can avoid setting the (non-localized) descriptions of your commands and options 
by putting them in a localization file, using the root locale (i.e., no locale suffix),
and have your localization bundle registered with `BApplicationConfigBuilder#addLocalizations`.

??? info "The same commands as before, but without the descriptions"
    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashSayDefaultDescription.kt:say_default_description-kotlin"
        ```

    === "Kotlin (DSL)"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashSayDefaultDescription.kt:say_default_description-kotlin_dsl"
        ```

    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashSayDefaultDescription.java:say_default_description-java"
        ```

!!! example "Adding the root localization bundle"
    For the given resource bundle:
    ```json title="src/main/resources/bc_localization/Commands.json"
    {
    --8<-- "bc_localization/Commands.json:default_description-json"
    }
    ```

    You can add the bundle by calling `BApplicationConfigBuilder#addLocalizations("Commands")`.

## Using predefined choices

If your choices stay the same for every command,
you can improve re-usability and avoid extra code by using choices on the resolver's level,
that is, the resolver will return the choices used for every option of their type.

All you now need to do is enable `usePredefinedChoices` on your option.

!!! example
    Here, the resolver for `TimeUnit` is already defined and will be explained in [Adding option resolvers](#adding-option-resolvers).

    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashConvertSimplified.kt:convert_simplified-kotlin"
        ```

    === "Kotlin (DSL)"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashConvertSimplified.kt:convert_simplified-kotlin_dsl"
        ```

    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashConvertSimplified.java:convert_simplified-java"
        ```

## Adding option resolvers

Option resolvers help you support other types for your command options, such as `TimeUnit`, or any object of your own.

Slash command option resolvers specify which option type will be used on Discord, 
and will handle the conversion from the Discord value to the corresponding object.

The class implementing the resolver, or the function returning a resolver, must be annotated with `#!java @Resolver`.

!!! note
    `#!java @Resolver` is one of the annotations that are considered as a service annotation.
    This means that it behaves exactly the same as if you had used `@BService`, 
    except here the annotation is more meaningful.

[//]: # (TODO Add link to DI section in note)

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

## Advanced usage

The Kotlin DSL also lets you do more, for example, using loops to generate commands, or even options. 
It also allows you to create more complex options, such as having multiple options in one parameter.

!!! info "Distinction between parameters and options"
    Method parameters are what you expect, a simple value in your method,
    but for the framework, parameters might be a complex object (composed of multiple options),
    or a single option, whether it's an injected service, a Discord option or a generated value.

    i.e., A parameter might be a single or multiple options, but an option is always a single value.

### Composite parameters

These are parameters composed of multiple options, of any type,
which gets merged into one parameter by using an aggregator.

!!! tip
    This is how varargs are implemented, they are a loop that generates N options, where X options are optional.

!!! example "Creating an aggregated parameter"
    Here is how you can use aggregated parameters to create a message delete timeframe, out of a `Long` and a `TimeUnit`.

    ```kotlin title="The aggregated object"
    --8<-- "wiki/commands/slash/SlashBan.kt:aggregated_object-kotlin"
    ```

    ```kotlin title="The aggregated parameter declaration"
    @Command
    class SlashBan {
        @AppDeclaration
        fun onDeclare(manager: GlobalApplicationCommandManager) {
            manager.slashCommand("ban", function = SlashBan::onSlashBan) {
                ...

    --8<-- "wiki/commands/slash/SlashBan.kt:declare_aggregate-kotlin_dsl"
            }
        }
    }
    ```

    The aggregating function can be a reference to the object's constructor,
    or a function taking the options and returning an object of the corresponding type. 

### Kotlin's inline classes

Input options as well as varargs can be encapsulated in an [inline class](https://kotlinlang.org/docs/inline-classes.html),
allowing you to define simple computable properties and functions for types where defining an extension makes no sense.
(Like adding an extension, that's specific to only one command, on a `String`)

!!! example "Using inline classes"

    ```kotlin
    --8<-- "wiki/commands/slash/SlashInlineWords.kt:inline_sentence-kotlin"
    ```

## Examples

You can take a look at more examples [here](https://github.com/freya022/BotCommands/tree/3.X/examples#examples).
