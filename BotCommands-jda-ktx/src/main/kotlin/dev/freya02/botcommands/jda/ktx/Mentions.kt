package dev.freya02.botcommands.jda.ktx

import net.dv8tion.jda.api.entities.Mentions
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import org.apache.commons.collections4.Bag

/**
 * Same as [Mentions.getChannels] but with a reified type parameter.
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified T : GuildChannel> Mentions.getChannels(): List<T> {
    return getChannels(T::class.java)
}

/**
 * Same as [Mentions.getChannelsBag] but with a reified type parameter.
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified T : GuildChannel> Mentions.getChannelsBag(): Bag<T> {
    return getChannelsBag(T::class.java)
}
