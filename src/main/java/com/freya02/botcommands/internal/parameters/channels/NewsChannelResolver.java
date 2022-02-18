package com.freya02.botcommands.internal.parameters.channels;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.NewsChannel;

public class NewsChannelResolver extends AbstractChannelResolver<NewsChannel> {
	public NewsChannelResolver() {
		super(NewsChannel.class, ChannelType.NEWS, Guild::getNewsChannelById);
	}
}
