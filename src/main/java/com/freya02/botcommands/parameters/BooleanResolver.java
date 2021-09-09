package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

public class BooleanResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public BooleanResolver() {
		super(Boolean.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return parseBoolean(args[0]);
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandEvent event, OptionMapping optionMapping) {
		return optionMapping.getAsBoolean();
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		return parseBoolean(arg);
	}

	@Nullable
	private Object parseBoolean(String arg) {
		if (arg.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		} else if (arg.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		} else {
			return null;
		}
	}
}
