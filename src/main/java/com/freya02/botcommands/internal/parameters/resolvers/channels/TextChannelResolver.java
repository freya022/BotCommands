package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.api.annotations.Resolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Resolver
public class TextChannelResolver extends AbstractChannelResolver<TextChannel> {
	public TextChannelResolver() {
		super(TextChannel.class, ChannelType.TEXT, Guild::getTextChannelById);
	}
}
