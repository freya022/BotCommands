package com.freya02.botcommands.test.commands.text;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import java.time.LocalDateTime;

@CommandMarker
public class CustomResolverTest extends ApplicationCommand {
	@JDASlashCommand(
			name = "resolve_test"
	)
	public void execute(GuildSlashEvent event, @AppOption LocalDateTime time) {
		System.out.println("time = " + time);
	}
}
