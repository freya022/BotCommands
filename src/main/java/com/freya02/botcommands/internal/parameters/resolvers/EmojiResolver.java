package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.core.service.annotations.Resolver;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.api.utils.EmojiUtils;
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Resolver
public class EmojiResolver
		extends ParameterResolver<EmojiResolver, Emoji>
		implements RegexParameterResolver<EmojiResolver, Emoji>,
		           SlashParameterResolver<EmojiResolver, Emoji>,
		           ComponentParameterResolver<EmojiResolver, Emoji> {

	public EmojiResolver() {
		super(Emoji.class);
	}

	@Override
	@Nullable
	public Emoji resolve(@NotNull BContext context, @NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
		return getEmoji(args[0]);
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return Pattern.compile("(\\S+)");
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "<:name:1234>";
	}

	@NotNull
	@Override
	public String getHelpExample(boolean isID) {
		return ":joy:";
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.STRING;
	}

	@Override
	@Nullable
	public Emoji resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return getEmoji(optionMapping.getAsString());
	}

	@Override
	@Nullable
	public Emoji resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		return getEmoji(arg);
	}

	@Nullable
	private Emoji getEmoji(String arg) {
		final Matcher emoteMatcher = Message.MentionType.EMOJI.getPattern().matcher(arg);
		if (emoteMatcher.find()) {
			return Emoji.fromCustom(emoteMatcher.group(1), Long.parseUnsignedLong(emoteMatcher.group(2)), arg.startsWith("<a"));
		} else {
			try {
				return EmojiUtils.resolveJDAEmoji(arg);
			} catch (NoSuchElementException e) {
				return null;
			}
		}
	}
}
