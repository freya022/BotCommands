package com.freya02.bot.componentsbot;

import com.freya02.bot.CommonMain;
import com.freya02.bot.ComponentsDB;
import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.api.components.DefaultComponentManager;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;

public class ComponentsBotMain {
	private static final Logger LOGGER = Logging.getLogger();

	public static void main(String[] args) {
		try {
			final CommonMain.CommonStuff commonStuff = CommonMain.start();
			final JDA jda = commonStuff.getJDA();

			//Instantiate the database needed for components
			final ComponentsDB componentsDB = new ComponentsDB(commonStuff.getConfig());

			//Build the command framework:
			// Prefix: !
			// Owner: User with the ID 222046562543468545
			// Commands package: com.freya02.bot.componentsbot.commands
			CommandsBuilder.newBuilder(222046562543468545L)
					.textCommandBuilder(textCommandsBuilder -> textCommandsBuilder.addPrefix("!"))
					.setComponentManager(new DefaultComponentManager(componentsDB::getConnection))
					.build(jda, "com.freya02.bot.componentsbot.commands"); //Registering listeners is taken care of by the lib
		} catch (Exception e) {
			LOGGER.error("Unable to start the bot", e);
			System.exit(-1);
		}
	}
}
