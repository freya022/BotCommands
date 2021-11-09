package com.freya02.botcommands.internal.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.RawGatewayEvent;
import net.dv8tion.jda.api.events.emote.GenericEmoteEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.invite.GenericGuildInviteEvent;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.priv.GenericPrivateMessageEvent;
import net.dv8tion.jda.api.events.message.priv.react.GenericPrivateMessageReactionEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.events.user.update.GenericUserPresenceEvent;
import net.dv8tion.jda.api.events.user.update.GenericUserUpdateEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.JDAImpl;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class EventUtils {
	private static <T, U> void checkEvent(EnumSet<GatewayIntent> jdaIntents, Class<T> eventType, Class<U> expectedBase, GatewayIntent... intents) {
		if (expectedBase.isAssignableFrom(eventType)) {
			for (GatewayIntent intent : intents) {
				if (!jdaIntents.contains(intent)) {
					throw new IllegalArgumentException("Cannot listen to a " + eventType.getSimpleName() + " as the intents " + Arrays.stream(intents).map(GatewayIntent::name).collect(Collectors.joining(", ")) + " are disabled, see " + expectedBase.getSimpleName() + ", currently enabled intents are: " + jdaIntents);
				}
			}
		}
	}

	public static <T extends GenericEvent> void checkEvent(JDA jda, EnumSet<GatewayIntent> intents, Class<T> eventType) {
		checkEvent(intents, eventType, UserTypingEvent.class, GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.GUILD_MESSAGE_TYPING);
		checkEvent(intents, eventType, GenericPrivateMessageReactionEvent.class, GatewayIntent.DIRECT_MESSAGE_REACTIONS);
		checkEvent(intents, eventType, GenericPrivateMessageEvent.class, GatewayIntent.DIRECT_MESSAGES);
		checkEvent(intents, eventType, GenericGuildMessageReactionEvent.class, GatewayIntent.GUILD_MESSAGE_REACTIONS);
		checkEvent(intents, eventType, GenericGuildMessageEvent.class, GatewayIntent.GUILD_MESSAGES);
		checkEvent(intents, eventType, GenericUserPresenceEvent.class, GatewayIntent.GUILD_PRESENCES);
		checkEvent(intents, eventType, GenericUserUpdateEvent.class, GatewayIntent.GUILD_MEMBERS);
		checkEvent(intents, eventType, GenericGuildVoiceEvent.class, GatewayIntent.GUILD_VOICE_STATES);
		checkEvent(intents, eventType, GenericGuildInviteEvent.class, GatewayIntent.GUILD_INVITES);
		checkEvent(intents, eventType, GenericEmoteEvent.class, GatewayIntent.GUILD_EMOJIS);
		checkEvent(intents, eventType, GuildBanEvent.class, GatewayIntent.GUILD_BANS);
		checkEvent(intents, eventType, GenericGuildMemberEvent.class, GatewayIntent.GUILD_MEMBERS);

		if (RawGatewayEvent.class.isAssignableFrom(eventType)) {
			if (!((JDAImpl) jda).isRawEvents()) {
				throw new IllegalArgumentException("Cannot listen to a " + eventType.getSimpleName() + " as JDA is not configured to emit raw gateway events, see JDABuilder#setRawEventsEnabled(boolean)");
			}
		}
	}
}
