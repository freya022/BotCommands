package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.interactions.commands.CommandPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

public class MentionableResolver extends ParameterResolver implements SlashParameterResolver {
	public MentionableResolver() {
		super(IMentionable.class);
	}

	@Override
	@Nullable
	public Object resolve(CommandPayload event, OptionMapping optionMapping) {
		return optionMapping.getAsMentionable();
	}
}
