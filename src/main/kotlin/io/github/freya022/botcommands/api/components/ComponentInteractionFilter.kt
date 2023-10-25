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
 * With more complex filters such as [`and`][and]/[`or`][or] filters,
 * a filter returning an error object does not mean a component is rejected.
 *
 * Instead, the cause of the error will be passed down to the component executor,
 * and then given back to the [ComponentInteractionRejectionHandler].
 *
 * ### Usage
 * - Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 * - Have exactly one instance of [ComponentInteractionRejectionHandler].
 * - Implement either [check] (Java) or [checkSuspend] (Kotlin).
 * - (Optional) Set your filter as a component-specific filter by disabling [global].
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
 * TODO update examples
 * ### Example - Rejecting component interactions from non-owners:
 * ```kt
 * @BService
 * class MyComponentFilter(private val config: BConfig) : ComponentInteractionFilter {
 *     override suspend fun isAcceptedSuspend(event: GenericComponentInteractionCreateEvent): Boolean {
 *         if (event.user.idLong !in config.ownerIds) {
 *             event.reply_("Only owners are allowed to use components", ephemeral = true).queue()
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
 * public class MyComponentCommandFilter implements ApplicationCommandFilter {
 *     private final BConfig config;
 *
 *     public MyComponentCommandFilter(BConfig config) {
 *         this.config = config;
 *     }
 *
 *     @Override
 *     public boolean isAccepted(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo) {
 *         if (!config.isOwner(event.getUser().getIdLong())) {
 *             event.reply("Only owners are allowed to use components").setEphemeral(true).queue();
 *             return false;
 *         }
 *         return true;
 *     }
 * }
 * ```
 *
 * @see ComponentInteractionRejectionHandler
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface ComponentInteractionFilter : Filter {
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
    suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): Any? =
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
    fun check(event: GenericComponentInteractionCreateEvent, handlerName: String?): Any? =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'isAccepted' or 'isAcceptedSuspend' method")
}

// TODO this requires the reply to be done after the entire filter evaluation
//  As there would be an issue when the left side of the "or" fails (and replies) but the right side passes
//infix fun ComponentInteractionFilter.or(other: ComponentInteractionFilter): ComponentInteractionFilter {
//    return object : ComponentInteractionFilter {
//        override suspend fun isAcceptedSuspend(
//            event: GenericComponentInteractionCreateEvent,
//            handlerName: String?
//        ): Boolean {
//            val isAccepted = this@or.isAcceptedSuspend(event, handlerName)
//            return isAccepted || other.isAcceptedSuspend(event, handlerName)
//        }
//    }
//}

infix fun ComponentInteractionFilter.and(other: ComponentInteractionFilter): ComponentInteractionFilter {
    return object : ComponentInteractionFilter {
        override val global: Boolean = false

        override val description: String
            get() = "(${this@and.description} && ${other.description})"

        override suspend fun checkSuspend(
            event: GenericComponentInteractionCreateEvent,
            handlerName: String?
        ): Any? {
            val errorObject = this@and.checkSuspend(event, handlerName)
            if (errorObject != null)
                return errorObject
            return other.checkSuspend(event, handlerName)
        }
    }
}