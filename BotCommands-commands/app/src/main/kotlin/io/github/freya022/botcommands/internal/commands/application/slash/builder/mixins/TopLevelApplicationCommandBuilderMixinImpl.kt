package io.github.freya022.botcommands.internal.commands.application.slash.builder.mixins

import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType

internal class TopLevelApplicationCommandBuilderMixinImpl internal constructor(
    private val manager: AbstractApplicationCommandManager,
) : TopLevelApplicationCommandBuilderMixin {

    override var contexts: Set<InteractionContextType> = manager.defaultContexts
        set(value) {
            manager.checkContexts(value)
            field = value
        }
    override var integrationTypes: Set<IntegrationType> = manager.defaultIntegrationTypes
        set(value) {
            manager.checkIntegrations(value)
            field = value
        }
    override var isDefaultLocked: Boolean = false
    override var nsfw: Boolean = false
}