# Writing prefixed commands

## A few keywords

* `TextCommand` - The class which should be inherited by every (regex) prefixed command
* `BContext` - The BotCommands context, it can help you get the `JDA` instance, check if someone is a bot (co)owner, get the default embeds, add/remove filters and much more
* `CommandEvent` - The event object received when a command is used, it extends `GuildMessageReceivedEvent` and provides more functions to help parse the command arguments
  * `#hasNext(Class<T>)`, `#peekArgument(Class<T>)` and `#nextArgument(Class<T>)` accepts classes such as strings and mentionables (user / member / text channel / role)
  * `#resolveNext(Class<?>...)` tries to resolve mentionables either from the full mention or from just a given ID, throwing an exception if it is not resolvable
* `@Hidden` - Hides a command from help content and from being used by non-owners
* `@RequireOwner` - Makes a command usable only by bot (co)owners


## Making a prefixed command
To make a valid prefixed command, you need to extend `TextCommand` and to have methods which are annotated with `@JDATextCommand`

There's attributes that are inheritable - so you can place them on the class instead of the individual methods, such as:
* Category
* Description
* Bot / User permission

### Regex based commands

* These commands have their method signature translated to a regex on runtime, it then converts the groups into your method parameters automatically
* Their parameters need to be annotated with @TextOption
* The commands can have a parsing order if you specify it in the command annotation

Be aware:

* The order of the methods might be important, only the first method which matches the regex is run
* Changing the order of the methods at a source code level is not reliable, **to fix this, specify the `order` value in the `@JDATextCommand` annotation on each method**
* **Strings are a common way of making these commands not work correctly, depending on how many there are and where they are placed in your parameters**, the framework will throw at startup if a command is "too complex"

Example:
```java
@CommandMarker //No unused warnings
@Category("Utils")
@Description("Gives information about an entity")
public class Info extends TextCommand {
	//Specifying the order makes it so methods have priorities, this is useful in this command because TextChannel, Role and Guild might have the same ids
	// (example: @everyone, the first text channel created and the guild has the same id)
	@JDATextCommand(name = "info", order = 1) //Method to be checked first
	public void exec(BaseCommandEvent event,
	                 @TextOption Member member) { //@TextOption is mandatory on parameters that have to be parsed
		//Show member info
	}

	@JDATextCommand(name = "info", order = 2)
	public void exec(BaseCommandEvent event, @TextOption User user) {
		//Show user info
	}
}
```

### Fallback commands

If none of the regex patterns matched, if this method exists then it's going to get called, otherwise, help content is shown

Example:
```java
@CommandMarker //No unused warnings
@Category("Utils")
@Description("Gives the ping of the bot")
public class Ping extends TextCommand {
	@JDATextCommand(name = "ping")
	public void exec(CommandEvent event) { //Fallback CommandEvent
		final long gatewayPing = event.getJDA().getGatewayPing();
		event.getJDA().getRestPing()
				.queue(l -> event.replyFormat("Gateway ping: **%d ms**\nRest ping: **%d ms**", gatewayPing, l).queue());
	}
}
```


## Adding alternative prefixes

You can add alternative prefixes if you have a `SettingsProvider` such as:

```java
public class PrefixSettingsProvider implements SettingsProvider {
	@Override
	@Nullable
	public List<String> getPrefixes(@NotNull Guild guild) {
		if (guild.getIdLong() == 722891685755093072L) {
			return List.of("^"); //Only the prefix "^" will be used for the guild ID above
		}

		return SettingsProvider.super.getPrefixes(guild);
	}
}
```

You can then register your `SettingsProvider` by adding `#setSettingsProvider(new PrefixSettingsProvider())` to your CommandsBuilder chain

## Replacing help content

You can replace the default help command by making a text command with the same path.
Your help command needs to implement `IHelpCommand` so that the help is still displayed when commands are detected, but their syntax is invalid

You can also not provide an help implementation and just disable the command with `TextCommandsBuilder#disableHelpCommand`

You can also modify the help embeds with `TextCommandsBuilder#setHelpBuilderConsumer`