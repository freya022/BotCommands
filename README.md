[bc-maven-central-shield]: https://img.shields.io/maven-central/v/io.github.freya022/BotCommands?label=Maven%20central&logo=apachemaven&versionPrefix=3
[bc-maven-central-link]: https://central.sonatype.com/artifact/io.github.freya022/BotCommands
[bc-jitpack-shield]: https://img.shields.io/badge/Snapshots-blue?logo=jitpack
[bc-jitpack-link]: https://jitpack.io/#freya022/BotCommands
[bc-releases]: #installation
[jda-version]: https://img.shields.io/badge/JDA-5.6.1+-important?logo=data:image/svg%2bxml;base64,PHN2ZwogICB2ZXJzaW9uPSIxLjEiCiAgIGlkPSJzdmczNzMxIgogICB3aWR0aD0iMjQ3LjQ5NzYyIgogICBoZWlnaHQ9IjE4MC4zMjIxNiIKICAgdmlld0JveD0iMCAwIDI0Ny40OTc2MiAxODAuMzIyMTYiCiAgIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgPHBhdGgKICAgICBzdHlsZT0iZmlsbDojZmZmZmZmO3N0cm9rZS13aWR0aDoxLjMzMzMzIgogICAgIGQ9Ik0gNDcuMTc4MTUzLDE3OC4yODk5NiBDIDMyLjkxNzg0MiwxNzUuMTk3NDEgMjAuMjA5MDc0LDE2OC4yOTI1MiA5LjUxMDEzODgsMTU3LjgyNDI5IEwgLTIuMTgwMTUwNGUtNiwxNDguNTE5MjMgMC44NjgzMDU4MiwxMzMuMzE2MTkgQyAyLjczMjU1NTgsMTAwLjY3NTM5IDEyLjI4Nzk4MSw2Mi4xMTU4MSAyNS43NTg2MzksMzIuODc0NzUgMzEuMzg1NDE0LDIwLjY2MDU4MSAzNS41NjU1NDYsMTYuOTkzMzIxIDUzLjA5MDUyNiw4Ljg5NjI4MzYgNjcuMTc3ODM2LDIuMzg3NTQzNiA4Ni42NjQ3MDYsLTEuNzAyODI1NCA5MC45NTQ2MDYsMC45NDg0NzE1NSA5Mi4yMTYwNDYsMS43MjgwODM2IDg5LjAzMzc4NiwzLjQyMjY1OTYgODAuNzYyMjU2LDYuMzc1OTkxNiA2Ny42NTc5MzYsMTEuMDU0ODUyIDUwLjA4MDM2MSwxOS45ODE5MzMgNDMuNzQ4ODAxLDI1LjE3MzkzIDM5LjkwOTU4NiwyOC4zMjIxNSA0MC4xMDk4ODksMjguMjkzODggNDguNzMxOTA0LDI0LjQ3MDY2IDcyLjgzNzMwNiwxMy43ODE3MDEgOTUuMzI4ODA2LDkuMjA3MzY3NiAxMjMuNzQ4ODEsOS4yMTM2Nzc2IGMgMjYuNjY5NTQsMC4wMDU5IDQ3LjM3NDMyLDQuMzA4MDU4NCA2OS44NjcxOCwxNC41MTczMzI0IDkuMTcwODgsNC4xNjI1NyA5Ljc1MzA4LDQuMjgyOTIgNi4xMzI4MiwxLjI2NzczIC01LjMxOTQzLC00LjQzMDM0NyAtMjQuNTM3NzksLTE0LjI0Nzg0MiAtMzUuMzMzMzQsLTE4LjA0OTY1NDQgLTEwLjQzMTYsLTMuNjczNjQ0IC0xMC4wNzIwMSwtMy40MjUxNzcgLTguMjA3ODYsLTUuNjcxMzM4IDIuODkzMDgsLTMuNDg1OTQ3IDIyLjczMzAyLDAuNTExMTg0IDM4LjQ0MDczLDcuNzQ0NjEgMTcuMjQ1MSw3Ljk0MTM5MzQgMjEuNDc3NTQsMTEuNjY3OTA3NCAyNy4wOTA2MywyMy44NTIzOTI0IDEzLjQ3MDY3LDI5LjI0MTA2IDIzLjAyNjA4LDY3LjgwMDY0IDI0Ljg5MDMzLDEwMC40NDE0NCBsIDAuODY4MzEsMTUuMjAzMDQgLTkuNTEwMTMsOS4zMDUwNiBjIC0xMC44OTI5NiwxMC42NTgwNyAtMjMuNDE5NzEsMTcuMzczOTIgLTM4LjI3NjkzLDIwLjUyMTA4IC0xNS41NjQ1LDMuMjk2OTkgLTE2LjQyOTI2LDMuMDc5MjYgLTIzLjYyODQxLC01Ljk0OTI1IC02LjU3MjkzLC04LjI0MzE3IC03LjYwOTc1LC0xMC43MDIxNiAtNC41MTI0OCwtMTAuNzAyMTYgMy44MzIyLDAgMjAuOTU4MDgsLTkuNjkzNTcgMjcuNDcwNiwtMTUuNTQ4ODkgMy43NDY4NSwtMy4zNjg3NCA2LjYwNTk3LC02LjMzMTQ3IDYuMzUzNiwtNi41ODM4MyAtMC4yNTIzNywtMC4yNTIzNyAtNC45MjU3NSwxLjg2NzIxIC0xMC4zODUyOSw0LjcxMDIgLTM3LjM1MjQsMTkuNDUwNzcgLTc5LjE1MzcyLDIyLjcwNzEyIC0xMTkuMjExMjQ0LDkuMjg2NjMgLTYuNTc5NzMsLTIuMjA0NDIgLTE2LjMzMjM2LC02LjI5NzUgLTIxLjY3MjUsLTkuMDk1NzQgLTUuMzQwMTQ3LC0yLjc5ODI0IC05LjcwOTM1OCwtNC42MzU2OCAtOS43MDkzNTgsLTQuMDgzMiAwLDEuNTAxNDggOS4yNzQxNzgsOS41Nzk0NCAxNS4xODk0OTgsMTMuMjMwMzIgMi44NTQyMywxLjc2MTU5IDguNDAyOTgsNC41MDg5NiAxMi4zMzA1Niw2LjEwNTI0IGwgNy4xNDEwNiwyLjkwMjM1IC02Ljk5NjE5LDguODcxNzkgYyAtNy45MTk3MiwxMC4wNDI5MiAtOC44MTk5OCwxMC4yODg4IC0yNC45MDIyNDMsNi44MDExMyB6IG0gMjAuNTg0MjczLC02MS4xODI0MyBjIDguNjE5OTMsLTMuNjAxNjQgOS45NzU3NCwtNy45MTM5MyA5Ljk4MTM5LC0zMS43NDY5IGwgMC4wMDUsLTIxIEggNjQuNDE1NDY2IDUxLjA4MjEzNSB2IDQuNjY2NjYgNC42NjY2NyBoIDYuNjY2NjcxIDYuNjY2NjYgdiAxNC42NjY2NyBjIDAsMTMuMTgwNCAtMC4yODczNiwxNC45NTQwMiAtMi44MzU3NSwxNy41MDI0MSAtMi40MjA1OCwyLjQyMDU5IC0zLjg4NDY0LDIuNzI3NTEgLTEwLDIuMDk2MzUgbCAtNy4xNjQyNDgsLTAuNzM5NDIgdiA1LjEyNTg4IGMgMCwyLjg1MzE1IDAuNzM4OTc0LDUuNDI0MDcgMS42NjY2NjYsNS43OTg0IDMuNTc0ODksMS40NDI1IDE3LjMxOTcyMiwwLjc4NTI0IDIxLjY4MDI5MiwtMS4wMzY3MiB6IG0gNTkuOTg2Mzg0LC0yLjg3MjQyIGMgMTQuODY5NTgsLTcuNzQ2OTYgMTguNDAyMzIsLTI4LjM5NzIzIDcuMDY2NjksLTQxLjMwNzc5IC01LjM3Nzk5LC02LjEyNTE5IC0xNC4xMTA5NSwtOC41NjY2OSAtMzAuNjQyMDMsLTguNTY2NjkgSCA5MS4wODIxMzYgdiAyNi44MDcxIDI2LjgwNzEgbCAxNS42NjY2NzQsLTAuNDgwNTUgYyAxMi42NDI4NCwtMC4zODc4IDE2LjY5NjA1LC0xLjAxNjg1IDIxLC0zLjI1OTE3IHogTSAxMDQuNDE1NDcsOTEuMDI3MjkgdiAtMTYgaCA1LjgwMjgzIGMgMTEuODUxOCwwIDE5LjAwMjM2LDkuMzA3NSAxNi4wOTksMjAuOTU1MTYgLTEuNjM0NjksNi41NTc5OSAtOC4xNzQ3NSwxMS4wNDQ4NCAtMTYuMDk5LDExLjA0NDg0IGggLTUuODAyODMgeiBtIDU4LjUwOTE0LDIxLjMzMzM0IDEuNTk3OSwtNS4zMzMzNCBoIDkuODkyOTYgOS44OTI5NiBsIDEuNTk3OTEsNS4zMzMzNCAxLjU5NzkxLDUuMzMzMzMgaCA3LjA2NDcgNy4wNjQ3MSBsIC00Ljc0NzEyLC0xMyBjIC0yLjYxMDkxLC03LjE1IC02LjkzNSwtMTkuMTQ4NzUgLTkuNjA5MDgsLTI2LjY2Mzg3IGwgLTQuODYxOTksLTEzLjY2Mzg4IGggLTggLTggbCAtNC44NjE5OCwxMy42NjM4OCBjIC0yLjY3NDA4LDcuNTE1MTIgLTYuOTk4MTgsMTkuNTEzODcgLTkuNjA5MDgsMjYuNjYzODcgbCAtNC43NDcxMiwxMyBoIDcuMDY0NyA3LjA2NDcxIHogbSA2LjI2Mjc2LC0xNy42NjY2NyBjIDAuNTIzMzcsLTEuNjUgMS45Mzc4LC01LjkwMjEzIDMuMTQzMTYsLTkuNDQ5MiBsIDIuMTkxNTcsLTYuNDQ5MTkgMi40NTE1NSw3Ljc4MjUyIGMgMS4zNDgzNCw0LjI4MDQgMi43Mzg1NCw4LjUzMjU0IDMuMDg5MzMsOS40NDkyIDAuNDU0MDgsMS4xODY2IC0xLjE1NzQzLDEuNjY2NjcgLTUuNTk0NzEsMS42NjY2NyAtNS44NTY0LDAgLTYuMTc1MDgsLTAuMTgxMDQgLTUuMjgwOSwtMyB6IiAvPgo8L3N2Zz4K
[jda-releases]: https://github.com/discord-jda/JDA/releases
[discord-shield]: https://img.shields.io/discord/848502702731165738?logo=discord&logoColor=white&color=e0e3ff&label=Chat
[discord-invite]: https://discord.gg/frpCcQfvTz
[kdoc-shield]: https://img.shields.io/badge/KDoc-Docs-blue?logo=kotlin&labelColor=2b303b
[kdoc-link]: https://docs.bc.freya02.dev
[wiki-shield]: https://img.shields.io/badge/Wiki-Home-blue?logo=materialformkdocs&labelColor=2b303b
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

Head over to [the wiki](https://bc.freya02.dev/3.X/setup/getting-started/) to get started,
you can also check out the [examples](src/examples).

## Installation
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
    implementation("io.github.freya022:BotCommands:VERSION")
}
```

Alternatively, you can use Jitpack to use **snapshot** versions, 
you can refer to [the JDA wiki](https://jda.wiki/using-jda/using-new-features/) for more information.

## Modules
The base `BotCommands` artifact will include modules often used, while others are optional.

### Default modules
- [`BotCommands-core`](./BotCommands-core): Root module which contains most features

### Optional modules
- [`BotCommands-spring`](./BotCommands-spring): Support for Spring Boot

## Sample usage
Here is how you would create a slash command that sends a message in a specified channel.
<details>
<summary>Kotlin</summary>

```kt
@Command
@RequiresComponents // (Optional) Disables the command if components are not enabled
class SlashSay(
    private val buttons: Buttons // Factory for buttons
) : ApplicationCommand() {

    // The descriptions can also be moved to localization files, reducing noise
    @JDASlashCommand(name = "say", description = "Sends a message in a channel")
    suspend fun onSlashSay(
        event: GuildSlashEvent,
        @SlashOption(description = "Channel to send the message in") channel: TextChannel,
        @SlashOption(description = "What to say") content: String
    ) {
        val deleteButton = buttons.danger(UnicodeEmojis.WASTEBASKET).ephemeral {
            bindTo { buttonEvent ->
                buttonEvent.deferEdit().queue()
                buttonEvent.hook.deleteOriginal().await()
            }
        }

        event.reply_("Done!", ephemeral = true)
            .deleteDelayed(5.seconds)
            .queue()

        channel.sendMessage(content)
            .addActionRow(deleteButton)
            .await()
    }
}
```
</details>

<details>
<summary>Kotlin (DSL)</summary>

```kt
@Command
@RequiresComponents // (Optional) Disables the command if components are not enabled
class SlashSay(
    private val buttons: Buttons // Factory for buttons
) : GlobalApplicationCommandProvider {

    suspend fun onSlashSay(event: GuildSlashEvent, channel: TextChannel, content: String) {
        val deleteButton = buttons.danger(UnicodeEmojis.WASTEBASKET).ephemeral {
            bindTo { buttonEvent ->
                buttonEvent.deferEdit().queue()
                buttonEvent.hook.deleteOriginal().await()
            }
        }

        event.reply_("Done!", ephemeral = true)
            .deleteDelayed(5.seconds)
            .queue()

        channel.sendMessage(content)
            .addActionRow(deleteButton)
            .await()
    }

    // This is nice if you need to run your own code to declare commands.
    // For example, a loop to create commands based on an enum
    // If you don't need any dynamic stuff, just stick to annotations
    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("say", function = ::onSlashSay) {
            // The descriptions can also be moved to localization files
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
@RequiresComponents // (Optional) Disables the command if components are not enabled
public class SlashSayJava extends ApplicationCommand {

    private final Buttons buttons; // Factory for buttons

    public SlashSay(Buttons buttons) {
        this.buttons = buttons;
    }

    // The descriptions can also be moved to localization files, reducing noise
    @JDASlashCommand(name = "say", description = "Sends a message in a channel")
    public void onSlashSay(
            GuildSlashEvent event,
            @SlashOption(description = "Channel to send the message in") TextChannel channel,
            @SlashOption(description = "What to say") String content
    ) {
        final Button deleteButton = buttons.danger(UnicodeEmojis.WASTEBASKET).ephemeral()
                .bindTo(buttonEvent -> {
                    buttonEvent.deferEdit().queue();
                    buttonEvent.getHook().deleteOriginal().queue();
                })
                .build();

        event.reply("Done!")
                .setEphemeral(true)
                .delay(Duration.ofSeconds(5))
                .flatMap(InteractionHook::deleteOriginal)
                .queue();

        channel.sendMessage(content)
                .addActionRow(deleteButton)
                .queue();
    }
}
```
</details>

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

## Breaking changes

Due to the nature of JDA (and the Discord API), and to always improve the developer experience,
the library could introduce breaking changes to allow quick adoption of newer features and better practices.

While attempting to reduce breaking changes by using deprecation mechanisms,
it is not always possible or practical to use deprecations.

Breaking/deprecating changes should be noticed via an increase of the **minor** version. (`3.0.0` -> `3.1.0`)

## Contributing
If you want to contribute, make sure to base your branch on `3.X`, and create your PR from it.

It would be appreciated to focus on improving the documentation,
such as the [wiki](https://github.com/freya022/BotCommands-Wiki/), the library documentation, or by [creating examples](src/examples).<br>
Maintainers will focus on bug reports and feature requests, which you can create issues for. 

Read [the contributing guide](.github/CONTRIBUTING.md) for more details.
