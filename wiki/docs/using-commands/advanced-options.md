Text and application command options can benefit from more complex option types, 
by combining multiple options into one parameter, 
such as varargs, mention strings and custom data structures.

## Varargs
Varargs lets you generate options (up to 25 options per command) and put the values in a `List`,
the number of required options is also configurable.

### Annotation-declared commands
Use [`#!java @VarArgs`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.annotations/-var-args/index.html) on the parameter.

The drawback is that each option will be configured the same, name, description, etc...

### Code-declared commands
Using `optionVararg` or `inlineClassOptionVararg` on your command builder lets you solve the above issues.

!!! example
    ```kotlin
    fun onSlashCommand(event: GuildSlashEvent, names: List<String>) {
        // ...    
    }
    ```

    ```kotlin
    manager.slashCommand("command", ::onSlashCommand) {
        optionVararg(
            declaredName = "names", // Name of the method parameter
            amount = 5, //How many options to generate
            requiredAmount = 1, //How many of them are required
            optionNameSupplier = { num -> "name_$num" } // Generate the name of each option
        ) { num ->
            // This runs for each option
            description = "Name N°$num" 
        }
    }
    ```

## Mention strings
!!! info "You can use this annotation on both code-declared and annotation-declared commands"

[`#!java @MentionsString`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.annotations/-mentions-string/index.html) is an annotation
that lets you retrieve as many mentions as a [string option lets you type](https://docs.jda.wiki/net/dv8tion/jda/api/interactions/commands/build/OptionData.html#MAX_STRING_OPTION_LENGTH),
you must use it on a `List` parameter with an element type supported by the annotation.

You can also use a `List<IMentionable>`, where you can set the requested mention types.

!!! note

    This won't restrict what the user can type on Discord,
    this only enables parsing mentions inside the string.

??? example "Bulk ban example"

    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashBulkBan.kt:bulk_ban-kotlin"
        ```

    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashBulkBan.java:bulk_ban-java"
        ```

## Advanced code-declared options

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
