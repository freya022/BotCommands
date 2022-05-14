package com.freya02.botcommands.internal.parameters.channels;

import com.freya02.botcommands.api.parameters.ParameterType;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class TextChannelResolver extends AbstractChannelResolver<TextChannel> {
	public TextChannelResolver() {
		super(ParameterType.ofClass(TextChannel.class), ChannelType.TEXT, Guild::getTextChannelById);
	}
}
