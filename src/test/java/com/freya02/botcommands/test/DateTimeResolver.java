package com.freya02.botcommands.test;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

class DateTimeResolver
		extends ParameterResolver<DateTimeResolver, LocalDateTime>
		implements SlashParameterResolver<DateTimeResolver, LocalDateTime> {

	public DateTimeResolver() {
		super(LocalDateTime.class);
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.STRING;
	}

	@Override
	@Nullable
	public LocalDateTime resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return LocalDateTime.ofEpochSecond(optionMapping.getAsLong(), 0, ZoneOffset.UTC);
	}
}
