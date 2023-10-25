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
 * Prevents text command execution by returning an error object to the command executor.
 *
 * Filters run when a [command variation][TextCommandBuilder.variation] is about to be executed,
 * i.e., after the permissions/rate limits... were checked.
 *
 * With more complex filters such as [`and`][and]/[`or`][or] filters,
 * a filter returning an error object does not mean a command is rejected.
 *
 * Instead, the cause of the error will be passed down to the command executor,
 * and then given back to the [TextCommandRejectionHandler].
 *
 * ### Usage
 * - Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 * - Have exactly one instance of [TextCommandRejectionHandler].
 * - Implement either [check] (Java) or [checkSuspend] (Kotlin).
 * - (Optional) Set your filter as a command-specific filter by disabling [global].
 *
 * TODO update examples
 * ### Example - Accepting commands only in a single channel:
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
 * @see TextCommandRejectionHandler
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface TextCommandFilter : Filter {
    //TODO remove in alpha 9
    @Deprecated(
        message = "Implement 'checkSuspend' instead, do not return a boolean",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("checkSuspend(event, commandVariation, args)")
    )
    @JvmSynthetic
    suspend fun isAcceptedSuspend(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): Boolean =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'check' or 'checkSuspend' method")

    //TODO remove in alpha 9
    @Deprecated(
        message = "Implement 'check' instead, do not return a boolean",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("check(event, commandVariation, args)")
    )
    fun isAccepted(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): Boolean =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'check' or 'checkSuspend' method")

    /**
     * Returns `null` if this filter should allow the command to run, or returns your own object if it can't.
     *
     * The object will be passed to your [TextCommandRejectionHandler] if the command is rejected.
     */
    @JvmSynthetic
    suspend fun checkSuspend(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): Any? =
        check(event, commandVariation, args)

    /**
     * Returns `null` if this filter should allow the command to run, or returns your own object if it can't.
     *
     * The object will be passed to your [TextCommandRejectionHandler] if the command is rejected.
     */
    fun check(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): Any? =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'check' or 'checkSuspend' method")
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
        override val global: Boolean = false

        override val description: String
            get() = "(${this@and.description} && ${other.description})"

        override suspend fun checkSuspend(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): Any? {
            val errorObject = this@and.checkSuspend(event, commandVariation, args)
            if (errorObject != null)
                return errorObject
            return other.checkSuspend(event, commandVariation, args)
        }
    }
}