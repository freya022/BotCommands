package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

/**
 * Processes component interaction rejections returned by [component interaction filters][ComponentInteractionFilter].
 *
 * ### Requirements
 * - Register your instance as a service with [@BService][BService].
 * - Implement either [handle] (Java) or [handleSuspend] (Kotlin).
 * - Acknowledge the interaction when it is rejected.
 *
 * ### Example - Replying the error string returned by the [ComponentInteractionFilter] example
 * ```kt
 * @BService
 * class MyComponentRejectionHandler : ComponentInteractionRejectionHandler<String> {
 *     override suspend fun handleSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?, userData: String) {
 *         event.reply_(userData, ephemeral = true).await()
 *     }
 * }
 * ```
 *
 * <Hr>
 *
 * ```java
 * @BService
 * public class MyComponentRejectionHandler implements ComponentInteractionRejectionHandler<String> {
 *     @Override
 *     public void handle(@NotNull GenericComponentInteractionCreateEvent event, @Nullable String handlerName, @NotNull String userData) {
 *         event.reply(userData).setEphemeral(true).queue();
 *     }
 * }
 * ```
 *
 * @param T Type of the error object returned by [ComponentInteractionFilter]
 *
 * @see ComponentInteractionFilter
 */
@InterfacedService(acceptMultiple = false)
interface ComponentInteractionRejectionHandler<T : Any> {
    @JvmSynthetic
    suspend fun handleSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?, userData: T): Unit =
        handle(event, handlerName, userData)

    fun handle(event: GenericComponentInteractionCreateEvent, handlerName: String?, userData: T): Unit =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'handle' or 'handleSuspend' method")
}