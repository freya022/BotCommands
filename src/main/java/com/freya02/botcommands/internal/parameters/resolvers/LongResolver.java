package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.core.BContext;
import com.freya02.botcommands.api.core.service.annotations.Resolver;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import kotlin.reflect.KParameter;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@Resolver
public class LongResolver
		extends ParameterResolver<LongResolver, Long>
		implements RegexParameterResolver<LongResolver, Long>,
		           SlashParameterResolver<LongResolver, Long>,
		           ComponentParameterResolver<LongResolver, Long> {

	public LongResolver() {
		super(Long.class);
	}

	@Override
	@Nullable
	public Long resolve(@NotNull BContext context, @NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
		try {
			return Long.valueOf(args[0]);
		} catch (NumberFormatException e) {
			return null;
		}
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

	@NotNull
	@Override
	public String getHelpExample(@NotNull KParameter parameter, @NotNull BaseCommandEvent event, boolean isID) {
		return isID ? "222046562543468545" : "42";
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.INTEGER;
	}

	@Override
	@Nullable
	public Long resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		try {
			return optionMapping.getAsLong();
		} catch (NumberFormatException e) { //Can't have discord to send us actual input when autocompleting lmao
			return 0L;
		}
	}

	@Override
	@Nullable
	public Long resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		return Long.valueOf(arg);
	}
}
