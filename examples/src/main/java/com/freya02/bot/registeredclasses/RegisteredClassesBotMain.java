package com.freya02.bot.registeredclasses;

import com.freya02.bot.CommonMain;
import com.freya02.bot.registeredclasses.othercommands.InstantiatedCommand;
import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.Logging;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;

public class RegisteredClassesBotMain {
	private static final Logger LOGGER = Logging.getLogger();

	public static void main(String[] args) {
		try {
			final CommonMain.CommonStuff commonStuff = CommonMain.start();
			final JDA jda = commonStuff.getJDA();

			final SomeObject someObject = new SomeObject();

			//Build the command framework:
			// Prefix: !
			// Owner: User with the ID 222046562543468545
			// Commands package: com.freya02.bot.registeredclasses.commands
			CommandsBuilder.newBuilder(222046562543468545L)
					.textCommandBuilder(textCommandsBuilder -> textCommandsBuilder.addPrefix("!"))
					.extensionsBuilder(extensionsBuilder -> extensionsBuilder
							.registerCommandDependency(JDA.class, () -> jda) //All fields of type JDA will have the JDA instance
							.registerConstructorParameter(SomeObject.class, x -> someObject) //All constructors with a SomeObject parameter will have this object
							.registerInstanceSupplier(InstantiatedCommand.class, ctx -> new InstantiatedCommand(null)) //If this command needs to be instantiated, do it manually
					)
					.addSearchPath("com.freya02.bot.registeredclasses.othercommands") //Also search commands in this package
					.build(jda, "com.freya02.bot.registeredclasses.commands"); //Registering listeners is taken care of by the lib
		} catch (Exception e) {
			LOGGER.error("Unable to start the bot", e);
			System.exit(-1);
		}
	}
}
