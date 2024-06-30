package io.github.freya022.botcommands.test.commands.slash.userapps

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType

// Uses breakpoints to see stuff about the interaction
@Command
class SlashIntegrationContexts : GlobalApplicationCommandProvider, GuildApplicationCommandProvider {
    suspend fun onIntegrationContextsTestGuildCommand(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).await()
    }

    suspend fun onIntegrationContextsTestGlobalGuildCommand(event: GlobalSlashEvent) {
        event.reply_("ok", ephemeral = true).await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("global_integration_contexts", ::onIntegrationContextsTestGlobalGuildCommand) {
            contexts = enumSetOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM)
            integrationTypes = enumSetOf(IntegrationType.GUILD_INSTALL)
        }
    }

    override fun declareGuildApplicationCommands(manager: GuildApplicationCommandManager) {
        manager.slashCommand("guild_integration_contexts", ::onIntegrationContextsTestGuildCommand) {

        }
    }
}