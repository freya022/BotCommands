package com.freya02.bot.commands.slash;

import com.freya02.botcommands.api.annotations.BotPermissions;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.LongRange;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JDAButtonListener;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

public class SlashBan extends ApplicationCommand {
	private static final String BAN_CONFIRM_HANDLER_NAME = "banConfirm";
	private static final String BAN_CANCEL_HANDLER_NAME = "banCancel";

	@BotPermissions(Permission.BAN_MEMBERS)
	@UserPermissions(Permission.BAN_MEMBERS)
	@JDASlashCommand(name = "ban", description = "Bans a user from this guild, the user does not need to be in the guild to work")
	public void ban(GuildSlashEvent event,
	                @AppOption(description = "The user to ban") User target,
	                @AppOption(description = "The number of days of messages to delete") @LongRange(from = 0, to = 7) long delDays,
	                @AppOption(description = "The reason for the ban") @Optional String reason) {
		if (reason == null) reason = "No reason";

		event.reply("Are you sure ?")
				.addActionRow(Components.group(
						Components.primaryButton(BAN_CANCEL_HANDLER_NAME).build("No"),
						Components.dangerButton(BAN_CONFIRM_HANDLER_NAME, target, delDays, reason).build("Yes")
				))
				.setEphemeral(true)
				.queue();
	}

	@JDAButtonListener(name = BAN_CONFIRM_HANDLER_NAME)
	public void banConfirm(ButtonEvent event, @AppOption User user, @AppOption long delDays, @AppOption String reason) {
		event.editMessage("Banned")
				.setActionRows() //Clear buttons
				.queue();

		//do ban logic
	}

	@JDAButtonListener(name = BAN_CANCEL_HANDLER_NAME)
	public void banCancel(ButtonEvent event) {
		event.editMessage("Cancelled")
				.setActionRows() //Clear buttons
				.queue();
	}
}
