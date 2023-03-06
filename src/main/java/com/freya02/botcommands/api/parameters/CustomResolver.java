package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.ExecutableInteractionInfo;
import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a custom resolver for interaction parameters.
 * <br>This does not need to be implemented unlike other resolvers such as {@link SlashParameterResolver}.
 * <br><b>This resolver only gets used if an interaction parameter is not annotated with {@link AppOption}. or {@link TextOption}</b>
 *
 * @see CustomResolverFunction
 */
public final class CustomResolver extends ParameterResolver {
	private final CustomResolverFunction<?> function;

	/**
	 * @see CustomResolverFunction
	 */
	public <T> CustomResolver(Class<T> clazz, CustomResolverFunction<T> function) {
		super(clazz);

		this.function = function;
	}

	/**
	 * @see CustomResolverFunction
	 */
	@Nullable
	public Object resolve(BContext context, ExecutableInteractionInfo executableInteractionInfo, Event event) {
		return function.apply(context, executableInteractionInfo, event);
	}
}
