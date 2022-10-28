package com.freya02.bot.wiki.localization;

import com.freya02.bot.Config;
import com.freya02.botcommands.api.CommandsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.Locale;

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
							.addLocalizations("LocalizationWikiCommands", Locale.US)
					);
			builder.build(jda, "com.freya02.bot.wiki.localization.commands");
		} catch (Exception e) {
			e.printStackTrace();

			System.exit(-1);
		}
	}
}
