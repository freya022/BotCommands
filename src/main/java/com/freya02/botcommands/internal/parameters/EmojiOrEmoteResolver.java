package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.entities.EmojiOrEmote;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.api.utils.EmojiUtils;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import com.freya02.botcommands.internal.entities.EmojiOrEmoteImpl;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import net.dv8tion.jda.api.entities.Message;
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

public class EmojiOrEmoteResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public EmojiOrEmoteResolver() {
		super(EmojiOrEmote.class);
	}

	@Override
	@Nullable
	public Object resolve(@NotNull BContext context, @NotNull TextCommandInfo info, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
		return getEmojiOrEmote(args[0]);
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

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.STRING;
	}

	@Override
	@Nullable
	public Object resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		final Matcher emoteMatcher = Message.MentionType.EMOTE.getPattern().matcher(optionMapping.getAsString());
		if (emoteMatcher.find()) {
			return new EmojiOrEmoteImpl(emoteMatcher.group(1), emoteMatcher.group(2));
		} else {
			return EmojiUtils.resolveEmojis(optionMapping.getAsString());
		}
	}

	@Override
	@Nullable
	public Object resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		return getEmojiOrEmote(arg);
	}

	@Nullable
	private Object getEmojiOrEmote(String arg) {
		final Matcher emoteMatcher = Message.MentionType.EMOTE.getPattern().matcher(arg);
		if (emoteMatcher.find()) {
			return new EmojiOrEmoteImpl(emoteMatcher.group(1), emoteMatcher.group(2));
		} else {
			try {
				return new EmojiOrEmoteImpl(EmojiUtils.resolveEmojis(arg));
			} catch (NoSuchElementException e) {
				LOGGER.error("Could not resolve emote: {}", arg);
				return null;
			}
		}
	}
}
