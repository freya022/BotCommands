@file:Suppress("DEPRECATION")

package dev.freya02.botcommands.jda.ktx

import net.dv8tion.jda.api.entities.Mentions
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import org.apache.commons.collections4.Bag
import org.apache.commons.collections4.MultiSet

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
@Deprecated("Use getChannelsMultiSet() instead.", ReplaceWith("getChannelsMultiSet<T>()"))
inline fun <reified T : GuildChannel> Mentions.getChannelsBag(): Bag<T> {
    return getChannelsBag(T::class.java)
}

/**
 * Same as [Mentions.getChannelsMultiSet] but with a reified type parameter.
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified T : GuildChannel> Mentions.getChannelsMultiSet(): MultiSet<T> {
    return getChannelsMultiSet(T::class.java)
}
