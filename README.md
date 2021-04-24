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
* [Resolving Discord entities](src/main/java/com/freya02/botcommands/utils/RichTextFinder.java) and [emojis](src/main/java/com/freya02/botcommands/utils/EmojiResolver.java)
* [Exception-free and garbage collectible InputStream](src/main/java/com/freya02/botcommands/utils/SimpleStream.java)

## Getting Started
It is recommended that you have some experience with Java and [JDA](https://github.com/DV8FromTheWorld/JDA) before you start using this library

### Prerequisites
[OpenJDK 11+](https://adoptopenjdk.net/) <br>
An IDE which supports Maven projects (like IntelliJ) or install [Maven](https://maven.apache.org/download.cgi) manually <br>
**Do not forget to add the Maven bin directory to your PATH environment variables**, if you choose not to use an IDE

### Building / Installing
#### Getting the library
You can choose one of the methods:
* Git clone of this repo `git clone https://github.com/freya022/BotCommands.git`
* **OR** download the repository ZIP file, extract it and rename the folder to `BotCommands`

#### Building the library
Once you have it in a folder, change your working directory to it `cd [path_of_your_choice]/BotCommands` <br>
Then you can build the library with `mvn install`, it will build the library and put it in your local Maven dependency folder <br>

#### Using the library
You can now use the library in your Maven projects by adding the dependency like any other Maven dependency.

<details>
<summary>Maven XML - How to add the library</summary>

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
        <repository> <!-- Repository for JDA -->
            <id>jcenter</id>
            <name>jcenter-bintray</name>
            <url>https://jcenter.bintray.com</url>
        </repository>
    </repositories>
    <dependencies>
        <!-- Your other project's dependencies here -->
        
        <dependency> <!-- Add JDA to your project -->
            <groupId>net.dv8tion</groupId>
            <artifactId>JDA</artifactId>
            <version>4.2.0_229</version>
        </dependency>
        <dependency> <!-- Add BotCommands to your project -->
            <groupId>com.freya02</groupId>
            <artifactId>BotCommands</artifactId>
            <version>1.2</version>
        </dependency>
    </dependencies>
</project>
```
</details>

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

### Making commands
Every command must be in the package or subpackage of the package you supplied to CommandsBuild#build

**The class must be annotated with @JdaCommand and extend Command**

<details>
<summary>Example</summary>

```java
import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Command;
import com.freya02.botcommands.CommandEvent;
import com.freya02.botcommands.annotation.JdaCommand;

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

### Making regex based commands

You can declare **instance** methods with parameters supported by [@Executable](src/main/java/com/freya02/botcommands/annotation/Executable.java) but with the first argument being a BaseCommandEvent <br>
For example if you need a command which accepts a `TextChannel` and a `long`, you can do

<details>
<summary>Example</summary>

```java
import com.freya02.botcommands.BContext;
import com.freya02.botcommands.BaseCommandEvent;
import com.freya02.botcommands.Command;
import com.freya02.botcommands.annotation.Executable;
import com.freya02.botcommands.annotation.JdaCommand;
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

If a command invocation does not call the correct function during testing, you can use the @DebugPatterns annotation to print the order in which the executables are loaded.

Changing the order of the methods at a source code level is not reliable, **to fix this, specify the `order` value in the Executable annotation on each method**

**If annotated with @AddExecutableHelp**, the parameter names of your executable are used in order to create the help content if you compile your bot with the `-parameters` switch on `javac`. In case the parameter names are not found, fallback ones are used.<br>
Note that you can also force a parameter name with the @ArgName annotation, alongside add an example with @ArgExample

## Examples

You can find example bots in the [examples](examples) folder