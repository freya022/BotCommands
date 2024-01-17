package io.github.freya022.botcommands.api.commands.application.slash.annotations

import io.github.freya022.botcommands.api.core.entities.InputUser
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.interactions.commands.SlashCommandReference

/**
 * Marks the [@SlashOption][SlashOption] as a list of [mentionable][IMentionable] retrieved from a string.
 *
 * The target parameter must be of type [List], where the element type is either:
 * - [User]
 * - [Member]
 * - [InputUser]
 * - Any subtype of [GuildChannel]
 * - [Role]
 * - [Slash commands][SlashCommandReference]
 * - [Custom emojis][CustomEmoji]
 *
 * If the list's element type is a concrete entity type (such as [Member]), no mention types can be added.
 *
 * If the list's element type is [IMentionable], mention types can be set, if none are, all mentions are passed.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MentionsString(
    @get:JvmName("value") vararg val types: MentionType = []
)
