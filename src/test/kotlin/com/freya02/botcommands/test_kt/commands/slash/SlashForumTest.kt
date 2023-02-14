package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.ChannelTypes
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.utils.EmojiUtils
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.utils.messages.MessageCreateData

@CommandMarker
class SlashForumTest : ApplicationCommand() {
    @JDASlashCommand(name = "forum_test")
    suspend fun execute(event: GuildSlashEvent, @AppOption @ChannelTypes(ChannelType.FORUM) channel: GuildChannel) {
        event.reply_("OK", ephemeral = true).queue()

        channel as ForumChannel

        channel.createForumPost("Test post", MessageCreateData.fromContent("Content"))
            .setTags(channel.availableTags[0])
            .await()
            .message
            .addReaction(EmojiUtils.resolveJDAEmoji("white_check_mark"))
            .queue()
    }
}