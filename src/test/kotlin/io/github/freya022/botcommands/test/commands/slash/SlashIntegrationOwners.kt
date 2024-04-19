package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.into
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType

@Command
class SlashIntegrationOwners(private val buttons: Buttons) : GlobalApplicationCommandProvider, GuildApplicationCommandProvider {
    suspend fun run(event: GlobalSlashEvent) {
        val button = buttons.primary("click me").ephemeral {
            bindTo { it.deferEdit().await() }
        }
        event.replyComponents(button.into()).setEphemeral(true).await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("global_owners", ::run) {
            integrationTypes = IntegrationType.ALL
            contexts = InteractionContextType.ALL
        }

        manager.slashCommand("global_owners_only_user", ::run) {
            integrationTypes = enumSetOf(IntegrationType.USER_INSTALL)
            contexts = InteractionContextType.ALL
        }

        manager.slashCommand("global_owners_only_guild", ::run) {
            integrationTypes = enumSetOf(IntegrationType.GUILD_INSTALL)
            contexts = InteractionContextType.ALL
        }
    }

    override fun declareGuildApplicationCommands(manager: GuildApplicationCommandManager) {
        manager.slashCommand("guild_owners", ::run) {

        }
    }
}