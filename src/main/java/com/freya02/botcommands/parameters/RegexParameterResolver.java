package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Interface which indicates this class can resolve parameters for regex commands
 */
public interface RegexParameterResolver {
	Object resolve(GuildMessageReceivedEvent event, String[] args);
}
