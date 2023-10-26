package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Processes text command rejections returned by [text command filters][TextCommandFilter].
 *
 * ### Usage
 * - Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 * - Implement either [handle] (Java) or [handleSuspend] (Kotlin).
 *
 * ### Example - Replying the error string returned by the [TextCommandFilter] example
 * ```kt
 * @BService
 * class MyTextCommandRejectionHandler : TextCommandRejectionHandler<String>, ApplicationCommandRejectionHandler<String> {
 *     override suspend fun handleSuspend(event: MessageReceivedEvent, variation: TextCommandVariation, args: String, userData: String) {
 *         event.message.reply(userData).await()
 *     }
 * }
 * ```
 *
 * <Hr>
 *
 * ```java
 * @BService
 * public class MyTextCommandRejectionHandler implements TextCommandRejectionHandler<String> {
 *     @Override
 *     public void handle(@NotNull MessageReceivedEvent event, @NotNull TextCommandVariation variation, @NotNull String args, @NotNull String userData) {
 *         event.getMessage().reply(userData).queue();
 *     }
 * }
 * ```
 *
 * @param T Type of the error object returned by [TextCommandFilter]
 *
 * @see TextCommandFilter
 */
@InterfacedService(acceptMultiple = false)
interface TextCommandRejectionHandler<T : Any> {
    @JvmSynthetic
    suspend fun handleSuspend(event: MessageReceivedEvent, variation: TextCommandVariation, args: String, userData: T): Unit =
        handle(event, variation, args, userData)

    fun handle(event: MessageReceivedEvent, variation: TextCommandVariation, args: String, userData: T): Unit =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'handle' or 'handleSuspend' method")
}