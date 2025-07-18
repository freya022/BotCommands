package io.github.freya022.botcommands.test.commands.slash

import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.reply_
import dev.freya02.jda.emojis.unicode.UnicodeEmojis
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.utils.deleteDelayed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import kotlin.time.Duration.Companion.seconds

@Command
@RequiresComponents // (Optional) Disables the command if components are not enabled
class SlashSay(
    private val buttons: Buttons // Factory for buttons
) : ApplicationCommand() {

    // The descriptions can also be moved to localization files, reducing noise
    @JDASlashCommand(name = "say", description = "Sends a message in a channel")
    suspend fun onSlashSay(
        event: GuildSlashEvent,
        @SlashOption(description = "Channel to send the message in") channel: TextChannel,
        @SlashOption(description = "What to say") content: String
    ) {
        val deleteButton = buttons.danger(UnicodeEmojis.WASTEBASKET).ephemeral {
            bindTo { buttonEvent ->
                buttonEvent.deferEdit().queue()
                buttonEvent.hook.deleteOriginal().await()
            }
        }

        event.reply_("Done!", ephemeral = true)
            .deleteDelayed(5.seconds)
            .queue()

        channel.sendMessage(content)
            .addActionRow(deleteButton)
            .await()
    }
}

@Command
@RequiresComponents // (Optional) Disables the command if components are not enabled
class SlashSayDsl(
    private val buttons: Buttons // Factory for buttons
) : GlobalApplicationCommandProvider {

    suspend fun onSlashSay(event: GuildSlashEvent, channel: TextChannel, content: String) {
        val deleteButton = buttons.danger(UnicodeEmojis.WASTEBASKET).ephemeral {
            bindTo { buttonEvent ->
                buttonEvent.deferEdit().queue()
                buttonEvent.hook.deleteOriginal().await()
            }
        }

        event.reply_("Done!", ephemeral = true)
            .deleteDelayed(5.seconds)
            .queue()

        channel.sendMessage(content)
            .addActionRow(deleteButton)
            .await()
    }

    // This is nice if you need to run your own code to declare commands.
    // For example, a loop to create commands based on an enum
    // If you don't need any dynamic stuff, just stick to annotations
    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("say_dsl", function = ::onSlashSay) {
            // The descriptions can also be moved to localization files, reducing noise
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
