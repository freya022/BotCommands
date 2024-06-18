package io.github.freya022.botcommands.api.commands.application;

import io.github.freya022.botcommands.api.commands.CommandPath;
import io.github.freya022.botcommands.api.commands.application.annotations.Test;
import io.github.freya022.botcommands.api.core.SettingsProvider;
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Defines command scopes for application commands.
 */
public enum CommandScope {
    /**
     * The guild command scope, only pushes application commands to the guilds
     * <br>Can be filtered with {@link ApplicationCommand#getGuildsForCommandId(String, CommandPath)} and {@link SettingsProvider#getGuildCommands(Guild)}
     * <br>Can be forced with {@link BApplicationConfigBuilder#forceGuildCommands(boolean)} and {@link Test @Test}
     */
    GUILD(false, true),
    /**
     * The global command scope, pushes this command to the first shard
     *
     * <p>Cannot be filtered on a per-guild basis
     */
    GLOBAL(true, false),
    /**
     * The global command scope, but with DMs disabled, pushes this command to the first shard
     * <br>This might be useful to have guild commands but without having to push them on every guild
     *
     * <p>Cannot be filtered on a per-guild basis
     */
    GLOBAL_NO_DM(true, true);

    private final boolean isGlobal;
    private final boolean guildOnly;

    CommandScope(boolean isGlobal, boolean guildOnly) {
        this.isGlobal = isGlobal;
        this.guildOnly = guildOnly;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public boolean isGuildOnly() {
        return guildOnly;
    }
}
