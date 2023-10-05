package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.annotations.Cooldown;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.ratelimit.RateLimitScope;

import java.time.temporal.ChronoUnit;

public class SlashCooldown extends ApplicationCommand {
	@Cooldown(cooldown = 10, unit = ChronoUnit.SECONDS, rateLimitScope = RateLimitScope.USER)
	@JDASlashCommand(name = "cooldown", subcommand = "user")
	public void run(GuildSlashEvent event) {
		event.reply("ok").setEphemeral(true).queue();
	}

	@Cooldown(cooldown = 10, unit = ChronoUnit.SECONDS, rateLimitScope = RateLimitScope.GUILD)
	@JDASlashCommand(name = "cooldown", subcommand = "guild")
	public void run2(GuildSlashEvent event) {
		event.reply("ok2").setEphemeral(true).queue();
	}

	@Cooldown(cooldown = 10, unit = ChronoUnit.SECONDS, rateLimitScope = RateLimitScope.CHANNEL)
	@JDASlashCommand(name = "cooldown", subcommand = "channel")
	public void run3(GuildSlashEvent event) {
		event.reply("ok3").setEphemeral(true).queue();
	}
}
