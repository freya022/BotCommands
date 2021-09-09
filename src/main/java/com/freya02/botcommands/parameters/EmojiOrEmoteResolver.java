package com.freya02.botcommands.parameters;

import com.freya02.botcommands.entities.EmojiOrEmote;
import com.freya02.botcommands.entities.impl.EmojiOrEmoteImpl;
import com.freya02.botcommands.utils.EmojiUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;

public class EmojiOrEmoteResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public EmojiOrEmoteResolver() {
		super(EmojiOrEmote.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return getEmojiOrEmote(args[0]);
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandEvent event, OptionMapping optionMapping) {
		final Matcher emoteMatcher = Message.MentionType.EMOTE.getPattern().matcher(optionMapping.getAsString());
		if (emoteMatcher.find()) {
			return new EmojiOrEmoteImpl(emoteMatcher.group(1), emoteMatcher.group(2));
		} else {
			return EmojiUtils.resolveEmojis(optionMapping.getAsString());
		}
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
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
