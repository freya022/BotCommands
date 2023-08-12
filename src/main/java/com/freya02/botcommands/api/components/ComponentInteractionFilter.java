package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.commands.application.ApplicationCommandFilter;
import com.freya02.botcommands.api.core.CooldownService;
import com.freya02.botcommands.api.core.config.BServiceConfigBuilder;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Filters component interactions (such as buttons and select menus),
 * any filter that returns {@code false} prevents the interaction from executing.
 *
 * <p>Filters are tested right before the component gets executed (i.e., after the component constraints were checked).
 *
 * <p><b>Note:</b> Your filter still has to acknowledge the interaction in case it rejects it.
 *
 * <p><b>Usage</b>: Register your instance as a service with {@link BService}
 * or {@link BServiceConfigBuilder#getServiceAnnotations() any annotation that enables your class for dependency injection}.
 *
 * @see InterfacedService @InterfacedService
 *
 * @see #isAccepted(GenericComponentInteractionCreateEvent)
 * @see CooldownService
 */
@InterfacedService(acceptMultiple = true)
public interface ComponentInteractionFilter {
	/**
	 * Returns whether the component interaction should be accepted or not.
	 *
	 * <p><b>Note:</b> Your filter still has to acknowledge the interaction in case it rejects it.
	 *
	 * @return {@code true} if the component interaction can run, {@code false} otherwise
	 *
	 * @see ApplicationCommandFilter
	 */
	boolean isAccepted(@NotNull GenericComponentInteractionCreateEvent event);
}
