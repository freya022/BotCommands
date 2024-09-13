package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

/**
 * Prevents component execution by returning an error object to the component executor.
 *
 * Filters run when a component is about to be executed,
 * i.e., after the constraints/rate limits... were checked.
 *
 * When the final filter returns an error object of type [T],
 * it will then be passed to the [ComponentInteractionRejectionHandler].
 *
 * ### Combining filters
 *
 * Filters can be combined with [`and`][and]/[`or`][or] (static methods for Java users).
 *
 * **Note:** Filters that are not accessible via dependency injection cannot be used.
 * For example, if you wish to combine filters using [`and`][and]/[`or`][or],
 * you should make a service factory that combines both filters and exposes the new one as a service:
 *
 * ```kt
 * @BConfiguration
 * class AdminAndInVoiceChannelFilterProvider {
 *     @BService
 *     fun adminAndInVoiceChannelFilter(
 *         adminFilter: AdminFilter,
 *         inVoiceChannelFilter: InVoiceChannelFilter
 *     ): ComponentInteractionFilter<String> = adminFilter and inVoiceChannelFilter
 * }
 * ```
 *
 * ### Requirements
 * - Register your instance as a service with [@BService][BService].
 * - Have exactly one instance of [ComponentInteractionRejectionHandler].
 * - Implement either [check] (Java) or [checkSuspend] (Kotlin).
 * - (Optional) Set your filter as a component-specific filter by disabling [global].
 *
 * ### Execution order
 * The execution order of global filters is determined by the priority of the service,
 * while component-specific filters use the insertion order.
 *
 * ### Example - Rejecting component interactions from non-owners
 * ```kt
 * @BService
 * class MyComponentFilter(private val botOwners: BotOwners) : ComponentInteractionFilter<String> {
 *     override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? {
 *         if (event.channel.idLong == 932902082724380744 && event.user !in botOwners) {
 *             return "Only owners are allowed to use components in <#932902082724380744>"
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
 * public class MyComponentFilter implements ComponentInteractionFilter<String> {
 *     private final BotOwners botOwners;
 *
 *     public MyComponentFilter(BotOwners botOwners) {
 *         this.botOwners = botOwners;
 *     }
 *
 *     @Nullable
 *     @Override
 *     public String check(@NotNull GenericComponentInteractionCreateEvent event, @Nullable String handlerName) {
 *         if (event.getChannel().getIdLong() == 932902082724380744L && !botOwners.isOwner(event.getUser())) {
 *             return "Only owners are allowed to use components in <#932902082724380744>";
 *         }
 *         return null;
 *     }
 * }
 * ```
 *
 * @param T Type of the error object handled by [ComponentInteractionRejectionHandler]
 *
 * @see ComponentInteractionRejectionHandler
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface ComponentInteractionFilter<T : Any> : Filter {
    /**
     * Returns `null` if this filter should allow the component to be used, or returns your own object if not.
     *
     * The object will be passed to your [ComponentInteractionRejectionHandler]
     * if the component interaction is rejected.
     *
     * @param handlerName The persistent handler name, as declared in [JDAButtonListener]/[JDASelectMenuListener],
     *                    might be null if there is no handler defined, or is ephemeral.
     */
    @JvmSynthetic
    suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): T? =
        check(event, handlerName)

    /**
     * Returns `null` if this filter should allow the component to be used, or returns your own object if not.
     *
     * The object will be passed to your [ComponentInteractionRejectionHandler]
     * if the component interaction is rejected.
     *
     * @param handlerName The persistent handler name, as declared in [JDAButtonListener]/[JDASelectMenuListener],
     *                    might be null if there is no handler defined, or is ephemeral.
     */
    fun check(event: GenericComponentInteractionCreateEvent, handlerName: String?): T? =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'isAccepted' or 'isAcceptedSuspend' method")

    companion object {
        @JvmStatic
        @JvmName("or")
        fun <T : Any> orJava(left: ComponentInteractionFilter<T>, right: ComponentInteractionFilter<T>): ComponentInteractionFilter<T> {
            return left or right
        }

        @JvmStatic
        @JvmName("and")
        fun <T : Any> andJava(left: ComponentInteractionFilter<T>, right: ComponentInteractionFilter<T>): ComponentInteractionFilter<T> {
            return left and right
        }
    }
}

infix fun <T : Any> ComponentInteractionFilter<T>.or(other: ComponentInteractionFilter<T>): ComponentInteractionFilter<T> {
    return object : ComponentInteractionFilter<T> {
        override val global: Boolean = false

        override val description: String
            get() = "(${this@or.description} || ${other.description})"

        override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): T? {
            // Elvis operator short circuits if left condition had no error
            this@or.checkSuspend(event, handlerName) ?: return null
            return other.checkSuspend(event, handlerName)
        }
    }
}

infix fun <T : Any> ComponentInteractionFilter<T>.and(other: ComponentInteractionFilter<T>): ComponentInteractionFilter<T> {
    return object : ComponentInteractionFilter<T> {
        override val global: Boolean = false

        override val description: String
            get() = "(${this@and.description} && ${other.description})"

        override suspend fun checkSuspend(
            event: GenericComponentInteractionCreateEvent,
            handlerName: String?
        ): T? {
            val errorObject = this@and.checkSuspend(event, handlerName)
            if (errorObject != null)
                return errorObject
            return other.checkSuspend(event, handlerName)
        }
    }
}