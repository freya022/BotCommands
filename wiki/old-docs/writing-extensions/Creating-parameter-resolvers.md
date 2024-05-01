The reason why your commands with method signatures like `#!java public void kick(GuildSlashEvent event, @AppOption User user, @AppOption String reason)` works is because there are default parameter resolvers, the default resolvers are registered automatically when ParameterResolvers is loaded. They can resolve regex command parameters / application command parameters and also button parameters

## Creating a new `ParameterResolver`
To create a new parameter resolver, you have the following steps:

* Create a new class
* Make it extend `ParameterResolver`
* Use the `super` constructor to indicate the type of the resolved object
* Implement one or more of these interfaces described in `ParameterResolver`
* Register the resolver with `ExtensionsBuilder#registerParameterResolver`

## Example - How to add a `ParameterResolver`

```java
//Create the resolver
public class TimestampResolver extends ParameterResolver implements SlashParameterResolver {
	public TimestampResolver() {
		super(Timestamp.class);
	}

	@Override
	public Object resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		final Matcher timestampMatcher = MARKDOWN.matcher(optionMapping.getAsString());
		if (!timestampMatcher.find()) return null; //Avoid expensive exceptions from JDA

		final String format = timestampMatcher.group("style");
		final long time = Long.parseLong(timestampMatcher.group("time"));
		return (format == null ? DEFAULT : fromStyle(format)).atTimestamp(time);
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.STRING;
	}
}
```

### Registering the parameter resolver

```java
CommandsBuilder.newBuilder()
	.extensionsBuilder(extensionsBuilder ->
			extensionsBuilder.registerParameterResolver(new TimestampResolver())
	)
	.build(jda, "com.freya02.bot.wiki.paramresolver.commands");
```

### Using these parameters

```java
public class SlashParamResolverTest extends ApplicationCommand {
	@JDASlashCommand(name = "paramres")
	public void run(GuildSlashEvent event, @AppOption Timestamp timestamp) {
		event.reply("Your timestamp as relative: " + TimeFormat.RELATIVE.format(timestamp.getTimestamp())).queue();
	}
}
```