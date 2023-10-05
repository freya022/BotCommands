package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

/**
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
 * **Example** - Rejecting component interactions from non-owners:
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
 * @see InterfacedService @InterfacedService
 *
 * @see isAccepted
 */
@InterfacedService(acceptMultiple = true)
interface ComponentInteractionFilter {
    /**
     * Returns whether the component interaction should be accepted or not.
     *
     * **Note:** Your filter still has to acknowledge the interaction in case it rejects it.
     *
     * @param handlerName The persistent handler name, as declared in [JDAButtonListener]/[JDASelectMenuListener],
     *                    might be null if there is no handler defined, or is ephemeral.
     *
     * @return `true` if the component interaction can run, `false` otherwise
     *
     * @see ComponentInteractionFilter
     */
    @JvmSynthetic
    suspend fun isAcceptedSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): Boolean =
        isAccepted(event, handlerName)

    /**
     * Returns whether the component interaction should be accepted or not.
     *
     * **Note:** Your filter still has to acknowledge the interaction in case it rejects it.
     *
     * @param handlerName The persistent handler name, as declared in [JDAButtonListener]/[JDASelectMenuListener],
     *                    might be null if there is no handler defined, or is ephemeral.
     *
     * @return `true` if the component interaction can run, `false` otherwise
     *
     * @see ComponentInteractionFilter
     */
    fun isAccepted(event: GenericComponentInteractionCreateEvent, handlerName: String?): Boolean =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'isAccepted' or 'isAcceptedSuspend' method")
}
