package com.freya02.botcommands.test.commands.text;

import com.freya02.botcommands.api.annotations.AppendMode;
import com.freya02.botcommands.api.annotations.BotPermissions;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
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

	@BotPermissions(mode = AppendMode.ADD, value = Permission.BAN_MEMBERS)
	@JDATextCommand(
			name = "test",
			subcommand = "lol",
			order = 1
	)
	public void run(BaseCommandEvent event, @TextOption long l) {
		event.reply("no2").queue();
	}

	@BotPermissions(mode = AppendMode.ADD, value = Permission.KICK_MEMBERS)
	@JDATextCommand(
			name = "test",
			subcommand = "lol",
			order = 2
	)
	public void run(BaseCommandEvent event, @TextOption String xd) {
		event.reply("no").queue();
	}

	@BotPermissions(mode = AppendMode.ADD, value = Permission.KICK_MEMBERS)
	@JDATextCommand(
			name = "test",
			subcommand = "lol"
	)
	public void run(CommandEvent event) {
		event.reply("no3").queue();
	}
}