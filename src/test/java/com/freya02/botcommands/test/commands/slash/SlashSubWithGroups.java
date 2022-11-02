package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.annotations.UserPermissions;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.concurrent.TimeUnit;

@UserPermissions(Permission.MANAGE_SERVER)
public class SlashSubWithGroups extends ApplicationCommand {
//	@JDASlashCommand(name = "tag")
//	public void onSlashTag(GuildSlashEvent event) {reply(event);}

	@JDASlashCommand(name = "tag", subcommand = "send")
	public void onSlashTagSend(GuildSlashEvent event) {reply(event);}

	@JDASlashCommand(name = "tag", group = "manage", subcommand = "create")
	public void onSlashTagCreate(GuildSlashEvent event) {reply(event);}

	private void reply(GuildSlashEvent event) {
		event.reply("Working")
				.delay(5, TimeUnit.SECONDS)
				.flatMap(InteractionHook::deleteOriginal)
				.queue();
	}
}
