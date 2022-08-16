package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.internal.annotations.IncludeClasspath;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.StageChannel;

@IncludeClasspath
public class StageChannelResolver extends AbstractChannelResolver<StageChannel> {
	public StageChannelResolver() {
		super(StageChannel.class, ChannelType.STAGE, Guild::getStageChannelById);
	}
}
