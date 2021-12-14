package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.CooldownScope;
import com.freya02.botcommands.api.Logging;
import gnu.trove.TCollections;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import org.slf4j.Logger;

import java.util.function.Supplier;

import static gnu.trove.impl.Constants.DEFAULT_CAPACITY;
import static gnu.trove.impl.Constants.DEFAULT_LOAD_FACTOR;

public abstract class Cooldownable {
	private record UserGuild(long guildId, long userId) {}

	private static final Logger LOGGER = Logging.getLogger();
	private final CooldownStrategy cooldownStrategy;

	//Trove maps are not *always* the fastest, by a small margin, but are by far the most memory efficient
	//The values are the time on which the cooldown expires
	private final TObjectLongMap<UserGuild> userCooldowns = TCollections.synchronizedMap(new TObjectLongHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0));
	private final TLongLongMap channelCooldowns = TCollections.synchronizedMap(new TLongLongHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0, 0));
	private final TLongLongMap guildCooldowns = TCollections.synchronizedMap(new TLongLongHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0, 0));

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
			case USER -> userCooldowns.put(getUGKey(event), System.currentTimeMillis() + getCooldownMillis());
			case GUILD -> guildCooldowns.put(event.getGuild().getIdLong(), System.currentTimeMillis() + getCooldownMillis());
			case CHANNEL -> channelCooldowns.put(event.getChannel().getIdLong(), System.currentTimeMillis() + getCooldownMillis());
		}
	}

	private UserGuild getUGKey(GuildMessageReceivedEvent event) {
		return new UserGuild(event.getGuild().getIdLong(), event.getAuthor().getIdLong());
	}

	private UserGuild getUGKey(Interaction event, Guild guild) {
		return new UserGuild(guild.getIdLong(), event.getUser().getIdLong());
	}

	public void applyCooldown(Interaction event) {
		switch (getCooldownScope()) {
			case USER -> {
				if (event.getGuild() == null) break;
				userCooldowns.put(getUGKey(event, event.getGuild()), System.currentTimeMillis() + getCooldownMillis());
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
			case USER -> Math.max(0, userCooldowns.get(getUGKey(event)) - System.currentTimeMillis());
			case GUILD -> Math.max(0, guildCooldowns.get(event.getGuild().getIdLong()) - System.currentTimeMillis());
			case CHANNEL -> Math.max(0, channelCooldowns.get(event.getChannel().getIdLong()) - System.currentTimeMillis());
		};
	}

	public long getCooldown(Interaction event, Supplier<String> cmdNameSupplier) {
		switch (getCooldownScope()) {
			case USER -> {
				if (event.getGuild() == null) {
					LOGGER.warn("Interaction command '{}' wasn't used in a guild but uses a guild-wide cooldown", cmdNameSupplier.get());

					return 0;
				}

				return Math.max(0, userCooldowns.get(getUGKey(event, event.getGuild())) - System.currentTimeMillis());
			}
			case GUILD -> {
				if (event.getGuild() == null) {
					LOGGER.warn("Interaction command '{}' wasn't used in a guild but uses a guild-wide cooldown", cmdNameSupplier.get());

					return 0;
				}

				return Math.max(0, guildCooldowns.get(event.getGuild().getIdLong()) - System.currentTimeMillis());
			}
			case CHANNEL -> {
				if (event.getChannel() == null) {
					LOGGER.warn("Interaction command '{}' wasn't used in a channel, somehow", cmdNameSupplier.get());

					return 0;
				}

				return Math.max(0, channelCooldowns.get(event.getChannel().getIdLong()) - System.currentTimeMillis());
			}
			default -> throw new IllegalStateException("Unexpected value: " + getCooldownScope());
		}
	}
}
