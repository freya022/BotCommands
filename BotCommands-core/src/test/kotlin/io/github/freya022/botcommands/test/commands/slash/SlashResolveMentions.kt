package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

@Command
@RequiresComponents
class SlashResolveMentions(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "resolve_mentions")
    suspend fun onSlashResolveMentions(
        event: GuildSlashEvent,
        @SlashOption postContainer: IPostContainer,
        @SlashOption forumChannel: ForumChannel,
        @SlashOption threadChannel: ThreadChannel,
        @SlashOption archivedThreadChannel: ThreadChannel
    ) {
        val button1 = buttons.primary("Resolve given channels").persistent {
            bindWith(::onResolveChannelsClick, postContainer, forumChannel, threadChannel, archivedThreadChannel)
        }
        val button2 = buttons.primary("Resolve mentioned channels").persistent {
            bindWith(::onResolveChannelsClick, postContainer, forumChannel, threadChannel, archivedThreadChannel)
        }
        event.reply_(
            "${forumChannel.asMention} ${threadChannel.asMention} ${archivedThreadChannel.asMention}",
            components = listOf(row(button1, button2)),
            ephemeral = true
        ).await()
    }

    @JDAButtonListener("SlashResolveMentions: ResolveChannelsButton")
    suspend fun onResolveChannelsClick(
        event: ButtonEvent,
        @ComponentData postContainer: IPostContainer,
        @ComponentData forumChannel: ForumChannel,
        @ComponentData threadChannel: ThreadChannel,
        @ComponentData archivedThreadChannel: ThreadChannel
    ) {
        event.reply_(
            """
                $postContainer
                $forumChannel
                $threadChannel
                $archivedThreadChannel
            """.trimIndent(),
            ephemeral = true
        ).await()
    }
}