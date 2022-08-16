package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.core.api.annotations.BService;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;

@BService
public class VoiceChannelResolver extends AbstractChannelResolver<VoiceChannel> {
	public VoiceChannelResolver() {
		super(VoiceChannel.class, ChannelType.VOICE, Guild::getVoiceChannelById);
	}
}
