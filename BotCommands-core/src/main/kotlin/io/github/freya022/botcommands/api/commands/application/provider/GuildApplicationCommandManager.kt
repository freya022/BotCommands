package io.github.freya022.botcommands.api.commands.application.provider

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.builder.TopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType

class GuildApplicationCommandManager internal constructor(context: BContext, val guild: Guild): AbstractApplicationCommandManager(context) {
    override val defaultScope = CommandScope.GUILD

    override val supportedContexts: Set<InteractionContextType> = setOf(InteractionContextType.GUILD)
    override val supportedIntegrationTypes: Set<IntegrationType> = setOf(IntegrationType.GUILD_INSTALL)
    override val defaultContexts: Set<InteractionContextType> = Defaults.contexts
    override val defaultIntegrationTypes: Set<IntegrationType> = Defaults.integrationTypes

    object Defaults {
        /**
         * Default value of [TopLevelApplicationCommandBuilder.contexts] and annotated application commands.
         *
         * Defaults to [InteractionContextType.GUILD], can be edited.
         */
        @JvmStatic
        var contexts: Set<InteractionContextType> = setOf(InteractionContextType.GUILD)
        /**
         * Default value of [TopLevelApplicationCommandBuilder.integrationTypes] and annotated application commands.
         *
         * Defaults to [IntegrationType.GUILD_INSTALL], can be edited.
         */
        @JvmStatic
        var integrationTypes: Set<IntegrationType> = setOf(IntegrationType.GUILD_INSTALL)
    }
}
