You might have a use case where your command needs specific objects from other instances (such as a database connection perhaps), normally you could pass them to your command constructors, but here your commands are constructed automatically, so manually doing it is not an option.

That's why you can register constructor parameter suppliers and also instance suppliers

## How to use them

Let's suppose you have a slash command class which looks like this:

```java
public class SlashCtorInjectionTest extends ApplicationCommand {
	private final BContext context;
	private final Connection connection;

	public SlashCtorInjectionTest(BContext context, Connection connection) {
		this.context = context;
		this.connection = connection;
	}

	@JDASlashCommand(name = "ctorinj")
	public void run(GuildSlashEvent event) {
		event.replyFormat("My fields are %s and %s", context, connection).queue();
	}
}
```

You can make these types of constructors possible by using either of the two methods described above:

Using an instance supplier:
```java
Connection connection = null; //Just a test value
CommandsBuilder.newBuilder(0L)
		.extensionsBuilder(extensionsBuilder ->
				extensionsBuilder.registerInstanceSupplier(SlashCtorInjectionTest.class, context -> new SlashCtorInjectionTest(context, connection))
		)
		.build(jda, "com.freya02.bot.wiki.ctorinj.commands");
```

Or using a constructor parameter supplier (preferred):
```java
Connection connection = null; //Just a test value
CommandsBuilder.newBuilder(0L)
		.extensionsBuilder(extensionsBuilder ->
				extensionsBuilder.registerConstructorParameter(Connection.class, ignored -> connection)
		)
		.build(jda, "com.freya02.bot.wiki.ctorinj.commands");
```

## Notes
* The constructor parameters can be in any order
* The constructor must **be accessible** (public)