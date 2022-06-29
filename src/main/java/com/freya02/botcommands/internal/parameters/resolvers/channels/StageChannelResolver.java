package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.api.parameters.ParameterType;
import com.freya02.botcommands.core.api.annotations.BService;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.StageChannel;

@BService
public class StageChannelResolver extends AbstractChannelResolver<StageChannel> {
	public StageChannelResolver() {
		super(ParameterType.ofClass(StageChannel.class), ChannelType.STAGE, Guild::getStageChannelById);
	}
}
