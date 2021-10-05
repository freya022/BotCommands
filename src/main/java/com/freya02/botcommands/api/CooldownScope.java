package com.freya02.botcommands.api;

import com.freya02.botcommands.api.annotations.Cooldown;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * <p>Provides cooldown scopes for executed commands</p>
 * <p>The cooldown time is specified with {@link Cooldown}</p>
 */
public enum CooldownScope {
    /** Enables cooldown for the user who called the command */
    USER,
    /** Enables cooldown for the {@linkplain Guild guild} the command got called in */
    GUILD,
    /** Enables cooldown for the {@linkplain TextChannel TextChannel} the command got called in */
    CHANNEL
}
