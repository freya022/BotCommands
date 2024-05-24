In addition to what [JDA-KTX](https://github.com/MinnDevelopment/jda-ktx) offers, Kotlin users have access to top-level functions and extensions in various categories:

## JDA
### REST actions
??? example "[`awaitUnit`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/await-unit.html) - Awaits completion and returns `Unit`, particularly useful to reply + return" 

    ```kotlin
    fun onSlashCommand(event: GuildSlashEvent, inputUser: InputUser)
        val member = inputUser.member
            ?: return event.reply_("The user needs to be a member of this server!", ephemeral = true).awaitUnit()
    }
    ```

??? example "[`awaitOrNullOn`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/await-or-null-on.html) - Awaits completion, returns `null` if the action failed with the expected error response"

    ```kotlin
    fun onSlashBanInfo(event: GuildSlashEvent, user: User)
        val ban = event.guild.retrieveBan(user).awaitOrNullOn(ErrorResponse.UNKNOWN_BAN)
            ?: return event.reply_("This user is not banned", ephemeral = true).awaitUnit()
    }
    ```

??? example "[`awaitCatching`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/await-catching.html) - Awaits completion and returns a `Result` with the wrapped value/failure"

    ```kotlin
    fun onSlashBanInfo(event: GuildSlashEvent, user: User)
        val ban = event.guild.retrieveBan(user).awaitCatching()
            .onErrorResponse(ErrorResponse.UNKNOWN_BAN) {
                return event.reply_("This user is not banned", ephemeral = true).awaitUnit()
            }
            .getOrThrow()
    }
    ```

### Error response handling
- [`onErrorResponse`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/on-error-response.html) - See `awaitCatching`
- [`Result<Unit>.ignore`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/ignore.html) - Ignores and clears the specified error responses
- [`Result<T>.handle`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/handle.html) - Supplies a value when the specified error response is caught

??? example "[`runIgnoringResponse`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/run-ignoring-response.html) - Runs the block and ignores the following error responses, throws other exceptions"

    ```kotlin
    runIgnoringResponse(ErrorResponse.CANNOT_SEND_TO_USER) {
        channel.sendMessage(msg).await()
    }
    ```

??? example "[`runIgnoringResponseOrNull`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/run-ignoring-response-or-null.html) - Runs the block and returns `null` on the following error responses, throws other exceptions"

    ```kotlin
    suspend fun Guild.retrieveBanOrNull(user: UserSnowflake): Ban? = runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_BAN) {
        retrieveBan(user).await() // Can also use awaitOrNullOn, removing runIgnoringResponseOrNull
    }
    ```

### Messages
- [`MessageCreateData.toEditData`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/to-edit-data.html) - Does what it says, the edit data will replace the content of the entire message
- [`MessageEditData.toCreateData`]() - Do I need to say anything?

- [`MessageCreateData.send`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/send.html) - Sends the message, this is useful for chaining
- [`MessageEditData.edit`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/edit.html) - Edits with that message, this is useful for chaining
- [`InteractionHook.replaceWith`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/replace-with.html) - Replaces this interaction's message with the following content

- [`RestAction<R>.deleteDelayed`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/delete-delayed.html) - Deletes the message after the delay, the rest action itself is delayed, not the code execution

### Entity retrieval
Those check the cache before doing a request.

- [`Guild.retrieveMemberOrNull`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/retrieve-member-or-null.html) - Returns null if the member does not exist
- [`JDA.retrieveUserOrNull`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/retrieve-user-or-null.html) - Returns null if the user does not exist
- [`Guild.retrieveThreadChannelById`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/retrieve-thread-channel-by-id.html) - Retrieves a thread by ID, from any thread container, archived or not.
- [`Guild.retrieveThreadChannelOrNull`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/retrieve-thread-channel-or-null.html) - Same but returns null if it does not exist, if the bot doesn't have access to it, or if the channel isn't a thread.

### Misc
??? example "[`suppressContentWarning`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/suppress-content-warning.html) - Runs the block and suppresses warnings emitted by JDA when reading message content, this is mostly useful in message context commands"

    ```kotlin
    val contentRaw = suppressContentWarning { message.contentRaw }
    ```

- [`getMissingPermissions`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/get-missing-permissions.html) - Gets the missing permissions from the required permissions, for the given entity, in the specified channel

Any method accepting a Java `Duration` should also have an extension using Kotlin's `Duration`

## Resolvers
??? example "[`enumResolver`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.parameters/enum-resolver.html) - Creates a parameter resolver which transforms arguments into an enum entry, compatible with most handlers"

    ```kotlin
    enum class MyEnum {
        FIRST,
        SECOND,
        THIRD
    }
    
    @BConfiguration
    class MyEnumResolverProvider {
        // Creates an enum resolver for all values
        // you can also customize what values can be used, per-guild,
        // and also change how they are displayed
        @Resolver
        fun myEnumResolver() = enumResolver<MyEnum>()
    }
    ```

??? example "[`resolverFactory`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.parameters/resolver-factory.html) - Creates a factory for parameter resolvers, useful to avoid the boilerplate of using [`TypedParameterResolverFactory`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.parameters/-typed-parameter-resolver-factory/index.html)"

    See example on [the docs](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.parameters/resolver-factory.html)

## I/O
??? example "[`readResource`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/read-resource.html) - Gets an `InputStream` of a resource from the classpath"

    ```kotlin
    readResource("/file.txt").use { contentStream ->
        // ...
    }
    ```

??? example "[`readResourceAsString`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/read-resource-as-string.html) - Gets a resource from the classpath as a `String`"

    ```kotlin
    val content = readResourceAsString("/file.txt")
    ```

??? example "[`withResource`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/with-resource.html) - Uses an `InputStream` of a resource from the classpath"

    ```kotlin
    withResource("/file.txt") { contentStream ->
        // ...
    }
    ```

## Coroutines
??? example "[`namedDefaultScope`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/named-default-scope.html) - Creates a `CoroutineScope` with a thread name and a fixed thread pool"

    ```kotlin
    // 1 thread named "[feature] timeout"
    // You can also configure other CoroutineScope characteristics
    private val timeoutScope = namedDefaultScope("[feature] timeout", corePoolSize = 1)

    // ...

    timeoutScope.launch {
        // Async task
    }
    ```

## Logging
??? example "[`KotlinLogging.loggerOf`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/logger-of.html) - Creates a logger targeting the specified class"

    ```kotlin
    private val logger = KotlinLogging.loggerOf<MyService>()

    @BService
    class MyServiceImpl : MyService {
        // ...
    }
    ```

## Collections
- [`enumSetOf`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/enum-set-of.html) - Creates an enum set of the provided type
- [`enumSetOfAll`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/enum-set-of-all.html) - Creates an enum set of the provided type, with all the entries in it
- [`enumMapOf`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/enum-map-of.html) - Creates a map with an enum key
- [`toImmutableList`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/to-immutable-list.html) - Creates an _immutable copy_ of the list
- [`toImmutableSet`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/to-immutable-set.html) - Creates an _immutable copy_ of the set
- [`toImmutableMap`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/to-immutable-map.html) - Creates an _immutable copy_ of the map
- [`containsAny`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/contains-any.html) - Checks if the collection contains any of the provided elements