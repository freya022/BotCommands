package com.freya02.botcommands.internal.utils;

import com.freya02.botcommands.api.Logging;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.RawGatewayEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.JDAImpl;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EventUtils {
	private static final Logger LOGGER = Logging.getLogger();
	private static final Set<Class<? extends GenericEvent>> WARNED_EVENT_TYPES = new HashSet<>();

	public static <T extends GenericEvent> void checkEvent(JDA jda, EnumSet<GatewayIntent> jdaIntents, Class<T> eventType) {
		final EnumSet<GatewayIntent> neededIntents = GatewayIntent.fromEvents(eventType);

		if (!jdaIntents.containsAll(neededIntents) && WARNED_EVENT_TYPES.add(eventType)) {
			final ArrayList<GatewayIntent> missingIntents = new ArrayList<>(neededIntents);
			missingIntents.removeAll(jdaIntents);

			LOGGER.warn("""
					Cannot listen to a %s as there are missing intents:
					Enabled intents: %s
					Intents needed: %s
					Missing intents: %s
					If this is intentional, this can be suppressed using EventUtils#suppressMissingIntents
					See %s for more detail""".formatted(eventType.getSimpleName(),
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

	/**
	 * Suppresses warnings related to missing intents, for this event.
	 */
	public static void suppressMissingIntents(Class<? extends GenericEvent> eventType) {
		WARNED_EVENT_TYPES.add(eventType);
	}
}
