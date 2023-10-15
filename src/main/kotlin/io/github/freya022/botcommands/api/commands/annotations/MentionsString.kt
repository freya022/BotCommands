package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.prefixed.annotations.TextOption
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji

/**
 * Marks the [@SlashOption][SlashOption] or [@TextOption][TextOption] as a list of [mentionable][IMentionable] retrieved from a string.
 *
 * The target parameter must be of type [List], where the element type is either:
 * - [User]
 * - [Member]
 * - Any subtype of [Channel]
 * - [Role]
 * - [Slash commands][Command]
 * - [Custom emojis][CustomEmoji]
 *
 * If no mention types are set, only mentions corresponding to the list's element type will be parsed.
 *
 * If mention types are set, the list's element type must be [IMentionable].
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MentionsString(
    @get:JvmName("value") vararg val types: MentionType = []
)
