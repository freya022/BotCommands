package com.freya02.botcommands.internal.parameters.channels;

import com.freya02.botcommands.api.parameters.ParameterType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;

public class GuildChannelResolver extends AbstractChannelResolver<GuildChannel> {
	public GuildChannelResolver() {
		super(ParameterType.ofClass(GuildChannel.class), null, Guild::getGuildChannelById);
	}
}
