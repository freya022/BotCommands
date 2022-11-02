package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.annotations.IncludeClasspath;
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@IncludeClasspath
public class DoubleResolver
		extends ParameterResolver<DoubleResolver, Double>
		implements RegexParameterResolver<DoubleResolver, Double>,
		           SlashParameterResolver<DoubleResolver, Double>,
		           ComponentParameterResolver<DoubleResolver, Double> {

	public DoubleResolver() {
		super(Double.class);
	}

	@Override
	@Nullable
	public Double resolve(@NotNull BContext context, @NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
		try {
			return Double.valueOf(args[0]);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return Pattern.compile("([-+]?[0-9]*[.,]?[0-9]+)");
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "1234.42";
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.NUMBER;
	}

	@Override
	@Nullable
	public Double resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		try {
			return optionMapping.getAsDouble();
		} catch (NumberFormatException e) { //Can't have discord to send us actual input when autocompleting lmao
			return 0d;
		}
	}

	@Override
	@Nullable
	public Double resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		return Double.valueOf(arg);
	}
}
