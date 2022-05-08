package com.freya02.botcommands.test.commands.guild_specific;

import com.freya02.botcommands.annotations.api.annotations.CommandId;
import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SlashCommandId extends ApplicationCommand {
	@Override
	@Nullable
	public Collection<Long> getGuildsForCommandId(@NotNull BContext context, @NotNull String commandId, @NotNull CommandPath commandPath) {
		if (commandId.equals("specific_run")) {
			return List.of(722891685755093072L);
		}

		return super.getGuildsForCommandId(context, commandId, commandPath);
	}

	@CommandId("global_run")
	@JDASlashCommand(name = "specific")
	public void run(GuildSlashEvent event) {
		event.reply("normal ok")
				.setEphemeral(true)
				.queue();
	}

	@CommandId("specific_run")
	@JDASlashCommand(name = "specific")
	public void run2(GuildSlashEvent event, @AppOption(description = "lol") User user) {
		event.reply("user " + user.getAsMention() + " ok")
				.setEphemeral(true)
				.queue();
	}
}
