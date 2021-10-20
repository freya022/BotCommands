package com.freya02.bot.wiki.ctorinj;

import com.freya02.bot.Config;
import com.freya02.bot.wiki.ctorinj.commands.SlashCtorInjectionTest;
import com.freya02.botcommands.api.CommandsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.IOException;
import java.sql.Connection;

public class CtorInjMain {
	public static void main(String[] args) {
		try {
			final Config config = Config.readConfig();

			final JDA jda = JDABuilder.createLight(config.getToken()).build().awaitReady();

			alt1(jda);
//			alt2(jda);
		} catch (Exception e) {
			e.printStackTrace();

			System.exit(-1);
		}
	}

	private static void alt1(JDA jda) throws IOException {
		Connection connection = null; //Just a test value
		CommandsBuilder.newBuilder()
				.extensionsBuilder(extensionsBuilder ->
						extensionsBuilder.registerConstructorParameter(Connection.class, ignored -> connection)
				)
				.build(jda, "com.freya02.bot.wiki.ctorinj.commands");
	}

	private static void alt2(JDA jda) throws IOException {
		Connection connection = null; //Just a test value
		CommandsBuilder.newBuilder(0L)
				.extensionsBuilder(extensionsBuilder ->
						extensionsBuilder.registerInstanceSupplier(SlashCtorInjectionTest.class, context -> new SlashCtorInjectionTest(context, connection))
				)
				.build(jda, "com.freya02.bot.wiki.ctorinj.commands");
	}
}
