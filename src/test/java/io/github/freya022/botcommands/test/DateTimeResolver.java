package io.github.freya022.botcommands.test;

import io.github.freya022.botcommands.api.parameters.ParameterResolver;
import io.github.freya022.botcommands.api.parameters.SlashParameterResolver;
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo;
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
	public LocalDateTime resolve(@NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return LocalDateTime.ofEpochSecond(optionMapping.getAsLong(), 0, ZoneOffset.UTC);
	}
}
