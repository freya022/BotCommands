package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.core.api.annotations.BService;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

@BService
public class TextChannelResolver extends AbstractChannelResolver<TextChannel> {
	public TextChannelResolver() {
		super(TextChannel.class, ChannelType.TEXT, Guild::getTextChannelById);
	}
}
