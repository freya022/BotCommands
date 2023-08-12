package com.freya02.botcommands.api.commands.application;

import com.freya02.botcommands.api.core.CooldownService;
import com.freya02.botcommands.api.core.config.BServiceConfigBuilder;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Filters application command interactions (such as slash commands and user/message context commands),
 * any filter that returns {@code false} prevents the command from executing.
 *
 * <p>Filters are tested right before the command gets executed (i.e., after the permissions/cooldown... were checked).
 *
 * <p><b>Note:</b> Your filter still has to acknowledge the interaction.
 *
 * <p><b>Usage</b>: Register your instance as a service with {@link BService}
 * or {@link BServiceConfigBuilder#getServiceAnnotations() any annotation that enables your class for dependency injection}.
 *
 * @see InterfacedService @InterfacedService
 *
 * @see #isAccepted(GenericCommandInteractionEvent, ApplicationCommandInfo)
 * @see CooldownService
 */
@InterfacedService(acceptMultiple = true)
public interface ApplicationCommandFilter {
    /**
     * Returns whether the command should be accepted or not.
     *
     * <p><b>Note:</b> Your filter still has to acknowledge the interaction in case it rejects it.
     *
     * @return {@code true} if the application command can run, {@code false} otherwise
     *
     * @see ApplicationCommandFilter
     */
    boolean isAccepted(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo);
}
