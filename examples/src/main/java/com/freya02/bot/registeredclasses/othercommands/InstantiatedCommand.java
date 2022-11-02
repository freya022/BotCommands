package com.freya02.bot.registeredclasses.othercommands;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import org.slf4j.Logger;

import java.sql.Connection;

public class InstantiatedCommand extends ApplicationCommand {
	private static final Logger LOGGER = Logging.getLogger();

	@SuppressWarnings("unused") //Just so the class is not instantiable, but manually instantiated
	public InstantiatedCommand(Connection con) {
		LOGGER.info("Instantiated !");
	}

	@JDASlashCommand(name = "instance")
	public void run(GuildSlashEvent event) {
		event.reply("Haha `new` go brr")
				.setEphemeral(true)
				.queue();
	}
}
