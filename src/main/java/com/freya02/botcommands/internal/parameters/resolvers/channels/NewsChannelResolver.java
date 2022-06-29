package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.api.parameters.ParameterType;
import com.freya02.botcommands.core.api.annotations.BService;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.NewsChannel;

@BService
public class NewsChannelResolver extends AbstractChannelResolver<NewsChannel> {
	public NewsChannelResolver() {
		super(ParameterType.ofClass(NewsChannel.class), ChannelType.NEWS, Guild::getNewsChannelById);
	}
}
