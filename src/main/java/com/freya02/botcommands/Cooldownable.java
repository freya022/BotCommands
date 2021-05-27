package com.freya02.botcommands;

import com.freya02.botcommands.slash.SlashCommandListener;
import gnu.trove.map.hash.TLongLongHashMap;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;

public abstract class Cooldownable {
	private static final Logger LOGGER = Logging.getLogger();
	private final CooldownScope cooldownScope;
	private final int cooldown;

	//The values is the time on which the cooldown expires
	private final TLongLongHashMap userCooldowns = new TLongLongHashMap();
	private final TLongLongHashMap channelCooldowns = new TLongLongHashMap();
	private final TLongLongHashMap guildCooldowns = new TLongLongHashMap();

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
				userCooldowns.put(event.getAuthor().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
			case GUILD:
				guildCooldowns.put(event.getGuild().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
			case CHANNEL:
				channelCooldowns.put(event.getChannel().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
		}
	}

	public void applyCooldown(SlashCommandEvent event) {
		switch (cooldownScope) {
			case USER:
				userCooldowns.put(event.getUser().getIdLong(), System.currentTimeMillis() + cooldown);
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
				return (int) (userCooldowns.get(event.getAuthor().getIdLong()) - System.currentTimeMillis());
			case GUILD:
				return (int) (guildCooldowns.get(event.getGuild().getIdLong()) - System.currentTimeMillis());
			case CHANNEL:
				return (int) (channelCooldowns.get(event.getChannel().getIdLong()) - System.currentTimeMillis());
			default:
				throw new IllegalStateException("Unexpected value: " + cooldownScope);
		}
	}

	public int getCooldown(SlashCommandEvent event) {
		switch (cooldownScope) {
			case USER:
				return (int) (userCooldowns.get(event.getUser().getIdLong()) - System.currentTimeMillis());
			case GUILD:
				if (event.getGuild() == null) {
					LOGGER.warn("Slash command '{}' wasn't used in a guild but uses a guild-wide cooldown", SlashCommandListener.reconstructCommand(event));

					return 0;
				}

				return (int) (guildCooldowns.get(event.getGuild().getIdLong()) - System.currentTimeMillis());
			case CHANNEL:
				return (int) (channelCooldowns.get(event.getChannel().getIdLong()) - System.currentTimeMillis());
			default:
				throw new IllegalStateException("Unexpected value: " + cooldownScope);
		}
	}
}
