package com.freya02.botcommands.parameters;

import com.freya02.botcommands.Emoji;
import com.freya02.botcommands.impl.EmojiImpl;
import com.freya02.botcommands.utils.EmojiUtils;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nonnull;

public class EmojiResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public EmojiResolver() {
		super(Emoji.class);
	}

	@Override
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return getEmoji(args[0]);
	}

	@Override
	public Object resolve(SlashCommandEvent event, OptionMapping optionData) {
		return getEmoji(optionData.getAsString());
	}

	@Override
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		return getEmoji(arg);
	}

	@Nonnull
	private EmojiImpl getEmoji(String arg) {
		return new EmojiImpl(EmojiUtils.resolveEmojis(arg));
	}
}
