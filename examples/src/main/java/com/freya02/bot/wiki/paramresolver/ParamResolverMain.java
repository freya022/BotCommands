package com.freya02.bot.wiki.paramresolver;

import com.freya02.bot.Config;
import com.freya02.botcommands.api.CommandsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class ParamResolverMain {
	public static void main(String[] args) {
		try {
			final Config config = Config.readConfig();

			final JDA jda = JDABuilder.createLight(config.getToken()).build().awaitReady();

			CommandsBuilder.newBuilder()
					.extensionsBuilder(extensionsBuilder ->
							extensionsBuilder.registerParameterResolver(new TimestampResolver())
					)
					.build(jda, "com.freya02.bot.wiki.paramresolver.commands");
		} catch (Exception e) {
			e.printStackTrace();

			System.exit(-1);
		}
	}
}
