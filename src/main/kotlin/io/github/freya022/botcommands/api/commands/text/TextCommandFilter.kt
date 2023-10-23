package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Filters text commands, any filter that returns `false` prevents the command from executing.
 *
 * Filters are tested right before the command gets executed (i.e., after the permissions/rate limits... were checked).
 *
 * **Note:** This runs on every [command variation][TextCommandBuilder.variation].
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
interface TextCommandFilter : Filter {
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

// TODO this requires the reply to be done after the entire filter evaluation
//  As there would be an issue when the left side of the "or" fails (and replies) but the right side passes
//infix fun TextCommandFilter.or(other: TextCommandFilter): TextCommandFilter {
//    return object : TextCommandFilter {
//        override suspend fun isAcceptedSuspend(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): Boolean {
//            val isAccepted = this@or.isAcceptedSuspend(event, commandVariation, args)
//            return isAccepted || other.isAcceptedSuspend(event, commandVariation, args)
//        }
//    }
//}

infix fun TextCommandFilter.and(other: TextCommandFilter): TextCommandFilter {
    return object : TextCommandFilter {
        override suspend fun isAcceptedSuspend(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): Boolean {
            val isAccepted = this@and.isAcceptedSuspend(event, commandVariation, args)
            return isAccepted && other.isAcceptedSuspend(event, commandVariation, args)
        }
    }
}