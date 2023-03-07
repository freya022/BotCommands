package com.freya02.bot.wiki.localization;

import com.freya02.bot.Config;
import com.freya02.botcommands.api.CommandsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class LocalizationMain {
	public static void main(String[] args) {
		try {
			final Config config = Config.readConfig();

			final JDA jda = JDABuilder.createLight(config.getToken())
					.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
					.setMemberCachePolicy(MemberCachePolicy.ALL)
					.setChunkingFilter(ChunkingFilter.ALL)
					.build()
					.awaitReady();

			final CommandsBuilder builder = CommandsBuilder.newBuilder()
					.applicationCommandBuilder(applicationCommandsBuilder -> applicationCommandsBuilder
							//This enables localization from the "LocalizationWikiCommands.json" bundle, in the en_US language (i.e. LocalizationWikiCommands_en_US.json)
							// If you wish to add more localizations, add a Locale here, and create the corresponding files
							.addLocalizations("LocalizationWikiCommands", DiscordLocale.ENGLISH_US)
					);
			builder.build(jda, "com.freya02.bot.wiki.localization.commands");
		} catch (Exception e) {
			e.printStackTrace();

			System.exit(-1);
		}
	}
}
