package com.freya02.botcommands.internal.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.RawGatewayEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.JDAImpl;

import java.util.ArrayList;
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

	public static <T extends GenericEvent> void checkEvent(JDA jda, EnumSet<GatewayIntent> jdaIntents, Class<T> eventType) {
		final EnumSet<GatewayIntent> neededIntents = GatewayIntent.fromEvents(eventType);

		if (!jdaIntents.containsAll(neededIntents)) {
			final ArrayList<GatewayIntent> missingIntents = new ArrayList<>(jdaIntents);
			missingIntents.removeAll(neededIntents);

			throw new IllegalArgumentException(("""
					Cannot listen to a %s as there are missing intents:
					Enabled intents: %s
					Intents needed: %s
					Missing intents: %s
					See %s for more detail""").formatted(eventType.getSimpleName(),
					jdaIntents.stream().map(GatewayIntent::name).collect(Collectors.joining(", ")),
					neededIntents.stream().map(GatewayIntent::name).collect(Collectors.joining(", ")),
					missingIntents.stream().map(GatewayIntent::name).collect(Collectors.joining(", ")),
					eventType.getSimpleName()));
		}

		if (RawGatewayEvent.class.isAssignableFrom(eventType)) {
			if (!((JDAImpl) jda).isRawEvents()) {
				throw new IllegalArgumentException("Cannot listen to a " + eventType.getSimpleName() + " as JDA is not configured to emit raw gateway events, see JDABuilder#setRawEventsEnabled(boolean)");
			}
		}
	}
}
