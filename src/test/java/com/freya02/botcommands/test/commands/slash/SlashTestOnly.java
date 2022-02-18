package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.Test;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SlashTestOnly extends ApplicationCommand {
	@Override
	@NotNull
	public List<CommandPrivilege> getCommandPrivileges(@NotNull Guild guild, @NotNull String cmdBaseName) {
		if (guild.getIdLong() != 722891685755093072L) {
			throw new IllegalArgumentException("Not the test guild ID");
		}

		return super.getCommandPrivileges(guild, cmdBaseName);
	}

	@Test
	@JDASlashCommand(
			name = "test-only"
	)
	public void testOnly(GuildSlashEvent event) {
		if (event.getGuild().getIdLong() != 722891685755093072L) {
			throw new IllegalArgumentException("Not the test guild ID");
		}

		event.reply("In a test guild").setEphemeral(true).queue();
	}
}
