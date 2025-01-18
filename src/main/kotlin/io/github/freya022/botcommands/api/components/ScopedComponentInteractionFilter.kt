package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

/**
 * Prevents component execution by returning an error object to the component executor,
 * before which you must acknowledge the interaction.
 *
 * You must add these filters on the components yourself.
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
 *     ): ScopedComponentInteractionFilter<String> = adminFilter and inVoiceChannelFilter
 * }
 * ```
 *
 * ### Requirements
 * - Register your instance as a service with [@BService][BService].
 * - Implement either [check] (Java) or [checkSuspend] (Kotlin).
 *
 * ### Execution order
 * The execution order is determined by the insertion order.
 *
 * ### Example - Rejecting component interactions from non-owners
 * **Note:** I recommend having a separate class to handle rejections,
 * as to not duplicate code on each filter.
 *
 * ```kt
 * @BService
 * class MyComponentFilter(
 *     private val rejectionHandler: MyComponentRejectionHandler,
 *     private val botOwners: BotOwners,
 * ) : ScopedComponentInteractionFilter {
 *
 *     override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? {
 *         if (event.channel.idLong == 932902082724380744 && event.user !in botOwners) {
 *             rejectionHandler.handle(event, "Only owners are allowed to use components in <#932902082724380744>")
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
 * public class MyComponentFilter implements ScopedComponentInteractionFilter {
 *
 *     private final MyComponentRejectionHandler rejectionHandler;
 *     private final BotOwners botOwners;
 *
 *     public MyComponentFilter(MyComponentRejectionHandler rejectionHandler, BotOwners botOwners) {
 *         this.rejectionHandler = rejectionHandler;
 *         this.botOwners = botOwners;
 *     }
 *
 *     @Nullable
 *     @Override
 *     public String check(@NotNull GenericComponentInteractionCreateEvent event, @Nullable String handlerName) {
 *         if (event.getChannel().getIdLong() == 932902082724380744L && !botOwners.isOwner(event.getUser())) {
 *             rejectionHandler.handle(event, "Only owners are allowed to use components in <#932902082724380744>");
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
interface ScopedComponentInteractionFilter : ComponentInteractionFilter {

    companion object {
        @JvmStatic
        @JvmName("or")
        fun orJava(left: ScopedComponentInteractionFilter, right: ScopedComponentInteractionFilter): ScopedComponentInteractionFilter {
            return left or right
        }

        @JvmStatic
        @JvmName("and")
        fun andJava(left: ScopedComponentInteractionFilter, right: ScopedComponentInteractionFilter): ScopedComponentInteractionFilter {
            return left and right
        }
    }
}

infix fun ScopedComponentInteractionFilter.or(other: ScopedComponentInteractionFilter): ScopedComponentInteractionFilter {
    return object : ScopedComponentInteractionFilter {
        override val description: String
            get() = "(${this@or.description} || ${other.description})"

        override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? {
            // Elvis operator short circuits if left condition had no error
            this@or.checkSuspend(event, handlerName) ?: return null
            return other.checkSuspend(event, handlerName)
        }
    }
}

infix fun ScopedComponentInteractionFilter.and(other: ScopedComponentInteractionFilter): ScopedComponentInteractionFilter {
    return object : ScopedComponentInteractionFilter {
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