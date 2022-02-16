package com.freya02.bot;

import com.freya02.botcommands.api.Logging;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class CommonMain {
	private static final Logger LOGGER = Logging.getLogger();

	public static class CommonStuff {
		private final JDA jda;
		private final Config config;

		private CommonStuff(JDA jda, Config config) {
			this.jda = jda;
			this.config = config;
		}

		public JDA getJDA() {
			return jda;
		}

		public Config getConfig() {
			return config;
		}
	}

	public static CommonStuff start() throws IOException, LoginException, InterruptedException {
		//Make sure that the file Config.json exists under src/main/java/resources/com/freya02/bot/Config.json and has a valid token inside
		final Config config = Config.readConfig();

		//Set up JDA
		final JDA jda = JDABuilder.createLight(config.getToken())
				.setActivity(Activity.playing("Prefix is !"))
				.build()
				.awaitReady();

		//Print some information about the bot
		LOGGER.info("Bot connected as {}", jda.getSelfUser().getAsTag());
		LOGGER.info("The bot is present on these guilds :");
		for (Guild guild : jda.getGuildCache()) {
			LOGGER.info("\t- {} ({})", guild.getName(), guild.getId());
		}

		return new CommonStuff(jda, config);
	}
}