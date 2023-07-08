package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.core.service.annotations.Resolver;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
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

@Resolver
public class BooleanResolver
		extends ParameterResolver<BooleanResolver, Boolean>
		implements RegexParameterResolver<BooleanResolver, Boolean>,
		           SlashParameterResolver<BooleanResolver, Boolean>,
		           ComponentParameterResolver<BooleanResolver, Boolean> {

	public BooleanResolver() {
		super(Boolean.class);
	}

	@Override
	@Nullable
	public Boolean resolve(@NotNull BContext context, @NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
		return parseBoolean(args[0]);
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return Pattern.compile("(true|false)", Pattern.CASE_INSENSITIVE);
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "true";
	}

	@NotNull
	@Override
	public String getHelpExample(boolean isID) {
		return "true";
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.BOOLEAN;
	}

	@Override
	@Nullable
	public Boolean resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return optionMapping.getAsBoolean();
	}

	@Override
	@Nullable
	public Boolean resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		return parseBoolean(arg);
	}

	@Nullable
	private Boolean parseBoolean(String arg) {
		if (arg.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		} else if (arg.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		} else {
			return null;
		}
	}
}
