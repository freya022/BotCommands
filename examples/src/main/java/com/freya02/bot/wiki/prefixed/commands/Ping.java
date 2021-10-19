package com.freya02.bot.wiki.prefixed.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.Category;
import com.freya02.botcommands.api.prefixed.annotations.Description;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;

@CommandMarker //No unused warnings
@Category("Utils")
@Description("Gives information about an entity")
public class Ping extends TextCommand {
	@JDATextCommand(name = "ping")
	public void exec(CommandEvent event) { //Fallback CommandEvent
		final long gatewayPing = event.getJDA().getGatewayPing();
		event.getJDA().getRestPing()
				.queue(l -> event.replyFormat("Gateway ping: **%d ms**\nRest ping: **%d ms**", gatewayPing, l).queue());
	}
}