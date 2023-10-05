package io.github.freya022.botcommands.internal.parameters.resolvers;

import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import io.github.freya022.botcommands.api.parameters.ParameterResolver;
import io.github.freya022.botcommands.api.parameters.SlashParameterResolver;
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Resolver
public class MentionableResolver
		extends ParameterResolver<MentionableResolver, IMentionable>
		implements SlashParameterResolver<MentionableResolver, IMentionable> {

	public MentionableResolver() {
		super(IMentionable.class);
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.MENTIONABLE;
	}

	@Override
	@Nullable
	public IMentionable resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return optionMapping.getAsMentionable();
	}
}
