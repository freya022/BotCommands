package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle;

import java.util.Locale;

import static com.freya02.botcommands.api.localization.Localization.Entry.entry;

@LocalizationBundle("Test")
public class SlashLocalization extends ApplicationCommand {
	@JDASlashCommand(name = "localization")
	public void run(GuildSlashEvent event) {
		event.reply("done")
				.setEphemeral(true)
				.queue();

		event.getChannel()
				.sendMessage("User localized (" + event.getUserLocale() + "):\n" + event.localizeUser("commands.localization.response",
						entry("guild_users", event.getGuild().getMemberCount()),
						entry("uptime", 3.141519)))
				.queue();

		event.getChannel()
				.sendMessage("Guild localized (" + event.getGuildLocale() + "):\n" + event.localizeGuild("commands.localization.response",
						entry("guild_users", event.getGuild().getMemberCount()),
						entry("uptime", 3.141519)))
				.queue();

		event.getChannel()
				.sendMessage("German localized:\n" + event.localize(Locale.GERMAN, "commands.localization.response",
						entry("guild_users", event.getGuild().getMemberCount()),
						entry("uptime", 3.141519)))
				.queue();
	}
}
