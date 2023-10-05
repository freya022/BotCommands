package io.github.freya022.botcommands.test.commands.text;

import io.github.freya022.botcommands.api.annotations.CommandMarker;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

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
