package com.freya02.bot.wiki.fieldinj.commands;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import java.sql.Connection;

public class SlashFieldInjectionTest extends ApplicationCommand {
	@Dependency private BContext context;
	@Dependency private Connection connection;

	@JDASlashCommand(name = "fieldinj")
	public void run(GuildSlashEvent event) {
		event.replyFormat("My fields are %s and %s", context, connection).queue();
	}
}