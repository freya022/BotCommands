package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.DoubleRange;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.annotations.LongRange;

public class SlashNumber extends ApplicationCommand {
	@JDASlashCommand(name = "number", subcommand = "long")
	public void longCmd(GuildSlashEvent event,
	                    @AppOption @LongRange(from = 10, to = 15) long number) {
		event.reply("" + number).setEphemeral(true).queue();
	}

	@JDASlashCommand(name = "number", subcommand = "double")
	public void doubleCmd(GuildSlashEvent event,
	                      @AppOption @DoubleRange(from = 10.35, to = 15.35) double number) {
		event.reply("" + number).setEphemeral(true).queue();
	}
}
