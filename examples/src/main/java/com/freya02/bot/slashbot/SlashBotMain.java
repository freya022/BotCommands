package com.freya02.bot.slashbot;

import com.freya02.bot.CommonMain;
import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.internal.Logging;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;

public class SlashBotMain {
	private static final Logger LOGGER = Logging.getLogger();

	public static void main(String[] args) {
		try {
			final JDA jda = CommonMain.start().getJda();

			//Build the command framework:
			// Prefix: ! (only used by help command since well, there's no text based commands here)
			// Owner: User with the ID 222046562543468545
			// Commands package: com.freya02.bot.slashbot.commands
			CommandsBuilder.newBuilder(222046562543468545L)
					.textCommandBuilder(textCommandsBuilder -> textCommandsBuilder.addPrefix("!"))
					.build(jda, "com.freya02.bot.slashbot.commands"); //Registering listeners is taken care of by the lib
		} catch (Exception e) {
			LOGGER.error("Unable to start the bot", e);
			System.exit(-1);
		}
	}
}
