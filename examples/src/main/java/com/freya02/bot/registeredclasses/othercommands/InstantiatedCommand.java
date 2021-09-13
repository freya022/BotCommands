package com.freya02.bot.registeredclasses.othercommands;

import com.freya02.botcommands.Logging;
import com.freya02.botcommands.api.application.GuildSlashEvent;
import com.freya02.botcommands.api.application.SlashCommand;
import com.freya02.botcommands.api.application.slash.annotations.JdaSlashCommand;
import org.slf4j.Logger;

import java.sql.Connection;

public class InstantiatedCommand extends SlashCommand {
	private static final Logger LOGGER = Logging.getLogger();

	@SuppressWarnings("unused") //Just so the class is not instantiable, but manually instantiated
	public InstantiatedCommand(Connection con) {
		LOGGER.info("Instantiated !");
	}

	@JdaSlashCommand(name = "instance")
	public void run(GuildSlashEvent event) {
		event.reply("Haha `new` go brr")
				.setEphemeral(true)
				.queue();
	}
}
