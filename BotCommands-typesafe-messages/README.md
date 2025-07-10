# BotCommands module - Typesafe messages
This module allows you to define functions which retrieves translated messages,
without having to implement anything, alongside a few other benefits:
- Checks if the template key exists in the root bundle
- Checks if function parameters exists in your template's arguments
- Checks if parameters can be formatted (on a best effort)
- Removes magic strings from your business logic

## Example
> [!NOTE]
> This example will use Kotlin but any other language should work.

### Creating a localization bundle
Let's start by creating a localization bundle at `src/main/resources/bc_localization/MyBotMessages.json`,
for our example it will contain a single localization template where:

- The key is `bot.info`
- The template is `I am in {guild_count, number} {guild_count, choice, 0#guilds|1#guild|1<guilds} and my up-time is {uptime, number} seconds.`
  - `guild_count` and `uptime` are variables
  - `number` and `choice` are format types
  - `0#guilds|1#guild|1<guilds` is a subformat pattern for [ChoiceFormat](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/ChoiceFormat.html)
  - See [MessageFormat](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/MessageFormat.html) for more details

```json
{
  "bot.info": "I am in {guild_count, number} {guild_count, choice, 0#guilds|1#guild|1<guilds} and my up-time is {uptime_ms, number} seconds."
}
```

### Base interfaces

> [!NOTE]
> None of the interfaces defined below need to be implemented, they will be implemented and registered automatically.

#### Message source
Create an interface which extends `IMessageSource`, each function annotated with `@LocalizedContent` needs to return `String`,
in that annotation you will need to put the key present in your localization bundle:

```kt
interface MyBotMessages : IMessageSource {

    // The function can have any name you want
    // Parameter names are converted to snake_case for use in the template
    @LocalizedContent("bot.info")
    fun botInfo(guildCount: Int, uptimeMs: Long): String
}
```

[//]: # (TODO use Duration instead of Long for the uptime, explain about converters)

You can of course add more functions with different templates if necessary.

#### Message source factory

Then, you need a way to get instances of `MyBotMessages`, create an interface extending `IMessageSourceFactory<MyBotMessages>`,
and annotate it with `@MessageSourceFactory("MyBotMessages")`:
```kt
// The base name of the localization bundles to look at,
// which files it actually loads is based on the available LocalizationMapReader(s)
// and the effective locale
@MessageSourceFactory("MyBotMessages")
interface MyBotMessagesFactory : IMessageSourceFactory<MyBotMessages>
```

This interface will allow you to create `MyBotMessages` instances from different objects,
such as `Interaction`.

[//]: # (TODO add more object types, probably Locale/DiscordLocale)

### Usage

```kt
@Command
class SlashInfo(
    // Inject our factory, instances of it are created automatically
    private val botMessagesFactory: MyBotMessagesFactory,
) : ApplicationCommand() {

    @JDASlashCommand(
        name = "info",
        description = "Sends info about the bot",
    )
    fun onSlashFox(event: GuildSlashEvent) {
        // Create an instance from the current interaction
        val botMessages = botMessagesFactory.create(event)
        val response = botMessages.botInfo(
            // Use named parameters to make the arguments clearer!
            guildCount = event.jda.guildCache.size(),
            uptimeMs = ManagementFactory.getRuntimeMXBean().uptime,
        )

        event.reply(response)
            .setEphemeral(true)
            .queue()
    }
}
```

Try out `/info`!

## Installation
There are different dependencies based on what dependency injection you use:

![](https://img.shields.io/maven-central/v/io.github.freya022/BotCommands-typesafe-messages-core?versionPrefix=3)

<details>
<summary>Built-in</summary>

### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-typesafe-messages-bc</artifactId>
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
    implementation("io.github.freya022:BotCommands-typesafe-messages-bc:VERSION")
}
```

</details>

<details>
<summary>Spring</summary>

### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-typesafe-messages-spring</artifactId>
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
    implementation("io.github.freya022:BotCommands-typesafe-messages-spring:VERSION")
}
```

</details>

Alternatively, you can use Jitpack to use **snapshot** versions,
you can refer to [the JDA wiki](https://jda.wiki/using-jda/using-new-features/) for more information.
