package com.freya02.botcommands.internal.parameters.channels;

import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ThreadChannelResolver extends ParameterResolver implements SlashParameterResolver, ChannelResolver {
	private static final EnumSet<ChannelType> THREAD_TYPES = EnumSet.of(ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_PRIVATE_THREAD);

	public ThreadChannelResolver() {
		super(ThreadChannel.class);
	}

	@Override
	@Nullable
	public Object resolve(CommandInteractionPayload event, OptionMapping optionMapping) {
		return optionMapping.getAsGuildChannel();
	}

	@Override
	@NotNull
	public EnumSet<ChannelType> getChannelTypes() {
		return THREAD_TYPES;
	}
}
