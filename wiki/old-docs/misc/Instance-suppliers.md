# Using instance suppliers

Maybe you have some use case where you would need to manually instantiate a command class

For this, you can register the instances directly in the `ExtensionsBuilder#registerInstanceSupplier`

## How to use it

You can register the instance supplier with `ExtensionsBuilder#registerInstanceSupplier`

### Example

```java
CommandsBuilder.newBuilder()
    .extensionsBuilder(extensionsBuilder ->
        extensionsBuilder.registerInstanceSupplier(SlashInstanceSupplierTest.class, ignored -> new SlashInstanceSupplierTest(new SlashInstanceSupplierTest.Dummy()))
    )
    .build(jda, "com.freya02.bot.wiki.instancesupplier.commands");
```

## Checking that it works

```java
public class SlashInstanceSupplierTest extends ApplicationCommand {
	private static final Logger LOGGER = Logging.getLogger();

	public static class Dummy {}

	//Making a non-instantiable constructor so only I can construct it
	public SlashInstanceSupplierTest(Dummy dummy) {
		LOGGER.debug("I got constructed with {}", dummy);
	}

	@JDASlashCommand(name = "instancesupplier")
	public void run(GuildSlashEvent event) {
		event.reply("I ran").queue();
	}
}
```