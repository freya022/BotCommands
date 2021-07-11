![](https://img.shields.io/badge/JDA%20Version-4.3.0__296-important)
![](https://img.shields.io/badge/Version-1.4.6-informational)
[![image](https://discordapp.com/api/guilds/848502702731165738/embed.png?style=shield)](https://discord.gg/frpCcQfvTz)

# BotCommands
This library aims at simplifying Discord bots creation with the [JDA](https://github.com/DV8FromTheWorld/JDA) library.

## Nice to have

The framework mainly automates these:
* Help content
* Command registration
* Permission checks
* Cooldown
* Message parsing (and mapping message to command)

It also helps in:
* [Waiting for events with EventWaiter](src/main/java/com/freya02/botcommands/waiter/EventWaiter.java)
* [Resolving Discord entities](src/main/java/com/freya02/botcommands/utils/RichTextFinder.java) and [emojis](src/main/java/com/freya02/botcommands/utils/EmojiUtils.java)
* Having (secure) button ids with persistent data or non-persistent consumers
* Having pagination (using buttons) and menus in [Paginator](https://github.com/freya022/BotCommands/blob/master/src/main/java/com/freya02/botcommands/menu/Paginator.java) and [Menu](https://github.com/freya022/BotCommands/blob/master/src/main/java/com/freya02/botcommands/menu/Menu.java)

Note that commands are running in separate threads from JDA as to not block the websocket, keep in mind that this does not allow you to have bad practises as described in [how to use RestAction(s)](https://github.com/DV8FromTheWorld/JDA/wiki/7%29-Using-RestAction) 

## Getting Started
You are recommended to have some experience with Java and [JDA](https://github.com/DV8FromTheWorld/JDA) before you start using this library

### Prerequisites
[OpenJDK 11+](https://adoptopenjdk.net/) <br>
An IDE which supports Maven projects (like IntelliJ) or install [Maven](https://maven.apache.org/download.cgi) manually <br>
**Do not forget to add the Maven bin directory to your PATH environment variables**, if you choose not to use an IDE

## Getting the library
### Installing with Jitpack

<details>
<summary>Maven XML - How to add the library using JitPack</summary>

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.me</groupId>
    <artifactId>TestBot</artifactId>
    <version>1.0-SNAPSHOT</version>

    <build>
        <!-- Possible build properties -->
    </build>
    <repositories>
        <repository> <!-- JDA repository -->
            <id>dv8tion</id>
            <name>m2-dv8tion</name>
            <url>https://m2.dv8tion.net/releases</url>
        </repository>
        <repository> <!-- for BotCommands and other libs perhaps -->
            <id>jitpack</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    <dependencies>
        <!-- Your other project's dependencies here -->
        
        <dependency> <!-- Add JDA to your project -->
            <!-- Uncomment this and comment the groupId below it if you use JDA snapshots -->
            <!-- <groupId>com.github.DV8FromTheWorld</groupId> -->
            <groupId>com.github.DV8FromTheWorld</groupId>
            <artifactId>JDA</artifactId>
            <version>JDA-Version</version>
        </dependency>
        <dependency> <!-- Add BotCommands to your project -->
            <groupId>com.github.freya022</groupId> <!-- Different if you choose to build from source -->
            <artifactId>BotCommands</artifactId>
            <version>VERSION</version>
        </dependency>
    </dependencies>
</project>
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
There is 2 command triggers:
* Ping-as-prefix: Triggers commands when your bot is mentioned (e.g: `@YourBot`)
* Custom-prefix: Triggers commands when any message contains your selected prefix (e.g: `!`)

They can be used respectively by `CommandsBuilder#withPing(...)` and `CommandsBuilder#withPrefix("YourPrefixHere", ...)`

Notice these `...` are the `ownerIds` parameters, these are the ids of the Discord users who can use the bot freely and receive messages when an uncaught exception happens

You should have some code that looks like this now:
```java
final CommandsBuilder commandsBuilder = CommandsBuilder.withPing("!", 222046562543468545L);
```

<details>
<summary>Optional - How to set a default embed</summary>

The library uses a default embed for the `help` command and can also be requested in `BaseCommandEvent#getDefaultEmbed`<br>
You can supply a default embed by doing something like this
```java
final SelfUser selfUser = jda.getSelfUser();
EmbedBuilder builder = new EmbedBuilder();
builder.setAuthor(selfUser.getName(), null, selfUser.getEffectiveAvatarUrl());

final Supplier<EmbedBuilder> embedSupplier = () -> new EmbedBuilder(builder).setTimestamp(Instant.now());
```

You will then set the default embed later.
</details>

### Building the ListenerAdapter
You can now build the framework
```java
commandsBuilder
    .setDefaultEmbedFunction(embedSupplier, () -> null) /* Optional, can replace the 2nd argument with an icon supplier and setting a footer icon's URL as "attachment://icon.jpg" */
    .build(jda, "com.freya02.bot.commands"); /* This is the package name with contains all your Command(s) */
```

## How do I make commands ?
See the [wiki](https://github.com/freya022/BotCommands/wiki), you got a page for each type of command (regular prefixed / regex prefixed / slash commands)

## Some debugging tools

- Enable the debug logs in your logback.xml file, for a logging tutorial you can look at [JDA's FAQ take at logging](https://github.com/DV8FromTheWorld/JDA/wiki/Logging-Setup#how-to-enable-debug-logs)
- [CommandsBuilder#updateCommandsOnGuildIds](src/main/java/com/freya02/botcommands/CommandsBuilder.java) - Updates the slash commands only in these guild IDs, useful for testing things without using another token

## Replacing help content

You can disable the prefixed help command and/or the `/help` command, these methods are in CommandsBuilder, if you do disable prefixed help commands you need to supply your own implementation when commands are detected, but their syntax is invalid

The provided implementation could just do nothing (such as `e -> {}`) if you want to just remove any form of help message

## Examples

You can find example bots in the [examples](examples) folder

## Support

The [JDA guild](https://discord.gg/jda) is not a place where you should ask for support on this framework, if you need support please join [this guild instead (link)](https://discord.gg/frpCcQfvTz)