# BotCommands module - JDA Kotlin extensions
This module provides a set of Kotlin extensions and top-level functions,
similar to [jda-ktx](https://github.com/MinnDevelopment/jda-ktx).

## Usage
### Coroutine extensions

```kt
// Await RestAction result
suspend fun <T> RestAction<T>.await()
// Await Task result
suspend fun <T> Task<T>.await()

// Await specific event
suspend fun <T : GenericEvent> JDA.await(filter: (T) -> Boolean = { true })
// Await specific event
suspend fun <T : GenericEvent> ShardManager.await(filter: (T) -> Boolean = { true })
// Await message from specific channel (filter by user and/or filter function)
suspend fun EventWaiter.awaitMessage(author: User? = null, filter: (Message) -> Boolean = { true }): Message

// PaginationAction as a Flow
fun <T> PaginationAction<T, *>.asFlow(): Flow<T>
```

### Reified type parameters
```kt
// guild.getChannel<TextChannel>(id)
inline fun <reified T : GuildChannel> IGuildChannelContainer<in T>.getChannel(id: Long): T?
inline fun <reified T : GuildChannel> IGuildChannelContainer<in T>.getChannel(id: String): T?
inline fun <reified T : GuildChannel> IGuildChannelContainer<in T>.getChannel(id: ULong): T?

// waiter.of<MessageReceivedEvent>()
inline fun <reified T : Event> EventWaiter.of(): EventWaiterBuilder<T>

// mentions.getChannels<TextChannel>()
inline fun <reified T : GuildChannel> Mentions.getChannels(): List<T>

// mentions.getChannelsBag<TextChannel>()
inline fun <reified T : GuildChannel> Mentions.getChannelsBag(): Bag<T>

// jda.listenOnce<MessageReceivedEvent>()
inline fun <reified E : GenericEvent> JDA.listenOnce(): Once.Builder<E>
```

### Result-like RestActions
A class similar to Kotlin's `Result` was created specially for REST actions,
it allows you to handle specific failure types separately, recover with a new value,
as well as ignore them.

You can find all the functions in [requests/RestResult.kt](./src/main/kotlin/dev/freya02/botcommands/jda/ktx/requests/RestResult.kt).

There are a few functions returning `RestResult`:
```kt
// Equivalent to runCatchingRest(block).ignore(ignored, *ignoredResponses)
inline fun <R> runCatchingResponse(ignored: ErrorResponse, vararg ignoredResponses: ErrorResponse, block: () -> R): RestResult<R>

// Awaits the result and encapsulates the value/error in a RestResult 
suspend fun <R> RestAction<R>.awaitCatching(): RestResult<R>

// Runs the block and encapsulates the value/error in a RestResult
inline fun <T> runCatchingRest(block: () -> T): RestResult<T>
```

### RestAction extensions
```kt
// Awaits completion then returns `Unit`, useful for doing an action then immediately return
// val member = guild.retrieveMemberByIdOrNull(id)
//     ?: return event.reply_("The member does not exist!").awaitUnit()
suspend fun RestAction<*>.awaitUnit()

// Same as the extension above, but instead of returning Unit it returns null
suspend fun <R> RestAction<*>.awaitNull(): R?

// Sends a request, only throwing asynchronously if the error was not ignored
// Equivalent of queue(null, ErrorHandler().ignore(ignored, *errorResponses))
fun RestAction<*>.queueIgnoring(ignored: ErrorResponse, vararg errorResponses: ErrorResponse)

// Awaits and returns null on the specific error responses, throws otherwise
suspend fun <R> RestAction<R>.awaitOrNullOn(ignored: ErrorResponse, vararg errorResponses: ErrorResponse): R?

// Awaits and wraps the result in a RestResult
suspend fun <R> RestAction<R>.awaitCatching(): RestResult<R>
```

### Error response handlers
```kt
// Runs the block and only throws if an error was not ignored
inline fun runIgnoringResponse(ignored: ErrorResponse, vararg ignoredResponses: ErrorResponse, block: () -> Unit)

// Runs the block and returns null if an error was not ignored
inline fun <R> runIgnoringResponseOrNull(ignored: ErrorResponse, vararg ignoredResponses: ErrorResponse, block: () -> R): R?
```

### Building messages
DSLs are also provided to build message, embeds as well as component trees,
you can see all utilities in [messages/Messages.kt](./src/main/kotlin/dev/freya02/botcommands/jda/ktx/messages/Messages.kt).

#### Messages
```kt
val message/*: MessageCreateData*/ = MessageCreate(mentions = Mentions.none()) {
    content = "Hello ${event.user.asMention}!"
  
    components += row(buttons.link("Click me", "https://github.com/freya022/BotCommands"))
}
```

[//]: # (TODO add CV2 example)

#### Embeds
```kt
val embed/*: MessageEmbed*/ = Embed(color = 0xFF0000, timestamp = Instant.now()) {
    title = "Hello Embed"
    description = "This is an embed description"
  
    field {
        name = "And a field!"
        value = "With a value"
        inline = false
    }
}

val message/*: MessageCreateData*/ = MessageCreate {
    embeds += embed
}
```

> [!TIP]
> A similar `embed` method exists inside the `MessageCreate` block, it will add the embed automatically.

### Sending/editing messages
A few extensions were added with the same parameters (and sometimes more) as `MessageCreate`/`MessageEdit`.

- [messages/Sending.kt](./src/main/kotlin/dev/freya02/botcommands/jda/ktx/messages/Sending.kt)
- [messages/SendingTo.kt](./src/main/kotlin/dev/freya02/botcommands/jda/ktx/messages/SendingTo.kt)
- [messages/Editing.kt](./src/main/kotlin/dev/freya02/botcommands/jda/ktx/messages/Editing.kt)
- [messages/EditingTo.kt](./src/main/kotlin/dev/freya02/botcommands/jda/ktx/messages/EditingTo.kt)

> [!NOTE]
> Some of them have a `_` suffix to help import, as JDA already has methods with the same names,
> you can use an import alias if you want to keep the same name:
> `import dev.freya02.botcommands.jda.ktx.messages.reply_ as reply`

### Message converters
```kt
// MessageCreateData -> MessageEditData (will replace entire message when editing)
fun MessageCreateData.toEditData(): MessageEditData

// MessageEditData -> MessageCreateData
fun MessageEditData.toCreateData(): MessageCreateData
```

### Durations
See [durations/Durations.kt](./src/main/kotlin/dev/freya02/botcommands/jda/ktx/durations/Durations.kt)

### Misc
```kt
// Suppresses warnings related to a missing 'MESSAGE_CONTENT' intent inside the block
inline fun <R> suppressContentWarning(block: () -> R): R
```

### Colors
A few functions to make colors were added:

- `rgb(red, green, blue)`
- `hsb(hue, saturation, brightness)`
- `hex("#FF0000")`
  - This one should display the color in the gutter, at least on IntelliJ

## Installation
![](https://img.shields.io/maven-central/v/io.github.freya022/BotCommands-jda-ktx?versionPrefix=3)

### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-jda-ktx</artifactId>
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
    implementation("io.github.freya022:BotCommands-jda-ktx:VERSION")
}
```

Alternatively, you can use Jitpack to use **snapshot** versions,
you can refer to [the JDA wiki](https://jda.wiki/using-jda/using-new-features/) for more information.
