package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.CommandStatus;
import com.freya02.botcommands.api.annotations.GuildSpecific;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public class SlashGuildSpecific extends ApplicationCommand {
	@Override
	public CommandStatus getGuildSpecificCommandStatus(@NotNull BContext context, @NotNull Guild guild, @NotNull String specificId, @NotNull CommandPath commandPath) {
		if (specificId.equals("specific_run")) {
			if (guild.getIdLong() == 722891685755093072L) {
				return CommandStatus.UNSURE; //Don't disable specific command on target guild
			}

			return CommandStatus.DISABLED;
		}

		return super.getGuildSpecificCommandStatus(context, guild, specificId, commandPath);
	}

	@GuildSpecific("specific_run")
	@JDASlashCommand(name = "specific")
	public void run(GuildSlashEvent event) {
		event.reply("normal ok")
				.setEphemeral(true)
				.queue();
	}

	@GuildSpecific("global_run")
	@JDASlashCommand(name = "specific")
	public void run2(GuildSlashEvent event, @AppOption(description = "lol") User user) {
		event.reply("user " + user.getAsMention() + " ok")
				.setEphemeral(true)
				.queue();
	}
}
