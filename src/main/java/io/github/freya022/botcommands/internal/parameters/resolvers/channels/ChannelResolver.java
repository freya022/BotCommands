package io.github.freya022.botcommands.internal.parameters.resolvers.channels;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public interface ChannelResolver {
	@NotNull EnumSet<ChannelType> getChannelTypes();
}
