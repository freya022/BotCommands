![](https://img.shields.io/badge/JDA%20Version-Read_notice-important)
![](https://img.shields.io/badge/Version-Use_latest_commit-informational)
[![image](https://discordapp.com/api/guilds/848502702731165738/embed.png?style=shield)](https://discord.gg/frpCcQfvTz)

## Notice

Please use group id `com.github.aasmart` and version `f723763`, this is the context-menu branch of JDA

# BotCommands
This framework simplifies the creation of Discord bots with the [JDA](https://github.com/DV8FromTheWorld/JDA) library.

## Features

* Automatic command registration
* Text based commands, with 2 ways of working:
  * More manual parsing, you have a tokenized message and you choose how to process the token
  * Automatic parsing of the arguments, your method signature is translated into a command syntax, such as:
    * Suppose the prefix is `!` and the command is `ban`
    * `@Executable public void run(BaseCommandEvent event, User user, int delDays, String reason)` `->` `!ban @someone 42 Foobar` should be valid
* Slash commands with automatic registration & argument parsing
  * They are passed to Discord on startup if any changes are detected
* A JDA event waiter with preconditions, timeouts and multiple checks
* Secure (as in random 64 char length ID from 81 chars) component (buttons/selection menus) IDs with persistent and non-persistent storage, also capable of received additional arguments the same way as slash commands do
* Message parsers (see RichTextParser) and emoji resolvers (can turn :joy: into 😂)
* Paginators and menus (using buttons !)
* Flexible constructors for your commands and injectable fields

Note that text-based commands, slash commands and component handlers are running in separate threads from JDA as to not block the websocket, keep in mind that this does not allow you to have bad practises as described in [how to use RestAction(s)](https://github.com/DV8FromTheWorld/JDA/wiki/7%29-Using-RestAction) 

## Getting Started
You are recommended to have some experience with Java and [JDA](https://github.com/DV8FromTheWorld/JDA) before you start using this library

### Prerequisites
[OpenJDK 11+](https://adoptopenjdk.net/) <br>
An IDE which supports Maven projects (like IntelliJ) or install [Maven](https://maven.apache.org/download.cgi) manually <br>
**Do not forget to add the Maven bin directory to your PATH environment variables**, if you choose not to use an IDE

## Getting the library
### Installing with Jitpack

You can add the following to your pom.xml
```xml
<repository>
    <id>jitpack</id>
    <url>https://jitpack.io</url>
</repository>

...

<dependency>
    <groupId>com.github.freya022</groupId>
    <artifactId>BotCommands</artifactId>
    <version>VERSION</version>
</dependency>
```

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

- Enable the debug/trace logs in your logback.xml file, for a logging tutorial you can look at [the wiki's logging page](https://github.com/freya022/BotCommands/wiki/Logging)
- [CommandsBuilder#updateCommandsOnGuildIds](src/main/java/com/freya02/botcommands/CommandsBuilder.java) - Updates the slash commands only in these guild IDs, useful for testing things without using another token

## Replacing help content

You can disable the prefixed help command and/or the `/help` command, these methods are in CommandsBuilder, if you do disable prefixed help commands you need to supply your own implementation when commands are detected, but their syntax is invalid

The provided implementation could just do nothing (such as `e -> {}`) if you want to just remove any form of help message

## Examples

You can find example bots in the [examples](examples) folder

## Support

The [JDA guild](https://discord.gg/jda) is not a place where you should ask for support on this framework, if you need support please join [this guild instead (link)](https://discord.gg/frpCcQfvTz)