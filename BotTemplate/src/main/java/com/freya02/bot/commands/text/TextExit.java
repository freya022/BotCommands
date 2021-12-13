package com.freya02.bot.commands.text;

import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.annotations.RequireOwner;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.Hidden;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.Logging;
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
