package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.MentionsString
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.utils.joinAsList
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.interactions.commands.SlashCommandReference

@Command
class SlashMentionsString : ApplicationCommand() {
    @TopLevelSlashCommandData
    @JDASlashCommand(name = "mentions_string", subcommand = "any")
    suspend fun onSlashMentionsStringAny(
        event: GuildSlashEvent,
        @SlashOption @MentionsString mentions: List<IMentionable>
    ) {
        event.reply("Mentions:\n${mentions.joinAsList(linePrefix = "-")}").await()
    }

    @JDASlashCommand(name = "mentions_string", subcommand = "role")
    suspend fun onSlashMentionsStringRole(
        event: GuildSlashEvent,
        @SlashOption @MentionsString(Message.MentionType.ROLE) mentions: List<IMentionable>
    ) {
        event.reply("Mentions:\n${mentions.joinAsList(linePrefix = "-")}").await()
    }

    @JDASlashCommand(name = "mentions_string", subcommand = "channel")
    suspend fun onSlashMentionsStringChannel(
        event: GuildSlashEvent,
        @SlashOption @MentionsString mentions: List<GuildChannel>
    ) {
        event.reply("Mentions:\n${mentions.joinAsList(linePrefix = "-")}").await()
    }

    @JDASlashCommand(name = "mentions_string", subcommand = "user")
    suspend fun onSlashMentionsStringUser(
        event: GuildSlashEvent,
        @SlashOption @MentionsString mentions: List<User>
    ) {
        event.reply("Mentions:\n${mentions.joinAsList(linePrefix = "-")}").await()
    }

    @JDASlashCommand(name = "mentions_string", subcommand = "member")
    suspend fun onSlashMentionsStringMember(
        event: GuildSlashEvent,
        @SlashOption @MentionsString mentions: List<Member>
    ) {
        event.reply("Mentions:\n${mentions.joinAsList(linePrefix = "-")}").await()
    }

    @JDASlashCommand(name = "mentions_string", subcommand = "input_user")
    suspend fun onSlashMentionsStringInputUser(
        event: GuildSlashEvent,
        @SlashOption @MentionsString mentions: List<InputUser>
    ) {
        event.reply("Mentions:\n${mentions.joinAsList(linePrefix = "-")}").await()
    }

    @JDASlashCommand(name = "mentions_string", subcommand = "emoji")
    suspend fun onSlashMentionsStringEmoji(
        event: GuildSlashEvent,
        @SlashOption @MentionsString mentions: List<CustomEmoji>
    ) {
        event.reply("Mentions:\n${mentions.joinAsList(linePrefix = "-")}").await()
    }

    @JDASlashCommand(name = "mentions_string", subcommand = "commands")
    suspend fun onSlashMentionsStringCommands(
        event: GuildSlashEvent,
        @SlashOption @MentionsString mentions: List<SlashCommandReference>
    ) {
        event.reply("Mentions:\n${mentions.joinAsList(linePrefix = "-")}").await()
    }

//    @JDASlashCommand(name = "mentions_string", subcommand = "commands_wrong_autocomplete")
//    fun wrongAutocomplete(
//        event: GuildSlashEvent,
//        @SlashOption @MentionsString mentions: List<SlashCommandReference>,
//        @SlashOption(autocomplete = "SlashMentionsString: test") test: String
//    ) { }
//
//    @AutocompleteHandler("SlashMentionsString: test")
//    fun onTestAutocomplete(event: CommandAutoCompleteInteractionEvent, @MentionsString mentions: List<SlashCommandReference>): List<String> {
//        return emptyList()
//    }

//    @JDASlashCommand(name = "mentions_string", subcommand = "wrong")
//    fun wrong(
//        event: GuildSlashEvent,
//        @SlashOption @MentionsString(Message.MentionType.CHANNEL) mentions: List<GuildChannel>
//    ) { }

//    @JDASlashCommand(name = "mentions_string", subcommand = "wrong2")
//    fun wrong2(
//        event: GuildSlashEvent,
//        @SlashOption @MentionsString mentions: List<Channel>
//    ) { }

//    @JDASlashCommand(name = "mentions_string", subcommand = "wrong3")
//    fun wrong3(
//        event: GuildSlashEvent,
//        @SlashOption mentions: List<IMentionable>
//    ) { }
}