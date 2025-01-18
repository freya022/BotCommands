package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Prevents text command execution by returning an error object to the command executor.
 *
 * Filters run when a [command variation][TextCommandBuilder.variation] is about to be executed,
 * i.e., after the permissions/rate limits... were checked.
 *
 * ### Combining filters
 *
 * Filters can be combined with [`and`][and]/[`or`][or] (static methods for Java users).
 *
 * ### Requirements
 * - Implement either [check] (Java) or [checkSuspend] (Kotlin).
 * - If you want this to be a global filter: Register it as a service with [@BService][BService]
 *
 * ### Execution order
 * The execution order of global filters is determined by the priority of the service,
 * while command-specific filters use the insertion order.
 *
 * ### Example - Accepting commands only in a single channel
 * **Note:** I recommend having a separate class to handle rejections,
 * as to not duplicate code on each filter.
 *
 * ```kt
 * @BService
 * class MyTextCommandFilter(
 *     private val rejectionHandler: MyTextCommandRejectionHandler,
 * ) : TextCommandFilter {
 *
 *     override suspend fun checkSuspend(
 *         event: MessageReceivedEvent,
 *         commandVariation: TextCommandVariation,
 *         args: String
 *     ): String? {
 *         if (event.channel.idLong != 722891685755093076) {
 *             rejectionHandler.handle(event, "Can only run commands in <#722891685755093076>")
 *             return "Wrong channel"
 *         }
 *         return null
 *     }
 * }
 * ```
 *
 * <Hr>
 *
 * ```java
 * @BService
 * public class MyTextCommandFilter implements TextCommandFilter {
 *
 *     private final MyTextCommandRejectionHandler rejectionHandler;
 *
 *     public MyTextCommandFilter(MyTextCommandRejectionHandler rejectionHandler) {
 *         this.rejectionHandler = rejectionHandler;
 *     }
 *
 *     @Nullable
 *     @Override
 *     public String check(@NotNull MessageReceivedEvent event, @NotNull TextCommandVariation commandVariation, @NotNull String args) {
 *         if (event.getChannel().getIdLong() != 722891685755093076L) {
 *             rejectionHandler.handle(event, "Can only run commands in <#722891685755093076>");
 *             return "Wrong channel";
 *         }
 *         return null;
 *     }
 * }
 * ```
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface TextCommandFilter : Filter {
    /**
     * Checks if this text command should run, returns `null` if this filter passes,
     * or a reason for the rejection, used for logging purposes.
     */
    @JvmSynthetic
    suspend fun checkSuspend(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): String? =
        check(event, commandVariation, args)

    /**
     * Checks if this text command should run, returns `null` if this filter passes,
     * or a reason for the rejection, used for logging purposes.
     */
    fun check(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): String? =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'check' or 'checkSuspend' method")
}

infix fun TextCommandFilter.or(other: TextCommandFilter): TextCommandFilter {
    return object : TextCommandFilter {
        override val description: String
            get() = "(${this@or.description} || ${other.description})"

        override suspend fun checkSuspend(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): String? {
            // Elvis operator short circuits if left condition had no error
            this@or.checkSuspend(event, commandVariation, args) ?: return null
            return other.checkSuspend(event, commandVariation, args)
        }
    }
}

infix fun TextCommandFilter.and(other: TextCommandFilter): TextCommandFilter {
    return object : TextCommandFilter {
        override val description: String
            get() = "(${this@and.description} && ${other.description})"

        override suspend fun checkSuspend(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): String? {
            val errorObject = this@and.checkSuspend(event, commandVariation, args)
            if (errorObject != null)
                return errorObject
            return other.checkSuspend(event, commandVariation, args)
        }
    }
}