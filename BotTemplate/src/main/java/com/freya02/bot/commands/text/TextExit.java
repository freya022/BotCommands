package com.freya02.bot.commands.text;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.annotations.RequireOwner;
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.commands.prefixed.TextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.Hidden;
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption;
import org.slf4j.Logger;

public class TextExit extends TextCommand {
	private static final Logger LOGGER = Logging.getLogger();

	@Hidden
	@RequireOwner
	@JDATextCommand(name = "exit")
	public void exit(BaseCommandEvent event, @TextOption @Optional String reason) {
		LOGGER.warn("Exiting for reason: {}", reason);

		event.reactSuccess()
				.mapToResult()
				.queue(x -> System.exit(0));
	}
}
