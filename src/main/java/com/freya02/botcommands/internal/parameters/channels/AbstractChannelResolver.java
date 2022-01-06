package com.freya02.botcommands.internal.parameters.channels;

import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public abstract class AbstractChannelResolver<T extends GuildChannel> extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	private static final Pattern PATTERN = Pattern.compile("(?:<#)?(\\d+)>?");
	private final ChannelType channelType;
	private final BiFunction<Guild, String, T> channelResolver;

	public AbstractChannelResolver(Class<T> channelClass, @Nullable ChannelType channelType, BiFunction<Guild, String, T> channelResolver) {
		super(channelClass);

		this.channelType = channelType;
		this.channelResolver = channelResolver;
	}

	@Nullable
	public ChannelType getChannelType() {
		return channelType;
	}

	@Override
	@Nullable
	public Object resolve(MessageReceivedEvent event, String[] args) {
		return channelResolver.apply(event.getGuild(), args[0]);
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return PATTERN;
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "<#1234>";
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.CHANNEL;
	}

	@Override
	@Nullable
	public Object resolve(CommandInteractionPayload event, OptionMapping optionMapping) {
		return optionMapping.getAsGuildChannel();
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		Objects.requireNonNull(event.getGuild(), "Can't get a guild from DMs");

		return channelResolver.apply(event.getGuild(), arg);
	}
}
