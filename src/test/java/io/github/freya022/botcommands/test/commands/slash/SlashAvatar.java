package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import net.dv8tion.jda.api.entities.User;

public class SlashAvatar extends ApplicationCommand {
	@JDASlashCommand(
			name = "avatar",
			description = "Gives the avatar of any user"
	)
	public void avatar(GuildSlashEvent event, @SlashOption User user) {
		event.reply(user.getEffectiveAvatarUrl() + "?size=512").setEphemeral(true).queue();
	}
}