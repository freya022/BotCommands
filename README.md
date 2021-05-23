<img src="https://img.shields.io/badge/JDA%20Version-db92d7d-important" alt="JDA Version db92d7d"/> <img src="https://img.shields.io/badge/Version-1.4.6-informational" alt="Version 1.4.6"/>

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
* [Waiting for events](src/main/java/com/freya02/botcommands/EventWaiter.java)
* [Resolving Discord entities](src/main/java/com/freya02/botcommands/utils/RichTextFinder.java) and [emojis](src/main/java/com/freya02/botcommands/utils/EmojiUtils.java)
* [Exception-free and garbage collectible InputStream](src/main/java/com/freya02/botcommands/utils/SimpleStream.java)

Note that commands are run in separate threads from JDA as to not block the websocket, keep in mind that this does not allow you to have bad practises as described in [how to use RestAction(s)](https://github.com/DV8FromTheWorld/JDA/wiki/7%29-Using-RestAction) 

## Getting Started
It is recommended that you have some experience with Java and [JDA](https://github.com/DV8FromTheWorld/JDA) before you start using this library

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

## Making commands
Every command must be in the package or subpackage of the package you supplied to CommandsBuild#build

**The class must be annotated with @JdaCommand and extend Command**

<details>
<summary>Example</summary>

```java
import com.freya02.botcommands.BContext;
import com.freya02.botcommands.prefixed.Command;
import com.freya02.botcommands.prefixed.CommandEvent;
import com.freya02.botcommands.prefixed.annotation.JdaCommand;

@JdaCommand(
		name = "test", //Mandatory
		description = "A test command",
		category = "Misc"
)
public class TestCommand extends Command {
	public TestCommand(BContext context) { super(context); }

	@Override
	protected void execute(CommandEvent event) {
		...
	}
}
```
</details>

The `Command#execute` sends the command's help content by default, you can override it to parse the command manually (though the arguments are tokenized inside CommandEvent for easier use)

## Making regex based commands

You can declare **instance** methods with parameters supported by [@Executable](src/main/java/com/freya02/botcommands/prefixed/annotation/Executable.java) but with the first argument being a BaseCommandEvent <br>
For example if you need a command which accepts a `TextChannel` and a `long`, you can do

<details>
<summary>Example</summary>

```java
import com.freya02.botcommands.BContext;
import com.freya02.botcommands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.prefixed.Command;
import com.freya02.botcommands.prefixed.annotation.Executable;
import com.freya02.botcommands.prefixed.annotation.JdaCommand;
import net.dv8tion.jda.api.entities.TextChannel;

@JdaCommand(
		name = "test", //Mandatory
		description = "A test command",
		category = "Misc"
)
public class TestCommand extends Command {
	public TestCommand(BContext context) { super(context); }

	@Executable
	public void exec(BaseCommandEvent event, TextChannel textChannel, long someLong) {
		//Only gets executed on commands like '!test #lobby 1234'
	}
}
```
</details>

**Be aware of the ordering of your functions**, the implementation looks at each Executable method and tries to match the pattern against the arguments, if it works, it gets executed, if it doesn't, it goes to the next one.

If a command invocation does not call the correct function during testing, you can enable debug logs to print the order in which the executables are loaded.

Changing the order of the methods at a source code level is not reliable, **to fix this, specify the `order` value in the Executable annotation on each method**

**If annotated with @AddExecutableHelp**, the parameter names of your executable are used in order to create the help content if you compile your bot with the `-parameters` switch on `javac`. In case the parameter names are not found, fallback ones are used.<br>
Note that you can also force a parameter name with the @ArgName annotation, alongside add an example with @ArgExample

## Making slash commands (Work in progress)

Every slash command must be in the same package (or a subpackage) that you supplied to CommandsBuild#build

**The class must extend SlashCommand**, but you can choose to not put a constructor or put one which accepts a [BContext](src/main/java/com/freya02/botcommands/BContext.java)

**The slash commands are methods annotated with JdaSlashCommand**, their first parameter must be a SlashEvent, or a GuildSlashEvent (for guild-only commands), you can control its option name/description with [@Option](src/main/java/com/freya02/botcommands/slash/annotations/Option.java) and command choices with [@Choices](src/main/java/com/freya02/botcommands/slash/annotations/Choices.java)

<details>
<summary>Example</summary>

```java
import com.freya02.botcommands.slash.SlashCommand;
import com.freya02.botcommands.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.slash.annotations.Option;
import com.freya02.botcommands.slash.GuildSlashEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class TestSlashCommand extends SlashCommand {
	@JdaSlashCommand(
			//This command is guild-only by default
			name = "say",
			description = "Says what you type"
	)
	public void say(GuildSlashEvent event,
                    @Option(name = "text", description = "What you want to say") String text) {
		//Your code here
	}
}
```
</details>

<details>
<summary>Example - Adding choices to slash commands</summary>

```java
import com.freya02.botcommands.slash.SlashCommand;
import com.freya02.botcommands.slash.annotations.Choice;
import com.freya02.botcommands.slash.annotations.Choices;
import com.freya02.botcommands.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.slash.annotations.Option;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class TestSlashCommand extends SlashCommand {
	@JdaSlashCommand(
			//This command is guild-only by default
			name = "say",
			description = "Says what you type"
	)
	public void say(SlashCommandEvent event,
	                @Option(name = "text", description = "What you want to say") @Choices({
			                @Choice(name = "Hi", value = "Greetings, comrad"),
			                @Choice(name = "Hello", value = "Oy")
	                }) String text) { //Only choices here are "Hi" and "Hello"
		//Your code here
	}
}
```
</details>

*Please note that replies sent with the interaction hook are ephemeral by default*

## Some debugging tools

- Enable the debug logs in your logback.xml file, for a logging tutorial you can look at [JDA's FAQ take at logging](https://github.com/DV8FromTheWorld/JDA/wiki/Logging-Setup#how-to-enable-debug-logs)
- [CommandsBuilder#updateCommandsOnGuildIds](src/main/java/com/freya02/botcommands/CommandsBuilder.java) - Updates the slash commands only in these guild IDs, useful for testing things without using another token

## Replacing help content

You can disable the prefixed help command and/or the `/help` command, these methods are in CommandsBuilder, if you do disable prefixed help commands you need to supply your own implementation when commands are detected, but their syntax is invalid

## Examples

You can find example bots in the [examples](examples) folder