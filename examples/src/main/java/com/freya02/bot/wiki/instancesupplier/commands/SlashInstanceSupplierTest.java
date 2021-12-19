package com.freya02.bot.wiki.instancesupplier.commands;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import org.slf4j.Logger;

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