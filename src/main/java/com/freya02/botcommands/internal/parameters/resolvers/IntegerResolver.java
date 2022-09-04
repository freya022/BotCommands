package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.annotations.IncludeClasspath;
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo;
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
public class IntegerResolver
		extends ParameterResolver<IntegerResolver, Integer>
		implements RegexParameterResolver<IntegerResolver, Integer>,
		           SlashParameterResolver<IntegerResolver, Integer>,
		           ComponentParameterResolver<IntegerResolver, Integer> {

	public IntegerResolver() {
		super(Integer.class);
	}

	@Override
	@Nullable
	public Integer resolve(@NotNull BContext context, @NotNull TextCommandInfo info, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
		try {
			return Integer.valueOf(args[0]);
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

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.INTEGER;
	}

	@Override
	@Nullable
	public Integer resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		try {
			return optionMapping.getAsInt();
		} catch (NumberFormatException e) { //Can't have discord to send us actual input when autocompleting lmao
			return 0;
		}
	}

	@Override
	@Nullable
	public Integer resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		return Integer.valueOf(arg);
	}
}