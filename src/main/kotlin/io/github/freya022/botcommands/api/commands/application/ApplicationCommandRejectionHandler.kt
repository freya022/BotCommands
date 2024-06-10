package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

/**
 * Processes application command rejections returned by [application command filters][ApplicationCommandFilter].
 *
 * ### Requirements
 * - Register your instance as a service with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 * - Implement either [handle] (Java) or [handleSuspend] (Kotlin).
 * - Acknowledge the interaction when it is rejected.
 *
 * ### Example - Replying the error string returned by the [ApplicationCommandFilter] example
 * ```kt
 * @BService
 * class MyApplicationCommandRejectionHandler : ApplicationCommandRejectionHandler<String> {
 *     override suspend fun handleSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo, userData: String) {
 *         event.reply_(userData, ephemeral = true).await()
 *     }
 * }
 * ```
 *
 * <Hr>
 *
 * ```java
 * @BService
 * public class MyApplicationCommandRejectionHandler implements ApplicationCommandRejectionHandler<String> {
 *     @Override
 *     public void handle(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo, @NotNull String userData) {
 *         event.reply(userData).setEphemeral(true).queue();
 *     }
 * }
 * ```
 *
 * @param T Type of the error object returned by [ApplicationCommandFilter]
 *
 * @see ApplicationCommandFilter
 */
@InterfacedService(acceptMultiple = false)
interface ApplicationCommandRejectionHandler<T : Any> {
    @JvmSynthetic
    suspend fun handleSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo, userData: T): Unit =
        handle(event, commandInfo, userData)

    fun handle(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo, userData: T): Unit =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'handle' or 'handleSuspend' method")
}