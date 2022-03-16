package com.freya02.botcommands.test.commands.varargs;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.annotations.VarArgs;

import java.util.List;

public class SlashVarargs extends ApplicationCommand {
	//TODO implement autocompletion
	@JDASlashCommand(name = "varargs")
	public void run(GuildSlashEvent event, @AppOption(name = "number", description = "lol") @VarArgs(3) List<Long> longs) {
		event.reply("longs: " + longs)
				.setEphemeral(true)
				.queue();
	}
}
