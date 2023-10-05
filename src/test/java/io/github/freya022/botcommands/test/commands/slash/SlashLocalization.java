package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle;
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.Nullable;

import static io.github.freya022.botcommands.api.localization.Localization.Entry.entry;

public class SlashLocalization extends ApplicationCommand {
	@JDASlashCommand(name = "localization")
	public void run(GuildSlashEvent event,
					@LocalizationBundle("Test") AppLocalizationContext ctx,
					@SlashOption @Nullable String localizationOpt) {
		event.reply("done")
				.setEphemeral(true)
				.queue();

		event.getChannel()
				.sendMessage("User localized (" + event.getUserLocale() + "):\n" + ctx.localizeUser("commands.localization.response",
						entry("guild_users", event.getGuild().getMemberCount()),
						entry("uptime", 3.141519)))
				.queue();

		event.getChannel()
				.sendMessage("Guild localized (" + event.getGuildLocale() + "):\n" + ctx.localizeGuild("commands.localization.response",
						entry("guild_users", event.getGuild().getMemberCount()),
						entry("uptime", 3.141519)))
				.queue();

		event.getChannel()
				.sendMessage("German localized:\n" + ctx.localize(DiscordLocale.GERMAN, "commands.localization.response",
						entry("guild_users", event.getGuild().getMemberCount()),
						entry("uptime", 3.141519)))
				.queue();
	}
}