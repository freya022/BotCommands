package com.freya02.bot.wiki.slash;

import com.freya02.bot.Config;
import com.freya02.botcommands.api.CommandsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class SlashMain {
	public static void main(String[] args) {
		try {
			final Config config = Config.readConfig();

			final JDA jda = JDABuilder.createLight(config.getToken()).build().awaitReady();

			final CommandsBuilder builder = CommandsBuilder.newBuilder(0L);
			builder
					.setSettingsProvider(new BasicSettingsProvider(builder.getContext()))
					.build(jda, "com.freya02.bot.wiki.slash.commands");
		} catch (IOException | LoginException | InterruptedException e) {
			e.printStackTrace();

			System.exit(-1);
		}
	}
}
