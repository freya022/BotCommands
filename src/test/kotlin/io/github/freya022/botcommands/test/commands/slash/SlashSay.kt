package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.core.utils.delay
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import kotlin.time.Duration.Companion.seconds

@Command
class SlashSay : ApplicationCommand() {
    @JDASlashCommand(name = "say", description = "Sends a message in a channel")
    suspend fun onSlashSay(
        event: GuildSlashEvent,
        @SlashOption(description = "Channel to send the message in") channel: TextChannel,
        @SlashOption(description = "What to say") content: String
    ) {
        event.reply_("Done!", ephemeral = true)
            .delay(5.seconds)
            .flatMap(InteractionHook::deleteOriginal)
            .queue()
        channel.sendMessage(content).await()
    }
}

@Command
class SlashSayDsl {
    suspend fun onSlashSay(
        event: GuildSlashEvent,
        channel: TextChannel,
        content: String
    ) {
        event.reply_("Done!", ephemeral = true)
            .delay(5.seconds)
            .flatMap(InteractionHook::deleteOriginal)
            .queue()
        channel.sendMessage(content).await()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("say_dsl", function = ::onSlashSay) {
            description = "Sends a message in a channel"

            option("channel") {
                description = "Channel to send the message in"
            }

            option("content") {
                description = "What to say"
            }
        }
    }
}