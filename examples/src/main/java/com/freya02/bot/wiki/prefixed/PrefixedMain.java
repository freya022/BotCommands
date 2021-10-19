package com.freya02.bot.wiki.prefixed;

import com.freya02.bot.Config;
import com.freya02.botcommands.api.CommandsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class PrefixedMain {
	public static void main(String[] args) {
		try {
			final Config config = Config.readConfig();

			final JDA jda = JDABuilder.createLight(config.getToken()).build().awaitReady();

			CommandsBuilder.newBuilder(0L).build(jda, "com.freya02.bot.wiki.prefixed");
		} catch (IOException | LoginException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
