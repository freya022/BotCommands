package io.github.freya022.botcommands.internal.parameters.resolvers.channels

import net.dv8tion.jda.api.entities.channel.ChannelType

interface IChannelResolver {
    val channelTypes: Set<ChannelType>
}
