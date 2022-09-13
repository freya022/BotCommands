package com.freya02.botcommands.internal.parameters.channels;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class GuildChannelResolver extends AbstractChannelResolver<GuildChannel> {
	public GuildChannelResolver() {
		super(GuildChannel.class, null, Guild::getGuildChannelById);
	}
}
