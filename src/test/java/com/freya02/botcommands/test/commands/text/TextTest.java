package com.freya02.botcommands.test.commands.text;

import com.freya02.botcommands.api.commands.annotations.BotPermissions;
import com.freya02.botcommands.api.commands.annotations.Optional;
import com.freya02.botcommands.api.commands.annotations.UserPermissions;
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.commands.prefixed.CommandEvent;
import com.freya02.botcommands.api.commands.prefixed.TextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

@UserPermissions(Permission.ADMINISTRATOR)
@BotPermissions(Permission.MANAGE_SERVER)
public class TextTest extends TextCommand {
	@JDATextCommand(
			name = "test"
	)
	public void run(BaseCommandEvent event,
	                @TextOption User user,
					@TextOption @Optional long delDays,
	                @TextOption @Nullable String reason
	) {
		event.replyFormat("ok %s : %s : %s", user, delDays, reason).queue();
	}

	@BotPermissions(value = Permission.BAN_MEMBERS, append = true)
	@JDATextCommand(
			name = "test",
			subcommand = "lol",
			order = 1
	)
	public void run(BaseCommandEvent event, @TextOption long l) {
		event.reply("no2").queue();
	}

	@BotPermissions(value = Permission.KICK_MEMBERS, append = true)
	@JDATextCommand(
			name = "test",
			subcommand = "lol",
			order = 2
	)
	public void run(BaseCommandEvent event, @TextOption String xd) {
		event.reply("no").queue();
	}

	@BotPermissions(value = Permission.KICK_MEMBERS, append = true)
	@JDATextCommand(
			name = "test",
			subcommand = "lol"
	)
	public void run(CommandEvent event) {
		event.reply("no3").queue();
	}
}