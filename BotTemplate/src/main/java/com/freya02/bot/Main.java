package com.freya02.bot;

import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.components.DefaultComponentManager;
import com.freya02.botcommands.internal.Logging;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;

public class Main {
	private static final Logger LOGGER = Logging.getLogger();

	public static void main(String[] args) {
		try {
			//Make sure that the file Config.json exists under src/main/java/resources/com/freya02/bot/Config.json and has a valid token inside
			final Config config = Config.readConfig();

			//Set up JDA
			final JDA jda = JDABuilder.createLight(config.getToken())
					.setActivity(Activity.playing("Prefix is " + config.getPrefix()))
					.build()
					.awaitReady();

			//Print some information about the bot
			LOGGER.info("Bot connected as {}", jda.getSelfUser().getAsTag());
			LOGGER.info("The bot is present on these guilds :");
			for (Guild guild : jda.getGuildCache()) {
				LOGGER.info("\t- {} ({})", guild.getName(), guild.getId());
			}

			//Instantiate the database needed for components
			final ComponentsDB componentsDB = new ComponentsDB(config);

			//Build the command framework:
			// Prefix: configured in Config.json
			// Owner: configured in Config.json
			// Commands package: com.freya02.bot.commands
			CommandsBuilder.newBuilder(config.getOwnerId())
					.textCommandBuilder(textCommandsBuilder -> textCommandsBuilder.addPrefix(config.getPrefix()))
					.setComponentManager(new DefaultComponentManager(componentsDB::getConnection))
					.build(jda, "com.freya02.bot.commands"); //Registering listeners is taken care of by the lib
		} catch (IOException | InterruptedException | LoginException | SQLException e) {
			LOGGER.error("Unable to start the bot", e);

			System.exit(-1);
		}
	}
}
