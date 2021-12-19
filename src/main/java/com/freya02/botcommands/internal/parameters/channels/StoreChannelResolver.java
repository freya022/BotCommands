package com.freya02.botcommands.internal.parameters.channels;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.StoreChannel;

public class StoreChannelResolver extends AbstractChannelResolver<StoreChannel> {
	public StoreChannelResolver() {
		super(StoreChannel.class, ChannelType.STORE, Guild::getStoreChannelById);
	}
}
