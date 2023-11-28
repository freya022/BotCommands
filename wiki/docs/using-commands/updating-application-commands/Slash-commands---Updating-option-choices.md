# Slash commands - Updating option choices

See [here](../using-slash-commands/writing-slash-commands.md#examples) 
the example on how to add predefined choices to your slash commands

You can also change the `GuildApplicationSettings#getOptionChoices` to provide dynamic values, 
for example if you have a command that adds choices,
you would add the choice to the list and then call 
`BContext#scheduleApplicationCommandsUpdate` to update the application commands with the new choices

Example - How to make a dynamic choice list, having a command to add choices

```java
public class SlashChoices extends ApplicationCommand {
	private static final Logger LOGGER = Logging.getLogger();

	private final List<Command.Choice> valueList = new ArrayList<>();

	@Override
	@NotNull
	public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
		if (optionIndex == 0) {
			return valueList;
		}

		return List.of();
	}

	@JDASlashCommand(name = "choices", subcommand = "choose")
	public void choose(GuildSlashEvent event,
	                   @AppOption(description = "The value you choose") String value) {
		event.reply("Your choice: " + value)
				.setEphemeral(true)
				.queue();
	}

	@JDASlashCommand(name = "choices", subcommand = "add")
	public void addChoice(GuildSlashEvent event,
	                      @AppOption(description = "The name of the choice") String name,
	                      @AppOption(description = "The value of the choice") String value) {
		event.deferReply(true).queue();

		valueList.add(new Command.Choice(name, value));

		//You should handle the exceptions inside the completable future, in case an error occurred
		event.getContext().scheduleApplicationCommandsUpdate(event.getGuild(), false, false);

		event.getHook().sendMessage("Choice added successfully").queue();
	}
}
```