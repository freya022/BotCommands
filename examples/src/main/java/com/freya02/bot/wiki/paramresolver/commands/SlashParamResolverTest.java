package com.freya02.bot.wiki.paramresolver.commands;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;

public class SlashParamResolverTest extends ApplicationCommand {
	@JDASlashCommand(name = "paramres")
	public void run(GuildSlashEvent event, @AppOption Timestamp timestamp) {
		event.reply("Your timestamp as relative: " + TimeFormat.RELATIVE.format(timestamp.getTimestamp())).queue();
	}
}