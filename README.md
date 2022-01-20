![](https://img.shields.io/badge/JDA%20Version-Read_notice-important)
![](https://img.shields.io/badge/Version-Use_latest_commit-informational)
[![image](https://discordapp.com/api/guilds/848502702731165738/embed.png?style=shield)](https://discord.gg/frpCcQfvTz)

## Notice

For the JDA dependency, you will need a special build of JDA, which you can find with group id `com.github.freya022` and version `e38137ecf6`, this is the interaction rework branch of JDA

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
    * This also includes command privileges (permissions) 
  * These commands as well as their options and choices **can also be localized** (per-guild language)
* A JDA **event waiter** with (multiple) preconditions, timeouts and consumers for every completion states 
* Secure and unique components (buttons / selection menus) IDs *with persistent and non-persistent storage*
  * **They can also receive additional arguments** the same way as slash commands do
* Message parsers (tokenizers, see `RichTextParser`) and emoji resolvers (can turn \:joy\: into 😂)
* Paginators and menus of different types (using buttons !)
* Flexible constructors for your commands and injectable fields

Note that text-based commands, slash commands and component handlers are running in separate threads from JDA as to not block the websocket, keep in mind that this does not allow you to have bad practises as described in [how to use RestAction(s)](https://github.com/DV8FromTheWorld/JDA/wiki/7%29-Using-RestAction) 

## Getting Started
You are recommended to have some experience with Java, OOP in general and [JDA](https://github.com/DV8FromTheWorld/JDA) before you start using this library

### Prerequisites
[OpenJDK 16+](https://adoptium.net/releases.html?variant=openjdk16&jvmVariant=hotspot) <br>
An IDE which supports Maven projects (I strongly recommend you use IntelliJ, it will be useful to gain time with Live Templates)

## Getting the library
### Installing with Jitpack

<details>
<summary>With Maven</summary>

You can add the following to your pom.xml
```xml
<repositories>
  <repository>
      <id>jitpack</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>

...

<dependencies>
  <dependency>
    <groupId>com.github.freya022</groupId>
    <artifactId>BotCommands</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>
```
</details>

<details>
<summary>With Gradle</summary>

You can add the following to your pom.xml
```gradle
dependencies {
    implementation 'com.github.freya022:BotCommands:VERSION'
    ...
}

...

repositories {
    maven {
        name 'jitpack'
        url 'https://jitpack.io'
    }
    ...
}
```
</details>

### Building / Installing manually

See [BUILDING.md](BUILDING.md)

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
    "com.freya02.bot.commands"  // This is the package name with contains all your commands / handlers...
); 
```

## How do I make commands ?
See the [wiki](https://github.com/freya022/BotCommands/wiki), you got a page for each type of command (regular prefixed / regex prefixed / slash commands)

## Some debugging tools

- Enable the debug/trace logs in your logback.xml file, for a logging tutorial you can look at [the wiki's logging page](https://github.com/freya022/BotCommands/wiki/Logging)
- There are also some switches in `DebugBuilder`, if you ever need them
- To test your application commands you can use the `@Test` annotation

## Replacing help content

You can disable the prefixed help command, these methods are in `TextCommandsBuilder`, if you do disable prefixed help commands you need to supply your own implementation when commands are detected, but their syntax is invalid

The provided implementation could just do nothing (such as `e -> {}`) if you want to just remove any form of help message

## Examples

You can find example bots in the [examples](Examples) folder

## Template bot

To get started with the framework, you can also clone this repo and extract the `BotTemplate` folder and use it as a bot template, of course, be sure to replace the group id as well as the artifact name, as well as providing a valid config file

## Support

The [JDA guild](https://discord.gg/jda) is not a place where you should ask for support on this framework, if you need support please join [this guild instead (link)](https://discord.gg/frpCcQfvTz)
