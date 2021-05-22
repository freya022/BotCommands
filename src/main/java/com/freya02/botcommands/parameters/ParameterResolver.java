package com.freya02.botcommands.parameters;

import com.freya02.botcommands.Emoji;
import com.freya02.botcommands.EmojiOrEmote;
import com.freya02.botcommands.Logging;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ParameterResolver {
	protected static final Logger LOGGER = Logging.getLogger();

	public abstract boolean isRegexCommandSupported();
	public abstract Object resolve(GuildMessageReceivedEvent event, String[] args);

	public abstract boolean isSlashCommandSupported();
	public abstract Object resolve(SlashCommandEvent event, OptionMapping optionData);

	public abstract boolean isButtonSupported();
	public abstract Object resolve(ButtonClickEvent event, String arg);

	@Nullable
	public static ParameterResolver of(Class<?> type) {
		if (Boolean.class.isAssignableFrom(type)) return new BooleanResolver();
		else if (EmojiOrEmote.class.isAssignableFrom(type)) return new EmojiOrEmoteResolver();
		else if (Emoji.class.isAssignableFrom(type)) return new EmojiResolver();
		else if (Emote.class.isAssignableFrom(type)) return new EmoteResolver();
		else if (Long.class.isAssignableFrom(type)) return new LongResolver();
		else if (Member.class.isAssignableFrom(type)) return new MemberResolver();
		else if (Role.class.isAssignableFrom(type)) return new RoleResolver();
		else if (String.class.isAssignableFrom(type)) return new StringResolver();
		else if (TextChannel.class.isAssignableFrom(type)) return new TextChannelResolver();
		else if (User.class.isAssignableFrom(type)) return new UserResolver();
		else if (Double.class.isAssignableFrom(type)) return new DoubleResolver();

		return null;
	}
}
