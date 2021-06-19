package com.freya02.bot;

import com.freya02.botcommands.CommandsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;

public class Main {
	public static void main(String[] args) {
		//Make sure that the file Token.txt exists under src/main/java/resources/com/freya02/bot/Token.txt and has a valid token inside
		try (final InputStream stream = Main.class.getResourceAsStream("Token.txt")) {
			final String botToken = new String(stream.readAllBytes()); //Read our secret bot token into a string

			//Set up JDA
			final JDA jda = JDABuilder.createLight(botToken)
					.setActivity(Activity.playing("Prefix is !"))
					.build()
					.awaitReady();

			//Print some information about the bot
			System.out.println("Bot connected as " + jda.getSelfUser().getAsTag());
			System.out.println("The bot is present on these guilds :");
			for (Guild guild : jda.getGuildCache()) {
				System.out.printf("\t- %s (%s)%n", guild.getName(), guild.getId());
			}

			//Build the command framework:
			// Prefix: ;
			// Owner: User with the ID 222046562543468545
			// Commands package: com.freya02.bot.commands
			CommandsBuilder.withPrefix("!", 222046562543468545L)
					.build(jda, "com.freya02.bot.commands"); //Registering listener is taken care of by the lib
		} catch (IOException | InterruptedException | LoginException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}