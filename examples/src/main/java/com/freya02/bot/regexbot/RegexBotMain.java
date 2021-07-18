package com.freya02.bot.regexbot;

import com.freya02.bot.CommonMain;
import com.freya02.botcommands.CommandsBuilder;
import com.freya02.botcommands.Logging;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class RegexBotMain {
	private static final Logger LOGGER = Logging.getLogger();

	public static void main(String[] args) {
		try {
			final JDA jda = CommonMain.start();

			//Build the command framework:
			// Prefix: !
			// Owner: User with the ID 222046562543468545
			// Commands package: com.freya02.bot.regexbot.commands
			CommandsBuilder.withPrefix("!", 222046562543468545L)
					.build(jda, "com.freya02.bot.regexbot.commands"); //Registering listeners is taken care of by the lib
		} catch (IOException | InterruptedException | LoginException e) {
			LOGGER.error("Unable to start the bot", e);
			System.exit(-1);
		}
	}
}
