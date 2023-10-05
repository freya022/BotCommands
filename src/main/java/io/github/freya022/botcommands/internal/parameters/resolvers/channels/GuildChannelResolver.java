package io.github.freya022.botcommands.internal.parameters.resolvers.channels;

import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

@Resolver
public class GuildChannelResolver extends AbstractChannelResolver<GuildChannel> {
	public GuildChannelResolver() {
		super(GuildChannel.class, null, Guild::getGuildChannelById);
	}
}
