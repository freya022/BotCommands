package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.interactions.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class DoubleResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public DoubleResolver() {
		super(Double.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return Double.valueOf(args[0]);
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return Pattern.compile("(\\d+)");
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "1234.42";
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandInteraction event, OptionMapping optionMapping) {
		return Double.valueOf(optionMapping.getAsString());
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		return Double.valueOf(arg);
	}
}
