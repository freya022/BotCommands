package com.freya02.bot.wiki.paramresolver;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.Timestamp;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

import static net.dv8tion.jda.api.utils.TimeFormat.*;

//Create the resolver
public class TimestampResolver extends ParameterResolver implements SlashParameterResolver {
	public TimestampResolver() {
		super(Timestamp.class);
	}

	@Override
	public Object resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		final Matcher timestampMatcher = MARKDOWN.matcher(optionMapping.getAsString());
		if (!timestampMatcher.find()) return null; //Avoid expensive exceptions from JDA

		final String format = timestampMatcher.group("style");
		final long time = Long.parseLong(timestampMatcher.group("time"));
		return (format == null ? DEFAULT : fromStyle(format)).atTimestamp(time);
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.STRING;
	}
}