package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.annotations.IncludeClasspath;
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

@IncludeClasspath
public class ThreadChannelResolver
		extends ParameterResolver<ThreadChannelResolver, ThreadChannel>
		implements SlashParameterResolver<ThreadChannelResolver, ThreadChannel>,
		           ChannelResolver {
	private static final EnumSet<ChannelType> THREAD_TYPES = EnumSet.of(ChannelType.GUILD_NEWS_THREAD, ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_PRIVATE_THREAD);

	public ThreadChannelResolver() {
		super(ThreadChannel.class);
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.CHANNEL;
	}

	@Override
	@Nullable
	public ThreadChannel resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		final GuildChannelUnion channel = optionMapping.getAsChannel();
		if (channel.getType().isThread()) {
			return channel.asThreadChannel();
		}

		return null;
	}

	@Override
	@NotNull
	public EnumSet<ChannelType> getChannelTypes() {
		return THREAD_TYPES;
	}
}
