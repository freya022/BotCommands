package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.internal.annotations.IncludeClasspath;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

@IncludeClasspath
public class GuildChannelResolver extends AbstractChannelResolver<GuildChannel> {
	public GuildChannelResolver() {
		super(GuildChannel.class, null, Guild::getGuildChannelById);
	}
}
