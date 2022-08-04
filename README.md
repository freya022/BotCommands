[![](https://img.shields.io/maven-central/v/io.github.freya022/BotCommands)](#getting-the-library)
[![](https://img.shields.io/badge/JDA%20Version-5.0.0--alpha.16-important)](https://github.com/DV8FromTheWorld/JDA/releases)
[![image](https://discordapp.com/api/guilds/848502702731165738/embed.png?style=shield)](https://discord.gg/frpCcQfvTz)
[![image](https://img.shields.io/badge/Wiki-Home-blue)](https://freya022.github.io/BotCommands-Wiki/)

# BotCommands
This framework simplifies the creation of Discord bots with the [JDA](https://github.com/DV8FromTheWorld/JDA) library.

## Features

* Automatic command registration
* Text based commands, with 2 ways of working:
  * More manual parsing, you have a tokenized message, and you choose how to process the tokens
  * Automatic parsing of the arguments, your method signature is translated into a command syntax, for example:
    * Suppose the prefix is `!` and the command is `ban`
      ```java
      @JDATextCommand(name = "ban")
      public void runBan(BaseCommandEvent event,
                         @TextOption User user,
                         @TextOption long delDays,
                         @TextOption String reason) {
          //Ban the user
      }
      ```
    * Which means `!ban @someone 42 Foobar` should be valid
* Application commands
  * Slash commands with **automatic & customizable argument parsing** (see wiki to add parsers)
    * Also supports choices, min/max values, channel types and autocompletion
  * Context menu commands (User / Message)
  * Application commands are **automatically registered on Discord on startup**
    * This also includes command permissions
  * These commands as well as their options and choices **can also be localized** (per-guild language)
* A JDA **event waiter** with (multiple) preconditions, timeouts and consumers for every completion states 
* Secure and unique components (buttons / selection menus) IDs *with persistent and non-persistent storage*
  * **They can also receive additional arguments** the same way as slash commands do
* Message parsers (tokenizers, see `RichTextParser`) and emoji resolvers (can turn `:joy:` into 😂)
* Paginators and menus of different types (using buttons !)
* Flexible constructors for your commands and injectable fields

Note that text-based commands, slash commands and component handlers are running in separate threads from JDA as to not block the websocket, keep in mind that this does not allow you to have bad practises as described in [how to use RestAction(s)](https://jda.wiki/using-jda/using-restaction/) 

## Getting Started
You are recommended to have some experience with Java, OOP in general and [JDA](https://github.com/DV8FromTheWorld/JDA) before you start using this library

### Prerequisites
* An [OpenJDK 17](https://adoptium.net/temurin/releases/?version=17) installation
* Enable preview features in your compiler with `--enable-preview`, [How I enable it](https://github.com/freya022/BotCommands/blob/c537adba0619a2d74767796b1aec60a9c8ee720b/pom.xml#L74-L81), [IntelliJ w/ Maven tutorial](https://www.baeldung.com/java-preview-features#intellij-idea), [Gradle tutorial](https://stackoverflow.com/questions/55433883/how-to-enable-java-12-preview-features-with-gradle)
* An IDE which supports Maven projects (I strongly recommend you use IntelliJ, it could be useful to save time with Live Templates)

## Getting the library
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
dependencies {
    implementation 'io.github.freya022:BotCommands:VERSION'
}
```

Alternatively, you can use jitpack to use snapshot versions, you can refer to [this wiki](https://jda.wiki/using-jda/using-new-features/) for more information

### Building / Installing manually

While I don't recommend, you can see [BUILDING.md](BUILDING.md)

**You're now ready to start coding!**

## How to use
You first need to get your JDA instance:
```java
final JDA jda = JDABuilder.create(token, /* GatewayIntents here */)
		/* Other options */
		.build();

jda.awaitReady();
```
Once you have your JDA instance ready, you can use the `CommandsBuilder` class to start using the library.<br>
There is 2 text-based command triggers:
* Ping-as-prefix: Triggers commands when your bot is mentioned (e.g: `@YourBot`)
* Custom-prefix: Triggers commands when any message contains your selected prefix (e.g: `!`)

(Of course you can still use only slash commands if you wish)

You can build a CommandsBuilder instance with `CommandsBuilder#newBuilder` and supply it the bot owner id, which should be your user ID

Additionally, the ids of the Discord users are those who can use the bot freely and receive messages when an uncaught exception happens

You should have some code that looks like this now:
```java
final CommandsBuilder commandsBuilder = CommandsBuilder.newBuilder(222046562543468545L);
```

You can also add 
```java
commandsBuilder.textCommandBuilder(textCommandsBuilder -> textCommandsBuilder
    .addPrefix(":")
)
```

To add a prefix for text based commands, this will also disable ping-as-prefix

### Building the framework
You can now build the framework
```java
commandsBuilder.build(
    jda,                        // The JDA instance you just built 
    "com.freya02.bot.commands"  // This is the package name which contains all your commands / handlers...
); 
```

## How do I make commands ?
See the [wiki](https://freya022.github.io/BotCommands-Wiki/using-commands/using-slash-commands/Slash-commands/), you got a page for each type of command (regular prefixed / regex prefixed / slash commands)

## Some debugging tools

- Enable the debug/trace logs in your logback.xml file, for a logging tutorial you can look at [the wiki's logging page](https://freya022.github.io/BotCommands-Wiki/Logging)
- There are also some switches in `DebugBuilder`, if you ever need them
- To test your application commands you can use the `@Test` annotation

## Replacing help content

You can disable the prefixed help command, these methods are in `TextCommandsBuilder`, if you do disable prefixed help commands you need to supply your own implementation when commands are detected, but their syntax is invalid

The provided implementation could just do nothing (such as `e -> {}`) if you want to just remove any form of help message

## Examples

You can find example bots in the [examples](examples) folder

## Template bot

To get started with the framework, you can also clone this repo and extract the `BotTemplate` folder and use it as a bot template, of course, be sure to replace the group id as well as the artifact name, as well as providing a valid config file

## Live templates

If you use IntelliJ, you can use the "live templates" provided [in live_templates.zip](live_templates.zip), this will help you make commands and handlers with predefined templates and ask you to complete them

For example: if you type `slash` in your class, this will generate a slash command declaration and ask you to complete the command name, description, etc... Of course there are many more templates, you can see all of these in `Settings > Editor > Live Templates` and in the `BotCommands` group 

If you don't know how to install live templates, you can follow [this guide from JetBrains](https://www.jetbrains.com/help/idea/sharing-live-templates.html#import)

## Support

The [JDA guild](https://discord.gg/jda) is not a place where you should ask for support on this framework, if you need support please join [this guild instead (link)](https://discord.gg/frpCcQfvTz)
