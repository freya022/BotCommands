package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.annotations.api.annotations.Cooldown;
import com.freya02.botcommands.api.CooldownScope;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

import java.util.concurrent.TimeUnit;

public class SlashCooldown extends ApplicationCommand {
	@Cooldown(cooldown = 10, unit = TimeUnit.SECONDS, cooldownScope = CooldownScope.USER)
	@JDASlashCommand(name = "cooldown", subcommand = "user")
	public void run(GuildSlashEvent event) {
		event.reply("ok").setEphemeral(true).queue();
	}

	@Cooldown(cooldown = 10, unit = TimeUnit.SECONDS, cooldownScope = CooldownScope.GUILD)
	@JDASlashCommand(name = "cooldown", subcommand = "guild")
	public void run2(GuildSlashEvent event) {
		event.reply("ok2").setEphemeral(true).queue();
	}

	@Cooldown(cooldown = 10, unit = TimeUnit.SECONDS, cooldownScope = CooldownScope.CHANNEL)
	@JDASlashCommand(name = "cooldown", subcommand = "channel")
	public void run3(GuildSlashEvent event) {
		event.reply("ok3").setEphemeral(true).queue();
	}
}
