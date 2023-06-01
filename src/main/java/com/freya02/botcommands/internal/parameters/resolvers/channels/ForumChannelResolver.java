package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.api.core.service.annotations.Resolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;

@Resolver
public class ForumChannelResolver extends AbstractChannelResolver<ForumChannel> {
	public ForumChannelResolver() {
		super(ForumChannel.class, ChannelType.FORUM, Guild::getForumChannelById);
	}
}
