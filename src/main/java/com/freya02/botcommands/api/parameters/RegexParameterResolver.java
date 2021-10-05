package com.freya02.botcommands.api.parameters;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Interface which indicates this class can resolve parameters for regex commands
 */
public interface RegexParameterResolver {
	@Nullable
	Object resolve(GuildMessageReceivedEvent event, String[] args);

	@NotNull
	Pattern getPattern();

	@NotNull
	String getTestExample();

	default Pattern getPreferredPattern() {
		if (this instanceof QuotableRegexParameterResolver) {
			return ((QuotableRegexParameterResolver) this).getQuotedPattern();
		} else {
			return getPattern();
		}
	}
}
