package com.freya02.botcommands.internal.parameters.channels;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class VoiceChannelResolver extends AbstractChannelResolver<VoiceChannel> {
	public VoiceChannelResolver() {
		super(VoiceChannel.class, ChannelType.VOICE, Guild::getVoiceChannelById);
	}
}
