package com.freya02.botcommands.internal.parameters.channels;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public interface ChannelResolver {
	@NotNull EnumSet<ChannelType> getChannelTypes();
}
