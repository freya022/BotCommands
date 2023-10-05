package io.github.freya022.botcommands.internal.parameters.resolvers.channels;

import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;

@Resolver
public class StageChannelResolver extends AbstractChannelResolver<StageChannel> {
	public StageChannelResolver() {
		super(StageChannel.class, ChannelType.STAGE, Guild::getStageChannelById);
	}
}
