package io.github.freya022.botcommands.internal.commands.application.resolvers

import net.dv8tion.jda.api.entities.channel.ChannelType

internal interface IChannelResolver {
    val channelTypes: Set<ChannelType>
}
