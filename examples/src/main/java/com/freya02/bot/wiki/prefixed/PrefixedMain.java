package com.freya02.bot.wiki.prefixed;

import com.freya02.bot.Config;
import com.freya02.botcommands.api.CommandsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class PrefixedMain {
	public static void main(String[] args) {
		try {
			final Config config = Config.readConfig();

			final JDA jda = JDABuilder.createLight(config.getToken())
					.enableIntents(GatewayIntent.MESSAGE_CONTENT)
					.build()
					.awaitReady();

			CommandsBuilder.newBuilder()
					.setSettingsProvider(new PrefixSettingsProvider())
					.build(jda, "com.freya02.bot.wiki.prefixed.commands");
		} catch (Exception e) {
			e.printStackTrace();

			System.exit(-1);
		}
	}
}
