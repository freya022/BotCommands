package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

/**
 * Prevents application command execution by returning an error object to the command executor.
 *
 * Filters run when an application command is about to be executed,
 * i.e., after the permissions/rate limits... were checked.
 *
 * When the final filter returns an error object of type [T],
 * it will then be passed to the [ApplicationCommandRejectionHandler].
 *
 * ### Combining filters
 *
 * Filters can be combined with [`and`][and]/[`or`][or] (static methods for Java users).
 *
 * ### Requirements
 * - Register your instance as a service with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 * This is not required if you pass the instance directly to the command builder.
 * - Have exactly one instance of [ApplicationCommandRejectionHandler].
 * - Implement either [check] (Java) or [checkSuspend] (Kotlin).
 * - (Optional) Set your filter as a command-specific filter by disabling [global].
 *
 * ### Execution order
 * The execution order of global filters is determined by the priority of the service,
 * while command-specific filters use the insertion order.
 *
 * ### Example - Accepting commands only in a single channel
 * ```kt
 * @BService
 * class MyApplicationCommandFilter : ApplicationCommandFilter<String> {
 *     override suspend fun checkSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): String? {
 *         if (event.guildChannel.idLong != 722891685755093076) {
 *             return "Can only run commands in <#722891685755093076>"
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
 * public class MyApplicationCommandFilter implements ApplicationCommandFilter<String> {
 *     @Nullable
 *     @Override
 *     public String check(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo) {
 *         if (channel.getIdLong() != 722891685755093076L) {
 *             return "Can only run commands in <#722891685755093076>";
 *         }
 *         return null;
 *     }
 * }
 * ```
 *
 * @param T Type of the error object handled by [ApplicationCommandRejectionHandler]
 *
 * @see ApplicationCommandRejectionHandler
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface ApplicationCommandFilter<T : Any> : Filter {
    /**
     * Returns `null` if this filter should allow the command to run, or returns your own object if it can't.
     *
     * The object will be passed to your [ApplicationCommandRejectionHandler] if the command is rejected.
     */
    @JvmSynthetic
    suspend fun checkSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): T? =
        check(event, commandInfo)

    /**
     * Returns `null` if this filter should allow the command to run, or returns your own object if it can't.
     *
     * The object will be passed to your [ApplicationCommandRejectionHandler] if the command is rejected.
     */
    fun check(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): T? =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'check' or 'checkSuspend' method")
}

infix fun <T : Any> ApplicationCommandFilter<T>.or(other: ApplicationCommandFilter<T>): ApplicationCommandFilter<T> {
    return object : ApplicationCommandFilter<T> {
        override val global: Boolean = false

        override val description: String
            get() = "(${this@or.description} || ${other.description})"

        override suspend fun checkSuspend(
            event: GenericCommandInteractionEvent,
            commandInfo: ApplicationCommandInfo
        ): T? {
            // Elvis operator short circuits if left condition had no error
            this@or.checkSuspend(event, commandInfo) ?: return null
            return other.checkSuspend(event, commandInfo)
        }
    }
}

infix fun <T : Any> ApplicationCommandFilter<T>.and(other: ApplicationCommandFilter<T>): ApplicationCommandFilter<T> {
    return object : ApplicationCommandFilter<T> {
        override val global: Boolean = false

        override val description: String
            get() = "(${this@and.description} && ${other.description})"

        override suspend fun checkSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): T? {
            val errorObject = this@and.checkSuspend(event, commandInfo)
            if (errorObject != null)
                return errorObject
            return other.checkSuspend(event, commandInfo)
        }
    }
}
