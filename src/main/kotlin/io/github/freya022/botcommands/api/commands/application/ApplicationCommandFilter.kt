package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

/**
 * Prevents application command execution by returning an error object to the command executor,
 * before which you must acknowledge the interaction.
 *
 * Filters run when an application command is about to be executed,
 * i.e., after the permissions/rate limits... were checked.
 *
 * ### Combining filters
 *
 * Filters can be combined with [`and`][and]/[`or`][or] (static methods for Java users).
 *
 * ### Requirements
 * - Register your instance as a service with [@BService][BService].
 * This is not required if you pass the instance directly to the command builder.
 * - Implement either [check] (Java) or [checkSuspend] (Kotlin).
 * - (Optional) Set your filter as a command-specific filter by disabling [global].
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
 * class MyApplicationCommandFilter(
 *     private val rejectionHandler: MyApplicationCommandRejectionHandler,
 * ) : ApplicationCommandFilter {
 *
 *     override suspend fun checkSuspend(
 *         event: GenericCommandInteractionEvent,
 *         commandInfo: ApplicationCommandInfo
 *     ): String? {
 *         if (event.channel!!.idLong != 722891685755093076) {
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
 * public class MyApplicationCommandFilter implements ApplicationCommandFilter {
 *
 *     private final MyApplicationCommandRejectionHandler rejectionHandler;
 *
 *     public MyApplicationCommandFilter(MyApplicationCommandRejectionHandler rejectionHandler) {
 *         this.rejectionHandler = rejectionHandler;
 *     }
 *
 *     @Nullable
 *     @Override
 *     public String check(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo) {
 *         if (event.getChannel().getIdLong() != 722891685755093076L) {
 *             rejectionHandler.handle(event, "Can only run commands in <#722891685755093076>");
 *             return "Not the right channel";
 *         }
 *         return null;
 *     }
 * }
 * ```
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface ApplicationCommandFilter : Filter {
    /**
     * Checks if this interaction should run, returns `null` if this filter passes,
     * or a reason for the rejection, used for logging purposes.
     */
    @JvmSynthetic
    suspend fun checkSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): String? =
        check(event, commandInfo)

    /**
     * Checks if this interaction should run, returns `null` if this filter passes,
     * or a reason for the rejection, used for logging purposes.
     */
    fun check(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): String? =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'check' or 'checkSuspend' method")
}

infix fun ApplicationCommandFilter.or(other: ApplicationCommandFilter): ApplicationCommandFilter {
    return object : ApplicationCommandFilter {
        override val global: Boolean = false

        override val description: String
            get() = "(${this@or.description} || ${other.description})"

        override suspend fun checkSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): String? {
            // Elvis operator short circuits if left condition had no error
            this@or.checkSuspend(event, commandInfo) ?: return null
            return other.checkSuspend(event, commandInfo)
        }
    }
}

infix fun ApplicationCommandFilter.and(other: ApplicationCommandFilter): ApplicationCommandFilter {
    return object : ApplicationCommandFilter {
        override val global: Boolean = false

        override val description: String
            get() = "(${this@and.description} && ${other.description})"

        override suspend fun checkSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): String? {
            val errorObject = this@and.checkSuspend(event, commandInfo)
            if (errorObject != null)
                return errorObject
            return other.checkSuspend(event, commandInfo)
        }
    }
}
