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

[//]: # (TODO add subcommands)

### Adding options

Options can be added with a parameter annotated with `#!java @SlashOption`.

All supported types are documented under `ParameterResolver`, and [other types can be added](option-resolvers.md).

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
    but for Java, you will need to [enable parameter names](../../using-botcommands/parameter-names.md) on the Java compiler.

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

Commands can be DSL-declared by either implementing:

- `GlobalApplicationCommandProvider` (for global / guild-only global application commands), or,
- `GuildApplicationCommandProvider` (for guild-specific application commands)

You can then use the `slashCommand` method on the `manager`, give it the command name, the command method, 
and then configure your command.

!!! tip
    You are allowed to not add any command at all, for example, 
    if the `guild` in `GuildApplicationCommandManager` isn't a guild you want the command to appear in.

!!! example
    ```kotlin
    --8<-- "wiki/commands/slash/SlashPing.kt:ping-kotlin_dsl"
    ```

[//]: # (TODO add subcommands)

### Adding options

Options can be added with a parameter and declaring it using `option` in your command builder,
where the `declaredName` is the name of your parameter, the block will let you change the description, choices, etc.

All supported types are documented under `ParameterResolver`, and [other types can be added](option-resolvers.md).

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
    Here, the resolver for `TimeUnit` is already defined and will be explained in [Adding option resolvers](option-resolvers.md).

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

## Examples

You can take a look at more examples [here](https://github.com/freya022/BotCommands/tree/3.X/examples#examples).
