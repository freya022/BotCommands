package com.freya02.botcommands.api.commands;

import com.freya02.botcommands.api.commands.annotations.Cooldown;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

/**
 * Provides cooldown scopes for executed commands, as specified with {@link Cooldown}.
 */
public enum CooldownScope {
    /** Enables cooldown for the user who called the command */
    USER,
    /** Enables cooldown for the {@linkplain Guild guild} the command got called in */
    GUILD,
    /** Enables cooldown for the {@linkplain GuildMessageChannel} the command got called in */
    CHANNEL
}
