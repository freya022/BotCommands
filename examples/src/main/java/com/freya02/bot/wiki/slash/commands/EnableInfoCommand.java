package com.freya02.bot.wiki.slash.commands;

import com.freya02.bot.wiki.slash.BasicSettingsProvider;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.commands.prefixed.CommandEvent;
import com.freya02.botcommands.api.commands.prefixed.TextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.Category;
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import net.dv8tion.jda.api.Permission;

@Category("Moderation")
@UserPermissions(Permission.MANAGE_ROLES)
public class EnableInfoCommand extends TextCommand {
	@JDATextCommand(
			name = "enableinfocommand",
			description = "Enables the /info command"
	)
	public void execute(CommandEvent event) {
		if (event.getMember().canInteract(event.getGuild().getSelfMember())) {
			final BasicSettingsProvider settingsProvider = (BasicSettingsProvider) event.getContext().getSettingsProvider();

			if (settingsProvider == null) {
				event.indicateError("No settings provider has been set").queue();

				return;
			}

			settingsProvider.addCommand(event.getGuild(), "info");

			event.reactSuccess().queue();
		} else {
			event.indicateError("You cannot do this").queue();
		}
	}
}