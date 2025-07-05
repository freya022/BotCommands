package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

/**
 * Prevents component execution by returning an error object to the component executor,
 * before which you must acknowledge the interaction.
 *
 * Filters run when a component is about to be executed,
 * i.e., after the constraints/rate limits... were checked.
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
 * - Implement either [check] (Java) or [checkSuspend] (Kotlin).
 * - (Optional) Set your filter as a component-specific filter by disabling [global].
 *
 * ### Execution order
 * The execution order of global filters is determined by the priority of the service,
 * while component-specific filters use the insertion order.
 *
 * ### Example - Rejecting component interactions from non-owners
 * **Note:** For the example's sake, I will reply directly on each failed condition,
 * however, I recommend having a separate function/class to handle rejections,
 * as to not duplicate code on each rejection case.
 *
 * ```kt
 * @BService
 * class MyComponentFilter(
 *     private val botOwners: BotOwners,
 * ) : ComponentInteractionFilter {
 *
 *     override val global: Boolean get() = true
 *
 *     override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? {
 *         if (event.channel.idLong == 932902082724380744 && event.user !in botOwners) {
 *             event.reply_("Only owners are allowed to use components in <#932902082724380744>", ephemeral = true).await()
 *             return "Not an owner"
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
 * public class MyComponentFilter implements ComponentInteractionFilter {
 *
 *     private final BotOwners botOwners;
 *
 *     public MyComponentFilter(BotOwners botOwners) {
 *         this.botOwners = botOwners;
 *     }
 *
 *     @Override
 *     public boolean getGlobal() {
 *         return true;
 *     }
 *
 *     @Nullable
 *     @Override
 *     public String check(@NotNull GenericComponentInteractionCreateEvent event, @Nullable String handlerName) {
 *         if (event.getChannel().getIdLong() == 932902082724380744L && !botOwners.isOwner(event.getUser())) {
 *             event.reply("Only owners are allowed to use components in <#932902082724380744>").setEphemeral(true).queue();
 *             return "Not an owner";
 *         }
 *         return null;
 *     }
 * }
 * ```
 * 
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface ComponentInteractionFilter : Filter {
    /**
     * Checks if this component can be used, returns `null` if this filter passes,
     * or a reason for the rejection, used for logging purposes.
     *
     * @param handlerName The persistent handler name, as declared in [JDAButtonListener]/[JDASelectMenuListener],
     *                    might be null if there is no handler defined, or is ephemeral.
     */
    @JvmSynthetic
    suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? =
        check(event, handlerName)

    /**
     * Checks if this component can be used, returns `null` if this filter passes,
     * or a reason for the rejection, used for logging purposes.
     *
     * @param handlerName The persistent handler name, as declared in [JDAButtonListener]/[JDASelectMenuListener],
     *                    might be null if there is no handler defined, or is ephemeral.
     */
    fun check(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'isAccepted' or 'isAcceptedSuspend' method")

    companion object {
        @JvmStatic
        @JvmName("or")
        fun orJava(left: ComponentInteractionFilter, right: ComponentInteractionFilter): ComponentInteractionFilter {
            return left or right
        }

        @JvmStatic
        @JvmName("and")
        fun andJava(left: ComponentInteractionFilter, right: ComponentInteractionFilter): ComponentInteractionFilter {
            return left and right
        }
    }
}

infix fun ComponentInteractionFilter.or(other: ComponentInteractionFilter): ComponentInteractionFilter {
    return object : ComponentInteractionFilter {
        override val global: Boolean = false

        override val description: String
            get() = "(${this@or.description} || ${other.description})"

        override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? {
            // Elvis operator short circuits if left condition had no error
            this@or.checkSuspend(event, handlerName) ?: return null
            return other.checkSuspend(event, handlerName)
        }
    }
}

infix fun ComponentInteractionFilter.and(other: ComponentInteractionFilter): ComponentInteractionFilter {
    return object : ComponentInteractionFilter {
        override val global: Boolean = false

        override val description: String
            get() = "(${this@and.description} && ${other.description})"

        override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? {
            val errorObject = this@and.checkSuspend(event, handlerName)
            if (errorObject != null)
                return errorObject
            return other.checkSuspend(event, handlerName)
        }
    }
}