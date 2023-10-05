package io.github.freya022.botcommands.test.commands.guild_specific;

import io.github.freya022.botcommands.api.commands.CommandPath;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SlashCommandId extends ApplicationCommand {
	@Override
	@Nullable
	public Collection<Long> getGuildsForCommandId(@NotNull String commandId, @NotNull CommandPath commandPath) {
		if (commandId.equals("specific_run")) {
			return List.of(722891685755093072L);
		}

		return super.getGuildsForCommandId(commandId, commandPath);
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
	public void run2(GuildSlashEvent event, @SlashOption(description = "lol") User user) {
		event.reply("user " + user.getAsMention() + " ok")
				.setEphemeral(true)
				.queue();
	}
}
