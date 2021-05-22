package com.freya02.botcommands;

import gnu.trove.map.hash.TLongLongHashMap;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public abstract class Cooldownable {
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

	/**
	 * @return non-zero if there is a cooldown running
	 */
	public int applyCooldown(GuildMessageReceivedEvent event) {
		int diff;
		switch (cooldownScope) {
			case USER:
				diff = (int) (userCooldowns.get(event.getAuthor().getIdLong()) - System.currentTimeMillis());
				if (diff > 0) return diff;

				userCooldowns.put(event.getAuthor().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
			case GUILD:
				diff = (int) (guildCooldowns.get(event.getGuild().getIdLong()) - System.currentTimeMillis());
				if (diff > 0) return diff;

				guildCooldowns.put(event.getGuild().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
			case CHANNEL:
				diff = (int) (channelCooldowns.get(event.getChannel().getIdLong()) - System.currentTimeMillis());
				if (diff > 0) return diff;

				channelCooldowns.put(event.getChannel().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
		}

		return 0;
	}

	/**
	 * @return non-zero if there is a cooldown running
	 */
	public int applyCooldown(SlashCommandEvent event) {
		int diff;
		switch (cooldownScope) {
			case USER:
				diff = (int) (userCooldowns.get(event.getUser().getIdLong()) - System.currentTimeMillis());
				if (diff > 0) return diff;

				userCooldowns.put(event.getUser().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
			case GUILD:
				if (event.getGuild() == null) break;

				diff = (int) (guildCooldowns.get(event.getGuild().getIdLong()) - System.currentTimeMillis());
				if (diff > 0) return diff;

				guildCooldowns.put(event.getGuild().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
			case CHANNEL:
				diff = (int) (channelCooldowns.get(event.getChannel().getIdLong()) - System.currentTimeMillis());
				if (diff > 0) return diff;

				channelCooldowns.put(event.getChannel().getIdLong(), System.currentTimeMillis() + cooldown);
				break;
		}

		return 0;
	}
}
