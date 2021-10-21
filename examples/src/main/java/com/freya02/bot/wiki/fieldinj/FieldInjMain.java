package com.freya02.bot.wiki.fieldinj;

import com.freya02.bot.Config;
import com.freya02.botcommands.api.CommandsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.sql.Connection;

public class FieldInjMain {
	public static void main(String[] args) {
		try {
			final Config config = Config.readConfig();

			final JDA jda = JDABuilder.createLight(config.getToken()).build().awaitReady();

			Connection connection = null; //Just a test value
			CommandsBuilder.newBuilder()
					.extensionsBuilder(extensionsBuilder ->
							extensionsBuilder.registerCommandDependency(Connection.class, () -> connection)
					)
					.build(jda, "com.freya02.bot.wiki.fieldinj.commands");
		} catch (Exception e) {
			e.printStackTrace();

			System.exit(-1);
		}
	}
}
