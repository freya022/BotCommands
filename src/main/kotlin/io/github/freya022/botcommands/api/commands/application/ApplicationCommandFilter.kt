package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfo
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

/**
 * Prevents application command execution by returning an error object to the command executor.
 *
 * Filters run when an application command is about to be executed,
 * i.e., after the permissions/rate limits... were checked.
 *
 * With more complex filters such as [`and`][and]/[`or`][or] filters,
 * a filter returning an error object does not mean a command is rejected.
 *
 * Instead, the cause of the error will be passed down to the command executor,
 * and then given back to the [ApplicationCommandRejectionHandler].
 *
 * ### Usage
 * - Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 * This is not required if you pass the instance directly to the command builder.
 * - Have exactly one instance of [ApplicationCommandRejectionHandler].
 * - Implement either [check] (Java) or [checkSuspend] (Kotlin).
 * - (Optional) Set your filter as a command-specific filter by disabling [global].
 *
 * TODO update examples
 * ### Example - Accepting commands only in a single channel:
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
 * @param T Type of the error object handled by [ApplicationCommandRejectionHandler]
 *
 * @see ApplicationCommandRejectionHandler
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface ApplicationCommandFilter<T : Any> : Filter {
    //TODO remove in alpha 9
    @Deprecated(
        message = "Implement 'checkSuspend' instead, do not return a boolean",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("checkSuspend(event, commandInfo)")
    )
    @JvmSynthetic
    suspend fun isAcceptedSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Boolean =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'check' or 'checkSuspend' method")

    //TODO remove in alpha 9
    @Deprecated(
        message = "Implement 'check' instead, do not return a boolean",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("check(event, commandInfo)")
    )
    fun isAccepted(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Boolean =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'check' or 'checkSuspend' method")

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

// TODO this requires the reply to be done after the entire filter evaluation
//  As there would be an issue when the left side of the "or" fails (and replies) but the right side passes
//infix fun ApplicationCommandFilter.or(other: ApplicationCommandFilter): ApplicationCommandFilter {
//    return object : ApplicationCommandFilter {
//        override suspend fun isAcceptedSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Boolean {
//            val isAccepted = this@or.isAcceptedSuspend(event, commandInfo)
//            return isAccepted || other.isAcceptedSuspend(event, commandInfo)
//        }
//    }
//}

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