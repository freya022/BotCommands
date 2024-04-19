package io.github.freya022.botcommands.test.commands.message

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL
import net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.InteractionContextType.*

@Command
class MessageContextTest : ApplicationCommand(), GlobalApplicationCommandProvider {
    @JDAMessageCommand(
        name = "test message (annotated)",
        contexts = [GUILD, BOT_DM, PRIVATE_CHANNEL],
        integrationTypes = [GUILD_INSTALL, USER_INSTALL]
    )
    suspend fun onMessageContextTest(event: GlobalMessageEvent) {
        event.reply_("Reply", ephemeral = true).await()

        println()

        event.hook.editOriginal("test")
            .flatMap(Message::delete)
            .await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.messageCommand("test message", ::onMessageContextTest) {
            contexts = InteractionContextType.ALL
            integrationTypes = IntegrationType.ALL
        }
    }
}