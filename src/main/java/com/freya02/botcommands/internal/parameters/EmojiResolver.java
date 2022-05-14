package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.entities.Emoji;
import com.freya02.botcommands.api.parameters.*;
import com.freya02.botcommands.api.utils.EmojiUtils;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import com.freya02.botcommands.internal.entities.EmojiImpl;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class EmojiResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public EmojiResolver() {
		super(ParameterType.ofClass(Emoji.class));
	}

	@Override
	@Nullable
	public Object resolve(@NotNull BContext context, @NotNull TextCommandInfo info, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
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
	@NotNull
	public OptionType getOptionType() {
		return OptionType.STRING;
	}

	@Override
	@Nullable
	public Object resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return getEmoji(optionMapping.getAsString());
	}

	@Override
	@Nullable
	public Object resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		return getEmoji(arg);
	}

	@NotNull
	private EmojiImpl getEmoji(String arg) {
		return new EmojiImpl(EmojiUtils.resolveEmojis(arg));
	}
}
