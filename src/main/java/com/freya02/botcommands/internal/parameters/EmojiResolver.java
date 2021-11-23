package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.entities.Emoji;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.api.utils.EmojiUtils;
import com.freya02.botcommands.internal.entities.EmojiImpl;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.interactions.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class EmojiResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public EmojiResolver() {
		super(Emoji.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return getEmoji(args[0]);
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return Pattern.compile("(\\S+)"); //Non whitespace
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "1️\u0031\u20E3";
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandInteraction event, OptionMapping optionMapping) {
		return getEmoji(optionMapping.getAsString());
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		return getEmoji(arg);
	}

	@NotNull
	private EmojiImpl getEmoji(String arg) {
		return new EmojiImpl(EmojiUtils.resolveEmojis(arg));
	}
}
