package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.ExecutableInteractionInfo;
import net.dv8tion.jda.api.events.Event;

public interface CustomResolverFunction<T> {
	T apply(BContext context, ExecutableInteractionInfo executableInteractionInfo, Event event);
}
