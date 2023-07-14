package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;
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