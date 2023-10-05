package io.github.freya022.botcommands.test.commands.help;

import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent;
import io.github.freya022.botcommands.api.commands.prefixed.IHelpCommand;
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandInfo;
import org.jetbrains.annotations.NotNull;

public class NewHelpCommand extends TextCommand implements IHelpCommand {
	@JDATextCommand(
			name = "help",
			description = "Sends help"
	)
	public void onTextHelp(BaseCommandEvent event) {
		event.reactSuccess().queue();
	}

	@Override
	public void onInvalidCommand(@NotNull BaseCommandEvent event, @NotNull TextCommandInfo commandInfo) {
		event.reactError().queue();
	}
}
