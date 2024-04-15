package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.core.utils.deleteDelayed
import io.github.freya022.botcommands.api.utils.EmojiUtils
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import kotlin.time.Duration.Companion.seconds

@Command
@Dependencies(Components::class) // Disables the command if components are not enabled
@RequiresComponents
class SlashSay(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "say", description = "Sends a message in a channel")
    suspend fun onSlashSay(
        event: GuildSlashEvent,
        @SlashOption(description = "Channel to send the message in") channel: TextChannel,
        @SlashOption(description = "What to say") content: String
    ) {
        event.reply_("Done!", ephemeral = true)
            .deleteDelayed(5.seconds)
            .queue()
        channel.sendMessage(content)
            .addActionRow(buttons.danger(EmojiUtils.resolveJDAEmoji("wastebasket")).ephemeral {
                bindTo { buttonEvent ->
                    buttonEvent.deferEdit().queue()
                    buttonEvent.hook.deleteOriginal().await()
                }
            })
            .await()
    }
}

@Command
@Dependencies(Components::class) // Disables the command if components are not enabled
@RequiresComponents
class SlashSayDsl(private val buttons: Buttons) : GlobalApplicationCommandProvider {
    suspend fun onSlashSay(event: GuildSlashEvent, channel: TextChannel, content: String) {
        event.reply_("Done!", ephemeral = true)
            .deleteDelayed(5.seconds)
            .queue()
        channel.sendMessage(content)
            .addActionRow(buttons.danger(EmojiUtils.resolveJDAEmoji("wastebasket")).ephemeral {
                bindTo { buttonEvent ->
                    buttonEvent.deferEdit().queue()
                    buttonEvent.hook.deleteOriginal().await()
                }
            })
            .await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
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