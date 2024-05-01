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
