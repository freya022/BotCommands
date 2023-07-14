package com.freya02.botcommands.test.commands.text;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;

import java.time.LocalDateTime;

@CommandMarker
public class CustomResolverTest extends ApplicationCommand {
	@JDASlashCommand(
			name = "resolve_test"
	)
	public void execute(GuildSlashEvent event, @SlashOption LocalDateTime time) {
		System.out.println("time = " + time);
	}
}
