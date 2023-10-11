<img align="right" src="assets/logo.svg" height="200" alt="BotCommands logo">

[![](https://img.shields.io/maven-central/v/io.github.freya022/BotCommands?versionPrefix=3)](#getting-the-library)
[![](https://img.shields.io/badge/JDA%20Version-5.0.0--beta.11+-important)](https://github.com/discord-jda/JDA/releases)
[![image](https://discord.com/api/guilds/848502702731165738/embed.png?style=shield)](https://discord.gg/frpCcQfvTz)
[![image](https://img.shields.io/badge/Javadocs-Overview-blue)](https://freya022.github.io/BotCommands/)
[![image](https://img.shields.io/badge/Wiki-Home-blue)](https://freya022.github.io/BotCommands-Wiki/3.X)

# BotCommands
A Kotlin-first (and Java) framework that makes creating Discord bots a piece of cake,
using the [JDA](https://github.com/discord-jda/JDA) library.

## Features

* Automatic registration of commands, resolvers, services, etc... with full dependency injection
* Text commands (annotated or manually declared), either with:
  * Automatic token parsing into your parameters
    * Suppose the prefix is `!` and the command is `ban`
      ```kt
      @JDATextCommand(name = "ban")
      fun onTextBan(event: BaseCommandEvent,
                    @TextOption user: User,
                    @TextOption timeframe: Long,
                    @TextOption unit: TimeUnit, // A resolver is used here
                    @TextOption reason: String) {
          //Ban the user
      }
      ```
    * Which can be used as: `!ban @freya02 7 days Get banned`
  * Manual token consuming
* Application commands (annotated or manually declared)
  * Slash commands with **automatic & customizable argument parsing**
    * Options can be grouped into _aggregates_, as to benefit from methods specifically on them 
    * Also supports choices, min/max values/length, channel types and autocomplete
  * Context menu commands (User / Message)
  * **Automatic smart** application commands registration
  * Entirely localizable, from the command declaration to the bot responses
* Customizable and localizable error messages
* Custom (annotated) event handlers, with priorities and async
* Modals
* _Unlimited_ data storage for components, *with persistent and ephemeral storage*
  * **They can also receive additional arguments** the same way as slash commands do
* Several utilities such as:
  * An event waiter with (multiple) preconditions, timeouts and consumers for every completion state
  * Message parsers (tokenizers, see `RichTextParser`) and emoji resolvers (turning `:joy:` into 😂)
  * Paginators and menus of different types (using components!)

While every event runs on their own coroutine,
you still need to be mindful in not blocking your bot.

## Getting Started
You are strongly recommended to have some experience with Kotlin (or Java),
OOP, [JDA](https://github.com/discord-jda/JDA) and Dependency Injection basics before you start using this library.

### Prerequisites
* An [OpenJDK 17+](https://adoptium.net/temurin/releases/?version=17) installation
* A competent IDE (I recommend IntelliJ IDEA, you can't go wrong with it in Java & Kotlin, + Live Templates)
* (Recommended, only Java) Enable method parameters names, please refer to the [wiki page](https://freya022.github.io/BotCommands-Wiki/3.X/using-commands/Inferred-option-names/)
* (Recommended) Use [HotswapAgent](https://github.com/HotswapProjects/HotswapAgent) in development, to avoid restarting too often

You can then head over to [the wiki](https://freya022.github.io/BotCommands-Wiki/3.X/setup/getting-started/) 
to get started using a bot template.

[//]: # (TODO keep an eye out for this wiki link)

## Getting the library
[![](https://img.shields.io/maven-central/v/io.github.freya022/BotCommands)](https://mvnrepository.com/artifact/io.github.freya022/BotCommands/latest)
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
class SlashSay : ApplicationCommand() {
    @JDASlashCommand(name = "say", description = "Sends a message in a channel")
    suspend fun onSlashSay(
        event: GuildSlashEvent,
        @SlashOption(description = "Channel to send the message in") channel: TextChannel,
        @SlashOption(description = "What to say") content: String
    ) {
        event.reply_("Done!", ephemeral = true)
            .delay(5.seconds)
            .flatMap(InteractionHook::deleteOriginal)
            .queue()
        channel.sendMessage(content).await()
    }
}
```
</details>

<details>
<summary>Kotlin (DSL)</summary>

```kt
@Command
class SlashSayDsl {
    suspend fun onSlashSay(
        event: GuildSlashEvent,
        channel: TextChannel,
        content: String
    ) {
        event.reply_("Done!", ephemeral = true)
            .delay(5.seconds)
            .flatMap(InteractionHook::deleteOriginal)
            .queue()
        channel.sendMessage(content).await()
    }
  
    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("say_dsl", function = ::onSlashSay) {
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
public class SlashSay extends ApplicationCommand {
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
        channel.sendMessage(content).queue();
    }
}
```
</details>

## Examples

You can find a number of feature demonstrations in the [examples subproject](examples).

## Debugging tips

- Enable the debug/trace logs in your logback.xml file, for a logging tutorial you can look at [the wiki's logging page](https://freya022.github.io/BotCommands-Wiki/3.X/setup/logging/)
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

## Building / Installing manually

While I don't recommend, you can see [BUILDING.md](BUILDING.md)