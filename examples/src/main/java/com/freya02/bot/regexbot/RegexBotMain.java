package com.freya02.bot.regexbot;

import com.freya02.bot.CommonMain;
import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.Logging;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;

public class RegexBotMain {
	private static final Logger LOGGER = Logging.getLogger();

	public static void main(String[] args) {
		try {
			final JDA jda = CommonMain.start().getJDA();

			//Build the command framework:
			// Prefix: !
			// Owner: User with the ID 222046562543468545
			// Commands package: com.freya02.bot.regexbot.commands
			CommandsBuilder.newBuilder(222046562543468545L)
					.textCommandBuilder(textCommandsBuilder -> textCommandsBuilder.addPrefix("!"))
					.build(jda, "com.freya02.bot.regexbot.commands"); //Registering listeners is taken care of by the lib
		} catch (Exception e) {
			LOGGER.error("Unable to start the bot", e);
			System.exit(-1);
		}
	}
}
