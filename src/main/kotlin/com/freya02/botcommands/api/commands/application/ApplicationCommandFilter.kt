package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.core.config.BServiceConfigBuilder
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.InterfacedService
import com.freya02.botcommands.api.core.utils.simpleNestedName
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
 * **Example** - Rejecting commands from outside a channel:
 * ```kt
 * @BService
 * class MyCommandFilters : ApplicationCommandFilter {
 *     override suspend fun isAcceptedSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Boolean {
 *         if (event.channel?.idLong != 722891685755093076) {
 *             event.reply_("Commands are not allowed in this channel", ephemeral = true).queue()
 *             return false
 *         }
 *         return true
 *     }
 * }
 * ```
 *
 * <Hr>
 *
 * ```java
 * @BService
 * public class MyApplicationCommandFilter implements ApplicationCommandFilter {
 *     @Override
 *     public boolean isAccepted(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo) {
 *         if (event.getChannel() == null || event.getChannel().getIdLong() != 722891685755093076L) {
 *             event.reply("Commands are not allowed in this channel").setEphemeral(true).queue();
 *             return false;
 *         }
 *         return true;
 *     }
 * }
 * ```
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
    @JvmSynthetic
    suspend fun isAcceptedSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Boolean =
        isAccepted(event, commandInfo)

    /**
     * Returns whether the command should be accepted or not.
     *
     * **Note:** Your filter still has to acknowledge the interaction in case it rejects it.
     *
     * @return `true` if the application command can run, `false` otherwise
     *
     * @see ApplicationCommandFilter
     */
    fun isAccepted(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Boolean =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'isAccepted' or 'isAcceptedSuspend' method")
}
