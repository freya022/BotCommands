package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import kotlin.reflect.KParameter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public abstract class AbstractChannelResolver<T extends GuildChannel>
		extends ParameterResolver<AbstractChannelResolver<T>, T>
		implements RegexParameterResolver<AbstractChannelResolver<T>, T>,
		           SlashParameterResolver<AbstractChannelResolver<T>, T>,
		           ComponentParameterResolver<AbstractChannelResolver<T>, T>,
		           ChannelResolver {
	private static final Pattern PATTERN = Pattern.compile("(?:<#)?(\\d+)>?");

	private final Class<T> channelClass;
	private final EnumSet<ChannelType> channelTypes;
	private final BiFunction<Guild, String, T> channelResolver;

	public AbstractChannelResolver(Class<T> channelClass, @Nullable ChannelType channelType, BiFunction<Guild, String, T> channelResolver) {
		super(channelClass);
		this.channelClass = channelClass;

		this.channelTypes = channelType == null ? EnumSet.noneOf(ChannelType.class) : EnumSet.of(channelType);
		this.channelResolver = channelResolver;
	}

	@Override
	@NotNull
	public EnumSet<ChannelType> getChannelTypes() {
		return channelTypes;
	}

	@Override
	@Nullable
	public T resolve(@NotNull BContext context, @NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
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

	@NotNull
	@Override
	public String getHelpExample(@NotNull KParameter parameter, boolean isID) {
		return "channel-id/mention";
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.CHANNEL;
	}

	@Override
	@Nullable
	public T resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		final GuildChannelUnion channel = optionMapping.getAsChannel();
		if (channelClass.isInstance(channel))
			return channelClass.cast(channel);

		return null;
	}

	@Override
	@Nullable
	public T resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		Objects.requireNonNull(event.getGuild(), "Can't get a guild from DMs");

		return channelResolver.apply(event.getGuild(), arg);
	}
}
