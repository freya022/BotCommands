package io.github.freya022.botcommands.test.commands.user

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL
import net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.InteractionContextType.*

@Command
class UserContextTest : ApplicationCommand(), GlobalApplicationCommandProvider {
    @JDAUserCommand(
        name = "test user (annotated)",
        contexts = [GUILD, BOT_DM, PRIVATE_CHANNEL],
        integrationTypes = [GUILD_INSTALL, USER_INSTALL]
    )
    suspend fun onUserContextTest(event: GlobalUserEvent) {
        event.reply_("Reply", ephemeral = true).await()

        println()

        event.hook.editOriginal("test")
            .flatMap(Message::delete)
            .await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.userCommand("test user", ::onUserContextTest) {
            contexts = InteractionContextType.ALL
            integrationTypes = IntegrationType.ALL
        }
    }
}