package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.api.annotations.Resolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

@Resolver
public class VoiceChannelResolver extends AbstractChannelResolver<VoiceChannel> {
	public VoiceChannelResolver() {
		super(VoiceChannel.class, ChannelType.VOICE, Guild::getVoiceChannelById);
	}
}
