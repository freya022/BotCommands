package com.freya02.bot.wiki.paramresolver;

import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.CommandPayload;
import net.dv8tion.jda.api.utils.Timestamp;

import java.util.regex.Matcher;

import static net.dv8tion.jda.api.utils.TimeFormat.*;

//Create the resolver
public class TimestampResolver extends ParameterResolver implements SlashParameterResolver {
	public TimestampResolver() {
		super(Timestamp.class);
	}

	@Override
	public Object resolve(CommandPayload event, OptionMapping optionMapping) {
		final Matcher timestampMatcher = MARKDOWN.matcher(optionMapping.getAsString());
		if (!timestampMatcher.find()) return null; //Avoid expensive exceptions from JDA

		final String format = timestampMatcher.group("style");
		final long time = Long.parseLong(timestampMatcher.group("time"));
		return (format == null ? DEFAULT : fromStyle(format)).atTimestamp(time);
	}
}