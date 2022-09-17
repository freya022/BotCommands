package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.internal.annotations.IncludeClasspath;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@IncludeClasspath
public class TextChannelResolver extends AbstractChannelResolver<TextChannel> {
	public TextChannelResolver() {
		super(TextChannel.class, ChannelType.TEXT, Guild::getTextChannelById);
	}
}
