package io.github.freya022.botcommands.api.commands.application;

import io.github.freya022.botcommands.api.commands.application.annotations.DeclarationFilter;
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder;
import net.dv8tion.jda.api.interactions.InteractionContextType;

/**
 * Defines the scope on which an application command is pushed to.
 */
public enum CommandScope {
    /**
     * The guild command scope, making the application command accessible on a per-guild basis.
     * <br>These commands can only be executed in the guild they are pushed to.
     *
     * <p>Can be filtered with {@link DeclarationFilter @DeclarationFilter}.
     * <br>Can be forced with {@link BApplicationConfigBuilder#forceGuildCommands(boolean)}.
     */
    GUILD,
    /**
     * The global command scope, making the application command be accessible
     * in the {@link InteractionContextType interaction contexts} set on the command.
     *
     * <p>These commands cannot be filtered.
     */
    GLOBAL,
    @Deprecated
    GLOBAL_NO_DM;
}
