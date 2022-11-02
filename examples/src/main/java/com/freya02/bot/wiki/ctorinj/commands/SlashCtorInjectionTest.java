package com.freya02.bot.wiki.ctorinj.commands;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

import java.sql.Connection;

public class SlashCtorInjectionTest extends ApplicationCommand {
	private final BContext context;
	private final Connection connection;

	public SlashCtorInjectionTest(BContext context, Connection connection) {
		this.context = context;
		this.connection = connection;
	}

	@JDASlashCommand(name = "ctorinj")
	public void run(GuildSlashEvent event) {
		event.replyFormat("My fields are %s and %s", context, connection).queue();
	}
}