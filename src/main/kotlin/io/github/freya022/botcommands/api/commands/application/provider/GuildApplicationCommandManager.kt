package io.github.freya022.botcommands.api.commands.application.provider

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.builder.TopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType

class GuildApplicationCommandManager internal constructor(context: BContext, val guild: Guild): AbstractApplicationCommandManager(context) {
    override val defaultScope = CommandScope.GUILD

    override val supportedContexts: Set<InteractionContextType> = enumSetOf(InteractionContextType.GUILD)
    override val supportedIntegrationTypes: Set<IntegrationType> = enumSetOf(IntegrationType.GUILD_INSTALL)
    override val defaultContexts: Set<InteractionContextType> = Defaults.contexts
    override val defaultIntegrationTypes: Set<IntegrationType> = Defaults.integrationTypes

    object Defaults {
        /**
         * Default value of [TopLevelApplicationCommandBuilder.contexts].
         *
         * Defaults to [InteractionContextType.GUILD], can be edited.
         */
        var contexts: Set<InteractionContextType> = enumSetOf(InteractionContextType.GUILD).unmodifiableView()
        /**
         * Default value of [TopLevelApplicationCommandBuilder.integrationTypes].
         *
         * Defaults to [IntegrationType.GUILD_INSTALL], can be edited.
         */
        var integrationTypes: Set<IntegrationType> = enumSetOf(IntegrationType.GUILD_INSTALL).unmodifiableView()
    }
}