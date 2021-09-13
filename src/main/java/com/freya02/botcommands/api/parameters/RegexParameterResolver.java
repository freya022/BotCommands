package com.freya02.botcommands.api.parameters;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Interface which indicates this class can resolve parameters for regex commands
 */
public interface RegexParameterResolver {
	@Nullable
	Object resolve(GuildMessageReceivedEvent event, String[] args);
}
