package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.core.api.annotations.BService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;

@BService
public class GuildChannelResolver extends AbstractChannelResolver<GuildChannel> {
	public GuildChannelResolver() {
		super(GuildChannel.class, null, Guild::getGuildChannelById);
	}
}
