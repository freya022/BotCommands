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

public class LongResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public LongResolver() {
		super(Long.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return Long.valueOf(args[0]);
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return Pattern.compile("(\\d+)");
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "1234";
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandInteraction event, OptionMapping optionMapping) {
		return optionMapping.getAsLong();
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		return Long.valueOf(arg);
	}
}
