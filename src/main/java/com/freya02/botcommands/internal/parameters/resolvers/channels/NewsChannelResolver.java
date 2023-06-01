package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.api.core.service.annotations.Resolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;

@Resolver
public class NewsChannelResolver extends AbstractChannelResolver<NewsChannel> {
	public NewsChannelResolver() {
		super(NewsChannel.class, ChannelType.NEWS, Guild::getNewsChannelById);
	}
}
