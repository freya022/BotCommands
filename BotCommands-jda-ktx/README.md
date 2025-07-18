# BotCommands module - JDA Kotlin extensions
This module provides a set of Kotlin extensions and top-level functions,
similar to [jda-ktx](https://github.com/MinnDevelopment/jda-ktx).

## Usage
If you come from `jda-ktx`, look at [Migrating from jda-ktx](#migrating-from-jda-ktx)

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
- [messages/Editing.kt](./src/main/kotlin/dev/freya02/botcommands/jda/ktx/messages/Editing.kt)

> [!NOTE]
> Some of them have a `_` suffix to help import, as JDA already has methods with the same names,
> you can use an import alias if you want to keep the same name:
> `import dev.freya02.botcommands.jda.ktx.messages.reply_ as reply`

### Colors
A few functions to make colors were added:

- `rgb(red, green, blue)`
- `hsb(hue, saturation, brightness)`
- `hex("#FF0000")`
  - This one should display the color in the gutter, at least on IntelliJ

## Migrating from `jda-ktx`
Migrating to this extension is recommended but not *necessary*

### Changes to the `CoroutineEventManager`
The core module includes its own event manager,
you can configure its default timeout at `BEventManagerConfig#defaultTimeout`,
and the `CoroutineScope` in `BCoroutineScopesConfig#eventManagerScope`.

As a result, extensions from `jda-ktx` relying on its `CoroutineEventManager` no longer work,
but you can fix this by importing the extension from this module.

### Changed functionalities
If you decide to switch, some functions have replacements:

- `awaitMessage` must now be used on an `EventWaiter` instance
- `awaitButton` is now `Button#await` (the framework's `Button` class)
- `jda-ktx`'s `Paginator` is replaced by the core [`Paginators`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.pagination/-paginators/index.html)

### Incompatible functionalities
If you keep using `jda-ktx`, some functions will not work or behave unexpectedly:
- [`scope` on `JDA`/`ShardManager`](https://github.com/MinnDevelopment/jda-ktx/blob/54110fc157e4e6c85a7ba052b37a3330a72fe8ab/src/main/kotlin/dev/minn/jda/ktx/jdabuilder/inject.kt#L38-L48)
- Any function related to build JDA or shard managers, it is recommended you use the helper functions in `JDAService`.

### Missing functionalities
Additionally, `jda-ktx` has additional extensions that were not ported:
- `String#toEmoji()`
- `String#toCustomEmoji()`
  - It is recommended to use [application emojis](https://bc.freya02.dev/3.X/using-botcommands/app-emojis/)
- `String#toUnicodeEmoji()`
  - It is recommended to use [jda-emojis](https://github.com/freya022/jda-emojis)'s `UnicodeEmojis` class (already included by default)
- `getDefaultScope`
  - You can replace it with `namedDefaultScope`
- All extensions already handled by the framework, such as creating/listening to commands, components and modals
- The [`WebhookAppender`](https://github.com/MinnDevelopment/jda-ktx/blob/54110fc157e4e6c85a7ba052b37a3330a72fe8ab/src/main/kotlin/dev/minn/jda/ktx/logback/WebhookAppender.kt)
- `named`, `String#invoke` and some `into` extensions from [messages/utils.kt](https://github.com/MinnDevelopment/jda-ktx/blob/master/src/main/kotlin/dev/minn/jda/ktx/messages/utils.kt)
- OkHttp's [`Call` extensions](https://github.com/MinnDevelopment/jda-ktx/blob/54110fc157e4e6c85a7ba052b37a3330a72fe8ab/src/main/kotlin/dev/minn/jda/ktx/util/okhttp.kt#L58) `awaitWith` and `await`
  - I would recommend using [ktor](https://ktor.io/docs/client-create-new-application.html#new-project) with the [OkHttp client engine](https://ktor.io/docs/client-engines.html#okhttp)
- [SLF4J logger delegation](https://github.com/MinnDevelopment/jda-ktx/blob/54110fc157e4e6c85a7ba052b37a3330a72fe8ab/src/main/kotlin/dev/minn/jda/ktx/util/proxies.kt#L61-L86) (`private val logger by SLF4J`)
  - I recommend using [kotlin-logging](https://github.com/oshai/kotlin-logging) instead (`private val logger = KotlinLogging.logger { }`)
  - The [`Logging`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.core/-logging/index.html) class may also be of interest
- [`ref` extensions](https://github.com/MinnDevelopment/jda-ktx/blob/54110fc157e4e6c85a7ba052b37a3330a72fe8ab/src/main/kotlin/dev/minn/jda/ktx/util/proxies.kt#L29-L59) on entities
  - I don't recommend using them because while they keep entities up to date between reconnects, they don't prevent you from sending requests to deleted entities

Please open an issue if you feel like one of these extensions are worth porting, an alternative is to port them locally.

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
