package com.freya02.bot;

import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.components.DefaultComponentManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;

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
					.enableIntents(GatewayIntent.MESSAGE_CONTENT)
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
		} catch (IOException | InterruptedException | SQLException e) {
			LOGGER.error("Unable to start the bot", e);

			System.exit(-1);
		}
	}
}
