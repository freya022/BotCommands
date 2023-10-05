package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.DoubleRange;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.LongRange;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

public class SlashNumber extends ApplicationCommand {
	@JDASlashCommand(name = "number", subcommand = "integer")
	public void longCmd(GuildSlashEvent event,
	                    @SlashOption int number) {
		event.reply("" + number).setEphemeral(true).queue();
	}

	@JDASlashCommand(name = "number", subcommand = "long")
	public void longCmd(GuildSlashEvent event,
	                    @SlashOption @LongRange(from = 10, to = 15) long number) {
		event.reply("" + number).setEphemeral(true).queue();
	}

	@JDASlashCommand(name = "number", subcommand = "double")
	public void doubleCmd(GuildSlashEvent event,
	                      @SlashOption @DoubleRange(from = 10.35, to = 15.35) double number) {
		event.reply("" + number).setEphemeral(true).queue();
	}
}