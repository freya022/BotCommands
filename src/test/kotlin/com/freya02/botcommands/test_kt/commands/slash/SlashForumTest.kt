package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.utils.EmojiUtils
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.utils.messages.MessageCreateData

@Command
class SlashForumTest : ApplicationCommand() {
    @JDASlashCommand(name = "forum_test")
    suspend fun execute(event: GuildSlashEvent, @AppOption forumChannel: ForumChannel) {
        event.reply_("OK", ephemeral = true).queue()

        forumChannel.createForumPost("Test post", MessageCreateData.fromContent("Content"))
            .setTags(forumChannel.availableTags[0])
            .await()
            .message
            .addReaction(EmojiUtils.resolveJDAEmoji("white_check_mark"))
            .queue()
    }
}