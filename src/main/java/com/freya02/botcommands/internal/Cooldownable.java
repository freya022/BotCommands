package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.CooldownScope;
import com.freya02.botcommands.api.Logging;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class Cooldownable {
	private static final Logger LOGGER = Logging.getLogger();
	private final CooldownStrategy cooldownStrategy;

	//The values is the time on which the cooldown expires
	private final Map<Long, Map<Long, Long>> userCooldowns = Collections.synchronizedMap(new HashMap<>());
	private final Map<Long, Long> channelCooldowns = Collections.synchronizedMap(new HashMap<>());
	private final Map<Long, Long> guildCooldowns = Collections.synchronizedMap(new HashMap<>());

	protected Cooldownable(CooldownStrategy cooldownStrategy) {
		this.cooldownStrategy = cooldownStrategy;
	}

	public long getCooldownMillis() {
		return cooldownStrategy.getCooldownMillis();
	}

	public CooldownScope getCooldownScope() {
		return cooldownStrategy.getScope();
	}

	public void applyCooldown(GuildMessageReceivedEvent event) {
		switch (getCooldownScope()) {
			case USER -> getGuildUserCooldownMap(event.getGuild()).put(event.getAuthor().getIdLong(), System.currentTimeMillis() + getCooldownMillis());
			case GUILD -> guildCooldowns.put(event.getGuild().getIdLong(), System.currentTimeMillis() + getCooldownMillis());
			case CHANNEL -> channelCooldowns.put(event.getChannel().getIdLong(), System.currentTimeMillis() + getCooldownMillis());
		}
	}

	@NotNull
	private Map<Long, Long> getGuildUserCooldownMap(Guild guild) {
		return userCooldowns.computeIfAbsent(guild.getIdLong(), x -> Collections.synchronizedMap(new HashMap<>()));
	}

	public void applyCooldown(Interaction event) {
		switch (getCooldownScope()) {
			case USER -> {
				if (event.getGuild() == null) break;
				getGuildUserCooldownMap(event.getGuild()).put(event.getUser().getIdLong(), System.currentTimeMillis() + getCooldownMillis());
			}
			case GUILD -> {
				if (event.getGuild() == null) break;
				guildCooldowns.put(event.getGuild().getIdLong(), System.currentTimeMillis() + getCooldownMillis());
			}
			case CHANNEL -> {
				if (event.getChannel() == null) break;
				channelCooldowns.put(event.getChannel().getIdLong(), System.currentTimeMillis() + getCooldownMillis());
			}
		}
	}

	public long getCooldown(GuildMessageReceivedEvent event) {
		return switch (getCooldownScope()) {
			case USER -> Math.max(0, getGuildUserCooldownMap(event.getGuild()).getOrDefault(event.getAuthor().getIdLong(), 0L) - System.currentTimeMillis());
			case GUILD -> Math.max(0, guildCooldowns.getOrDefault(event.getGuild().getIdLong(), 0L) - System.currentTimeMillis());
			case CHANNEL -> Math.max(0, channelCooldowns.getOrDefault(event.getChannel().getIdLong(), 0L) - System.currentTimeMillis());
		};
	}

	public long getCooldown(Interaction event, Supplier<String> cmdNameSupplier) {
		switch (getCooldownScope()) {
			case USER -> {
				if (event.getGuild() == null) {
					LOGGER.warn("Interaction command '{}' wasn't used in a guild but uses a guild-wide cooldown", cmdNameSupplier.get());

					return 0;
				}

				return Math.max(0, getGuildUserCooldownMap(event.getGuild()).getOrDefault(event.getUser().getIdLong(), 0L) - System.currentTimeMillis());
			}
			case GUILD -> {
				if (event.getGuild() == null) {
					LOGGER.warn("Interaction command '{}' wasn't used in a guild but uses a guild-wide cooldown", cmdNameSupplier.get());

					return 0;
				}

				return Math.max(0, guildCooldowns.getOrDefault(event.getGuild().getIdLong(), 0L) - System.currentTimeMillis());
			}
			case CHANNEL -> {
				if (event.getChannel() == null) {
					LOGGER.warn("Interaction command '{}' wasn't used in a channel, somehow", cmdNameSupplier.get());

					return 0;
				}

				return Math.max(0, channelCooldowns.getOrDefault(event.getChannel().getIdLong(), 0L) - System.currentTimeMillis());
			}
			default -> throw new IllegalStateException("Unexpected value: " + getCooldownScope());
		}
	}
}
