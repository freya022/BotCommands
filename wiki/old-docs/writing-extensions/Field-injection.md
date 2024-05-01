You might have a use case where your command needs specific objects from other instances (such as a database connection perhaps), normally you could pass them to your command constructors, but here your commands are constructed automatically, so manually doing it is not an option.

That's why you can register field dependencies suppliers (or use [constructor injection](./Constructor-injection.md))

## How to use them

You can make these types of fields possible by using the `@Dependency` annotation:

### Example

```java
public class SlashFieldInjectionTest extends ApplicationCommand {
	@Dependency private BContext context;
	@Dependency private Connection connection;

	@JDASlashCommand(name = "fieldinj")
	public void run(GuildSlashEvent event) {
		event.replyFormat("My fields are %s and %s", context, connection).queue();
	}
}
```