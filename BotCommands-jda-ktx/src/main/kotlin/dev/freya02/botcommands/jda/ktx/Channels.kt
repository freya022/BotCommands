package dev.freya02.botcommands.jda.ktx

import net.dv8tion.jda.api.entities.channel.attribute.IGuildChannelContainer
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel

/**
 * Same as [IGuildChannelContainer.getChannelById] but with a reified type parameter.
 */
inline fun <reified T : GuildChannel> IGuildChannelContainer<in T>.getChannel(id: Long): T? = getChannelById(T::class.java, id)

/**
 * Same as [IGuildChannelContainer.getChannelById] but with a reified type parameter.
 */
inline fun <reified T : GuildChannel> IGuildChannelContainer<in T>.getChannel(id: String): T? = getChannelById(T::class.java, id)

/**
 * Same as [IGuildChannelContainer.getChannelById] but with a reified type parameter.
 */
inline fun <reified T : GuildChannel> IGuildChannelContainer<in T>.getChannel(id: ULong): T? = getChannel<T>(id.toLong())
