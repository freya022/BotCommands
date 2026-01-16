# BotCommands module - Typesafe messages
This module allows you to define functions which retrieves translated messages,
without having to implement anything, alongside a few other benefits:
- Checks if the templates exists in your bundles, ensuring your content can always be displayed
- Checks if function parameters exists in your template's arguments, meaning all parameters map to an argument
- Checks if template arguments map to function parameters, so all arguments have values
- Removes the need for magic strings (for the arguments), improving type safety and making regressions appear immediately

## Example
> [!NOTE]
> This example will use Kotlin but any other language should work.

### Creating a localization bundle
Let's start by creating a localization bundle at `src/main/resources/bc_localization/MyBotMessages.json`,
for our example it will contain a single localization template

```json
{
  "bot.info": "I am in {guild_count, number} {guild_count, choice, 0#guilds|1#guild|1<guilds} and I am up since {uptime_timestamp}."
}
```

- The key is `bot.info`
- The template is `I am in {guild_count, number} {guild_count, choice, 0#guilds|1#guild|1<guilds} and I am up since {uptime_timestamp}.`
  - `guild_count` and `uptime_timestamp` are variables
  - `number` and `choice` are format types
  - `0#guilds|1#guild|1<guilds` is a subformat pattern for [ChoiceFormat](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/ChoiceFormat.html)
  - See [MessageFormat](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/MessageFormat.html) for more details

### Creating our message source

Create an interface which extends `IMessageSource`;
it will contain functions annotated with `@LocalizedContent`,
the annotation's value is the key present in your localization bundle,
and the function needs to return a `String`.

```kt
interface CommandReplies : IMessageSource {

    // The function can have any name you want
    @LocalizedContent("bot.info")
    fun botInfo(
      // Parameter names are converted to snake_case for use in the template, here it's 'guild_count'
      guildCount: Int, 
      // You could also pass a Timestamp as it has a proper `toString()`
      uptimeTimestamp: String
    ): String
}
```

> [!NOTE]
> You do not need to implement this interface.

> [!TIP]
> You can inject instances of this interface in any interaction handler such as application commands, components and modals.

> [!TIP]
> You can override the locale using `@PreferLocale` or by passing a `DiscordLocale` or a `Locale` in the first parameter.

### Creating a factory for our source

We then need a way to get instances of our source;
create an interface extending `IMessageSourceFactory<CommandReplies>`
and annotate it with `@MessageSourceFactory("MyBotMessages")`,
the `MyBotMessages` string is the name of the bundle we added in the first step.

```kt
@MessageSourceFactory("MyBotMessages")
interface CommandRepliesFactory : IMessageSourceFactory<CommandReplies>
```

Instances of this interface can be injected like any other service,
and will allow you to create `CommandReplies` instances from an `Interaction`.

> [!NOTE]
> You do not need to implement this interface.

### Usage

```kt
@Command
class SlashInfo {

    @JDASlashCommand(
        name = "info",
        description = "Sends info about the bot",
    )
    fun onSlashInfo(event: GuildSlashEvent, replies: CommandReplies) {
        val response = replies.botInfo(
            // Use named parameters to make the arguments clearer!
            guildCount = event.jda.guildCache.size(),
            uptimeTimestamp = TimeFormat.RELATIVE.format(ManagementFactory.getRuntimeMXBean().startTime),
        )

        event.reply(response)
            .setEphemeral(true)
            .queue()
    }
}
```

> [!TIP]
> Injecting the `CommandReplies` instance in the slash command function
> is the same as injecting `CommandRepliesFactory` in your class then using it in your command to create instances of `CommandReplies`.

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
    <artifactId>BotCommands-typesafe-messages-core</artifactId>
    <version>VERSION</version>
  </dependency>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-typesafe-messages-bc</artifactId>
    <version>VERSION</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

### Gradle
```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.freya022:BotCommands-typesafe-messages-core:VERSION")
    runtimeOnly("io.github.freya022:BotCommands-typesafe-messages-bc:VERSION")
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
    <artifactId>BotCommands-typesafe-messages-core</artifactId>
    <version>VERSION</version>
  </dependency>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands-typesafe-messages-spring</artifactId>
    <version>VERSION</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

### Gradle
```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.freya022:BotCommands-typesafe-messages-core:VERSION")
    runtimeOnly("io.github.freya022:BotCommands-typesafe-messages-spring:VERSION")
}
```

</details>

### Snapshots

To use the latest, unreleased changes, see [SNAPSHOTS.md](../SNAPSHOTS.md).
