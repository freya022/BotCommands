package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.core.CooldownService
import com.freya02.botcommands.api.core.config.BServiceConfigBuilder
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.InterfacedService
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

/**
 * Filters application command interactions (such as slash commands and user/message context commands),
 * any filter that returns `false` prevents the command from executing.
 *
 * Filters are tested right before the command gets executed (i.e., after the permissions/cooldown... were checked).
 *
 * **Note:** Your filter still has to acknowledge the interaction in case it rejects it.
 *
 * **Usage**: Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * @see InterfacedService @InterfacedService
 *
 * @see isAccepted
 * @see CooldownService
 */
@InterfacedService(acceptMultiple = true)
interface ApplicationCommandFilter {
    /**
     * Returns whether the command should be accepted or not.
     *
     * **Note:** Your filter still has to acknowledge the interaction in case it rejects it.
     *
     * @return `true` if the application command can run, `false` otherwise
     *
     * @see ApplicationCommandFilter
     */
    fun isAccepted(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Boolean
}
