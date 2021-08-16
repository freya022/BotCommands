package com.freya02.botcommands;

import com.freya02.botcommands.slash.SlashCommandListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class Cooldownable {
	private static final Logger LOGGER = Logging.getLogger();
	private final CooldownScope cooldownScope;
	private final int cooldown;

	//The values is the time on which the cooldown expires
	private final Map<Long, Map<Long, Long>> userCooldowns = Collections.synchronizedMap(new HashMap<>());
	private final Map<Long, Long> channelCooldowns = Collections.synchronizedMap(new HashMap<>());
	private final Map<Long, Long> guildCooldowns = Collections.synchronizedMap(new HashMap<>());

	protected Cooldownable(CooldownScope cooldownScope, int cooldown) {
		this.cooldownScope = cooldownScope;
		this.cooldown = cooldown;
	}

	public CooldownScope getCooldownScope() {
		return cooldownScope;
	}

	public int getCooldown() {
		return cooldown;
	}

	public void applyCooldown(GuildMessageReceivedEvent event) {
		switch (cooldownScope) {
			case USER:
				getGuildUserCooldownMap(event.getGuild()).put(event.getAuthor().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
			case GUILD:
				guildCooldowns.put(event.getGuild().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
			case CHANNEL:
				channelCooldowns.put(event.getChannel().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
		}
	}

	@Nonnull
	private Map<Long, Long> getGuildUserCooldownMap(Guild guild) {
		return userCooldowns.computeIfAbsent(guild.getIdLong(), x -> Collections.synchronizedMap(new HashMap<>()));
	}

	public void applyCooldown(SlashCommandEvent event) {
		switch (cooldownScope) {
			case USER:
				if (event.getGuild() == null) break;
				
				getGuildUserCooldownMap(event.getGuild()).put(event.getUser().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
			case GUILD:
				if (event.getGuild() == null) break;

				guildCooldowns.put(event.getGuild().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
			case CHANNEL:
				channelCooldowns.put(event.getChannel().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
		}
	}

	public int getCooldown(GuildMessageReceivedEvent event) {
		switch (cooldownScope) {
			case USER:
				return (int) Math.max(0, getGuildUserCooldownMap(event.getGuild()).getOrDefault(event.getAuthor().getIdLong(), 0L) - System.currentTimeMillis());
			case GUILD:
				return (int) Math.max(0, guildCooldowns.getOrDefault(event.getGuild().getIdLong(), 0L) - System.currentTimeMillis());
			case CHANNEL:
				return (int) Math.max(0, channelCooldowns.getOrDefault(event.getChannel().getIdLong(), 0L) - System.currentTimeMillis());
			default:
				throw new IllegalStateException("Unexpected value: " + cooldownScope);
		}
	}

	public int getCooldown(SlashCommandEvent event) {
		switch (cooldownScope) {
			case USER:
				if (event.getGuild() == null) {
					LOGGER.warn("Slash command '{}' wasn't used in a guild but uses a guild-wide cooldown", SlashCommandListener.reconstructCommand(event));

					return 0;
				}
				
				return (int) Math.max(0, getGuildUserCooldownMap(event.getGuild()).getOrDefault(event.getUser().getIdLong(), 0L) - System.currentTimeMillis());
			case GUILD:
				if (event.getGuild() == null) {
					LOGGER.warn("Slash command '{}' wasn't used in a guild but uses a guild-wide cooldown", SlashCommandListener.reconstructCommand(event));

					return 0;
				}

				return (int) Math.max(0, guildCooldowns.getOrDefault(event.getGuild().getIdLong(), 0L) - System.currentTimeMillis());
			case CHANNEL:
				return (int) Math.max(0, channelCooldowns.getOrDefault(event.getChannel().getIdLong(), 0L) - System.currentTimeMillis());
			default:
				throw new IllegalStateException("Unexpected value: " + cooldownScope);
		}
	}
}
