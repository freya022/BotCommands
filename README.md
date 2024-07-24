[bc-maven-central-shield]: https://img.shields.io/maven-central/v/io.github.freya022/BotCommands?versionPrefix=3
[bc-maven-central-link]: https://mvnrepository.com/artifact/io.github.freya022/BotCommands/latest
[bc-jitpack-shield]: https://img.shields.io/badge/Snapshots-JitPack-blue
[bc-jitpack-link]: https://jitpack.io/#freya022/BotCommands
[bc-releases]: #getting-the-library
[jda-version]: https://img.shields.io/badge/JDA%20Version-5.0.0+-important
[jda-releases]: https://github.com/discord-jda/JDA/releases
[discord-shield]: https://discord.com/api/guilds/848502702731165738/embed.png?style=shield
[discord-invite]: https://discord.gg/frpCcQfvTz
[kdoc-shield]: https://img.shields.io/badge/API_reference-KDoc-blue
[kdoc-link]: https://docs.bc.freya02.dev
[wiki-shield]: https://img.shields.io/badge/Wiki-Home-blue
[wiki-link]: https://bc.freya02.dev/3.X

<img align="right" src="assets/logo.svg" height="150" alt="BotCommands logo">

[![BotCommands version][bc-maven-central-shield]][bc-releases]
[![JDA version][jda-version]][jda-releases]
[![Snapshots][bc-jitpack-shield]][bc-jitpack-link]

[![Discord invite][discord-shield]][discord-invite]
[![Wiki home][wiki-shield]][wiki-link]
[![Documentation][kdoc-shield]][kdoc-link]

# BotCommands
A Kotlin-first (and Java) framework that makes creating Discord bots a piece of cake,
using the [JDA](https://github.com/discord-jda/JDA) library.

## Features
The framework being built around events and dependency injection,
your project can take advantage of that and avoid passing objects around, 
while also easily being able to use services provided by the framework. 

### Commands
* Automatic registration of commands, resolvers, services, etc... with full dependency injection
* Can be used with annotations or with code (in Kotlin)

### Application commands
* Slash commands with automatic & customizable argument processing
  * Supports choices, min/max values/length, channel types and autocomplete
  * Options can be grouped into objects
* Context menu commands (User / Message)
* Automatic, smart application commands registration

<details>
<summary>Example</summary>

```kt
@Command
class SlashBan : ApplicationCommand() {
    @JDASlashCommand(name = "ban", description = "Bans an user")
    suspend fun onSlashBan(
        event: GuildSlashEvent,
        @SlashOption(description = "The user to ban") user: User,
        @SlashOption(description = "Timeframe of messages to delete") timeframe: Long,
        // Use choices that come from the TimeUnit resolver
        @SlashOption(description = "Unit of the timeframe", usePredefinedChoices = true) unit: TimeUnit, // A resolver is used here
        @SlashOption(description = "Why the user gets banned") reason: String = "No reason supplied" // Optional
    ) {
        // ...
        event.reply_("${user.asMention} has been banned for '$reason'", ephemeral = true)
          .deleteDelayed(5.seconds)
          .await()
    }
}
```

![Slash ban example](assets/slash_ban_example.gif)

</details>

### Text commands
* Supports prefix and mentions
* With two parsing modes:
  1. Each parameter is an argument, works the same as slash commands
  2. Manual argument consumption

<details>
<summary>Example</summary>

```kt
@Command
class TextBan : TextCommand() {
    @JDATextCommandVariation(path = ["ban"], description = "Bans the mentioned user")
    suspend fun onTextBan(
        event: BaseCommandEvent,
        @TextOption user: User,
        @TextOption(example = "2") timeframe: Long,
        @TextOption unit: TimeUnit, // A resolver is used here
        @TextOption(example = "Get banned") reason: String = "No reason supplied" // Optional
    ) {
        // ...
        event.reply("${user.asMention} has been banned")
            .deleteDelayed(5.seconds)
            .await()
    }
}
```

Can then be used as `@Bot ban @freya02 1 days A totally valid reason`

Here's how the help content would look with [a subcommand and a few more variations](src/test/kotlin/io/github/freya022/botcommands/test/readme/TextBan.kt):

![Help content example](assets/command_help_embed_example.png)
</details>

### Components and modals
* Unlimited data storage for components, with persistent and ephemeral storage
* Both modals and persistent components have a way to pass data

### Event handlers
* Custom (annotated) event handlers, with priorities and async

### Localization
* Entirely localizable, from the command declaration to the bot responses

### Dependency injection
* Loads everything and passes objects automatically
* Can create custom conditions to disable services/commands at startup
* Can be replaced with Spring IoC

### Utilities
  * A PostgreSQL (and H2) database abstraction, with logged queries
  * An event waiter with (multiple) preconditions, timeouts and consumers for every completion state
  * Message parsers (tokenizers, see `RichTextParser`) and emoji resolvers (turning `:joy:` into 😂)
  * Paginators and menus of different types (using components!)

And way more features!

## Getting Started
You are strongly recommended to have some experience with Kotlin (or Java),
OOP, [JDA](https://github.com/discord-jda/JDA) and Dependency Injection basics before you start using this library.

### Prerequisites
* An [OpenJDK 17+](https://adoptium.net/temurin/releases/?version=21) installation
* A competent IDE (I recommend IntelliJ IDEA, you can't go wrong with it in Java & Kotlin, + Live Templates)
* (Only Java) Enable method parameters names, please refer to the [wiki page](https://bc.freya02.dev/3.X/using-botcommands/parameter-names/)
* (Recommended) Use [HotswapAgent](https://github.com/HotswapProjects/HotswapAgent) in development, to avoid restarting too often
* (Recommended) Use [stacktrace-decoroutinator](https://github.com/Anamorphosee/stacktrace-decoroutinator), to get clearer stack traces in suspending code

Head over to [the wiki](https://bc.freya02.dev/3.X/setup/getting-started/) to get started,
you can also check out the [examples](src/examples).

## Getting the library
[![BotCommands on maven central][bc-maven-central-shield] ][bc-maven-central-link]
### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands</artifactId>
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
    implementation 'io.github.freya022:BotCommands:VERSION'
}
```

Alternatively, you can use Jitpack to use **snapshot** versions, 
you can refer to [the JDA wiki](https://jda.wiki/using-jda/using-new-features/) for more information.

## Sample usage
Here is how you would create a slash command that sends a message in a specified channel.
<details>
<summary>Kotlin</summary>

```kt
@Command
@RequiresComponents // Disables the command if components are not enabled
class SlashSay(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "say", description = "Sends a message in a channel")
    suspend fun onSlashSay(
        event: GuildSlashEvent,
        @SlashOption(description = "Channel to send the message in") channel: TextChannel,
        @SlashOption(description = "What to say") content: String
    ) {
        event.reply_("Done!", ephemeral = true)
            .deleteDelayed(event.hook, 5.seconds)
            .queue()
        channel.sendMessage(content)
            .addActionRow(buttons.danger(EmojiUtils.resolveJDAEmoji("wastebasket")).ephemeral {
                bindTo { buttonEvent ->
                    buttonEvent.deferEdit().queue()
                    buttonEvent.hook.deleteOriginal().await()
                }
            })
            .await()
    }
}
```
</details>

<details>
<summary>Kotlin (DSL)</summary>

```kt
@Command
@RequiresComponents // Disables the command if components are not enabled
class SlashSay(private val buttons: Buttons) : GlobalApplicationCommandProvider {
    suspend fun onSlashSay(event: GuildSlashEvent, channel: TextChannel, content: String) {
        event.reply_("Done!", ephemeral = true)
            .deleteDelayed(event.hook, 5.seconds)
            .queue()
        channel.sendMessage(content)
            .addActionRow(buttons.danger(EmojiUtils.resolveJDAEmoji("wastebasket")).ephemeral {
                bindTo { buttonEvent ->
                    buttonEvent.deferEdit().queue()
                    buttonEvent.hook.deleteOriginal().await()
                }
            })
            .await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("say", function = ::onSlashSay) {
            description = "Sends a message in a channel"

            option("channel") {
                description = "Channel to send the message in"
            }

            option("content") {
                description = "What to say"
            }
        }
    }
}
```
</details>

<details>
<summary>Java</summary>

```java
@Command
@RequiresComponents // Disables the command if components are not enabled
public class SlashSay extends ApplicationCommand {
    private final Buttons buttons;
  
    public SlashSay(Buttons buttons) {
        this.buttons = buttons;
    }

    @JDASlashCommand(name = "say", description = "Sends a message in a channel")
    public void onSlashSay(
            GuildSlashEvent event,
            @SlashOption(description = "Channel to send the message in") TextChannel channel,
            @SlashOption(description = "What to say") String content
    ) {
        event.reply("Done!")
                .setEphemeral(true)
                .delay(Duration.ofSeconds(5))
                .flatMap(InteractionHook::deleteOriginal)
                .queue();

        final Button deleteButton = buttons.danger(EmojiUtils.resolveJDAEmoji("wastebasket")).ephemeral()
                .bindTo(buttonEvent -> {
                    buttonEvent.deferEdit().queue();
                    buttonEvent.getHook().deleteOriginal().queue();
                })
                .build();
        channel.sendMessage(content)
                .addActionRow(deleteButton)
                .queue();
    }
}
```
</details>

## Examples

You can find a number of feature demonstrations in the [examples](src/examples).

## Debugging tips

- Enable the debug/trace logs in your logback.xml file, for a logging tutorial you can look at [the wiki's logging page](https://freya022.github.io/BotCommands/3.X/setup/logging/)
- Look at the switches in `BDebugConfig`

## Live templates

IntelliJ IDEA users can use [live templates](https://www.jetbrains.com/help/idea/using-live-templates.html) provided in [this zip file](BotCommands%203.X%20Live%20Templates.zip),
helping you make commands and other handlers with predefined templates, for both Kotlin and Java, 
keeping a consistent naming scheme and acting as a cheatsheet.

For example, if you type `slashCommand` in your class, this will generate a slash command 
and guide you through the declaration.

A list of live template can be found in `Settings > Editor > Live Templates`,
in the `BotCommands 3.X - [Language]` group.

For an installation guide, you can follow [this guide from JetBrains](https://www.jetbrains.com/help/idea/sharing-live-templates.html#import).

## Support

Don't hesitate to join [the support server](https://discord.gg/frpCcQfvTz) if you have any question!

## Contributing
If you want to contribute, make sure to base your branch on `3.X`, and create your PR from it.

It would be appreciated to focus on improving the documentation,
such as the [wiki](wiki), the library documentation, or by [creating examples](src/examples).<br>
Maintainers will focus on bug reports and feature requests, which you can create issues for. 

Read [the contributing guide](.github/CONTRIBUTING.md) for more details.
