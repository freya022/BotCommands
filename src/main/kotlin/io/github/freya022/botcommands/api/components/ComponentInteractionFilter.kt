package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
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
 * With more complex filters such as [`and`][and]/[`or`][or] filters (static methods for Java users),
 * a filter returning an error object does not mean a component is rejected.
 *
 * Instead, the cause of the error will be passed down to the component executor,
 * and then given back to the [ComponentInteractionRejectionHandler].
 *
 * ### Usage
 * - Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations]
 * (this is required as the service is retrieved when the component is used).
 * - Have exactly one instance of [ComponentInteractionRejectionHandler].
 * - Implement either [check] (Java) or [checkSuspend] (Kotlin).
 * - (Optional) Set your filter as a component-specific filter by disabling [global].
 *
 * **Note:** The execution order of global filters is determined by the priority of the service,
 * while command-specific filters use the insertion order.
 *
 * Filters component interactions (such as buttons and select menus),
 * any filter that returns `false` prevents the interaction from executing.
 *
 * Filters are tested right before the component gets executed (i.e., after the constraints/rate limits were checked).
 *
 * **Note:** Your filter still has to acknowledge the interaction in case it rejects it.
 *
 * **Usage**: Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * ### Example - Rejecting component interactions from non-owners
 * ```kt
 * @BService
 * class MyComponentFilter(private val context: BContext) : ComponentInteractionFilter<String> {
 *     override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? {
 *         if (event.channel.idLong == 932902082724380744 && event.user.idLong !in context.ownerIds) {
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
 *     private final BContext context;
 *
 *     public MyComponentFilter(BContext context) {
 *         this.context = context;
 *     }
 *
 *     @Nullable
 *     @Override
 *     public String check(@NotNull GenericComponentInteractionCreateEvent event, @Nullable String handlerName) {
 *         if (event.getChannel().getIdLong() == 932902082724380744L && context.isOwner(event.getUser().getIdLong())) {
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
    //TODO remove in alpha 9
    @Deprecated(
        message = "Implement 'checkSuspend' instead, do not return a boolean",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("checkSuspend(event, handlerName)")
    )
    @JvmSynthetic
    suspend fun isAcceptedSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): Boolean =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'check' or 'checkSuspend' method")

    //TODO remove in alpha 9
    @Deprecated(
        message = "Implement 'check' instead, do not return a boolean",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("check(event, handlerName)")
    )
    fun isAccepted(event: GenericComponentInteractionCreateEvent, handlerName: String?): Boolean =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'check' or 'checkSuspend' method")

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