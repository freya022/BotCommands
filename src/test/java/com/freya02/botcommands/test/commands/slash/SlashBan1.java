package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.annotations.BotPermissions;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

public class SlashBan1 extends ApplicationCommand {
	@BotPermissions(Permission.BAN_MEMBERS)
	@JDASlashCommand(name = "firstban", group = "user", subcommand = "perm")
	public void ban1(GuildSlashEvent event,
	                 @AppOption User user,
	                 @AppOption @Optional long delDays,
	                 //@Nullable is the same as @Optional but with static analysis
	                 @AppOption @Nullable String reason) {
		event.reply("Nah").setEphemeral(true).queue();

		System.out.println("user = " + user);
		System.out.println("delDays = " + delDays);
		System.out.println("reason = " + reason);

		//ban
	}
}