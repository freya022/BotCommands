package com.freya02.botcommands.api.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder
import com.freya02.botcommands.api.core.config.BServiceConfigBuilder
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.InterfacedService
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Filters text commands, any filter that returns `false` prevents the command from executing.
 *
 * Filters are tested right before the command gets executed (i.e., after the permissions/rate limits... were checked).
 *
 * **Note:** this runs on every [command variation][TextCommandBuilder.variation].
 *
 * **Usage**: Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * **Example** - Rejecting commands from outside a channel:
 * ```kt
 * @BService
 * class MyTextCommandFilter : TextCommandFilter {
 *     override suspend fun isAcceptedSuspend(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): Boolean {
 *         return event.channel.idLong == 722891685755093076
 *     }
 * }
 * ```
 *
 * <Hr>
 *
 * ```java
 * @BService
 * public class MyTextCommandFilter implements TextCommandFilter {
 *     @Override
 *     public boolean isAccepted(@NotNull MessageReceivedEvent event, @NotNull TextCommandVariation commandVariation, @NotNull String args) {
 *         return event.getChannel().getIdLong() == 722891685755093076L;
 *     }
 * }
 * ```
 *
 * @see InterfacedService @InterfacedService
 *
 * @see isAccepted
 */
@InterfacedService(acceptMultiple = true)
interface TextCommandFilter {
    /**
     * Returns whether the command should be accepted or not.
     *
     * @return `true` if the command can run, `false` otherwise
     *
     * @see TextCommandFilter
     */
    @JvmSynthetic
    suspend fun isAcceptedSuspend(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): Boolean =
        isAccepted(event, commandVariation, args)

    /**
     * Returns whether the command should be accepted or not.
     *
     * @return `true` if the command can run, `false` otherwise
     *
     * @see TextCommandFilter
     */
    fun isAccepted(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): Boolean =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'isAccepted' or 'isAcceptedSuspend' method")
}
