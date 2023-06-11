package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.api.core.service.annotations.Resolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;

@Resolver
public class StageChannelResolver extends AbstractChannelResolver<StageChannel> {
	public StageChannelResolver() {
		super(StageChannel.class, ChannelType.STAGE, Guild::getStageChannelById);
	}
}
