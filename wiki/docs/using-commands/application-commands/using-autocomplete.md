Autocomplete lets you have options where you can give suggestions to the user while they type,
the framework allows you to return a collection of choices, 
choice-compatible types such as `String`, `Long` and `Double`, or even custom types,
all of which can be cached.

!!! note

    Autocompleted options do not force the user to choose one of the returned choices,
    they can still type anything.

## Creating autocomplete handlers

=== "Code-declared"

    You will have to implement the [`AutocompleteHandlerProvider`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration/-autocomplete-handler-provider/index.html),
    enabling you to declare autocomplete handlers using the manager.

    You can optionally put a name on the handler, if you plan on using [`autocompleteByName`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.builder/-slash-command-option-builder/autocomplete-by-name.html), 
    however, that's not necessary when using [`autocompleteByFunction`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.builder/-slash-command-option-builder/autocomplete-by-function.html).

    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashWord.kt:word_autocomplete-kotlin_dsl"
        ```

=== "Annotated"

    You will have to use [`@AutocompleteHandler`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration/-autocomplete-handler-provider/index.html),
    give it a unique name, I'd recommend using one similar to `ClassName: optionName`, it will be useful to reference it in commands later on.

    !!! info

        An annotated autocomplete handler can still be referenced [by name](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.builder/-slash-command-option-builder/autocomplete-by-name.html) 
        and [by function](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.builder/-slash-command-option-builder/autocomplete-by-function.html)
        in code-declared commands.

    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashWord.kt:word_autocomplete-kotlin"
        ```
    
    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashWordAutocomplete.java:word_autocomplete-java"
        ```

!!! tip "Sorting autocomplete results"

    Sorting results by relevancy is a tricky task, while it can be as simple as `myItemName.startsWith(input)`
    you can try to use [`AutocompleteAlgorithms`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete/-autocomplete-algorithms/index.html)
    to easily sort the results, but your mileage may vary.

    It may sometimes makes more sense to use one "similarity" algorithm over another, 
    depending on what user input you expect, and what the source items are,
    you can experiment different algos from the [java-string-similarity library](https://github.com/tdebatty/java-string-similarity),
    already included in the framework.

    You are encouraged to to try inputs against different algos, and find what works the best, 
    which one could filter the results of the previous algo, etc.

## Caching

When the results are stable, you can enable autocomplete caching, saving time when a user types the same query.

=== "Code-declared"

    To enable it, configure the cache using the [`cache`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder/-autocomplete-info-builder/cache.html) 
    configurer.

    By default it will cache by using the user input, but you can add arguments to the cache key by:

    - Adding the user ID with [`userLocal`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder/-autocomplete-cache-info-builder/user-local.html)
    - Adding the channel ID with [`channelLocal`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder/-autocomplete-cache-info-builder/channel-local.html)
    - Adding the guild ID with [`guildLocal`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder/-autocomplete-cache-info-builder/guild-local.html)
    - Adding values of options by their names in [`compositeKeys`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder/-autocomplete-cache-info-builder/composite-keys.html)    

=== "Annotated"

    To enable it, configure the cache using [`#!java @CacheAutocomplete`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations/-cache-autocomplete/index.html).

    By default it will cache by using the user input, but you can add arguments to the cache key by:

    - Adding the user ID with [`userLocal`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations/-cache-autocomplete/user-local.html)
    - Adding the channel ID with [`channelLocal`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations/-cache-autocomplete/channel-local.html)
    - Adding the guild ID with [`guildLocal`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations/-cache-autocomplete/guild-local.html)
    - Adding values of options by their names in [`compositeKeys`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations/-cache-autocomplete/composite-keys.html)

!!! note
    
    If the outputs for a same input are stable but may rarely change (think, a list that changes daily), you can [invalidate autocomplete caches](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core/-b-context/invalidate-autocomplete-cache.html)
    when it does eventually change.

## Transforming elements into choices

If you wish to return collections of anything but the default supported types,
you will need to create a service which transforms those objects into choices,
by implementing [`AutocompleteTranformer`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.commands.application.slash.autocomplete/-autocomplete-transformer/index.html).

!!! example

    === "Kotlin"
        ```kotlin title="FullName.kt"
        --8<-- "wiki/autocomplete/transformer/FullNameTransformer.kt:full_name_obj-kotlin"
        ```

        ```kotlin title="FullNameTransformer.kt"
        --8<-- "wiki/autocomplete/transformer/FullNameTransformer.kt:autocomplete_transformer-kotlin"
        ```

    === "Java"
        ```java title="FullName.java"
        --8<-- "wiki/java/autocomplete/transformer/FullName.java:full_name_obj-java"
        ```

        ```java title="FullNameTransformer.java"
        --8<-- "wiki/java/autocomplete/transformer/FullNameTransformer.java:autocomplete_transformer-java"
        ```

## Usage in commands

Discover how to use your autocomplete handlers on:

- [Annotated slash commands](writing-slash-commands.md#using-autocomplete)
- [Code-declared slash commands](writing-slash-commands.md#using-autocomplete_1)