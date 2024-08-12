package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.options.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import java.util.*

interface TopLevelApplicationCommandBuilder<T : ApplicationCommandOptionAggregateBuilder<T>> : ApplicationCommandBuilder<T> {
    /**
     * @see TopLevelSlashCommandData.scope
     * @see JDAUserCommand.scope
     * @see JDAMessageCommand.scope
     */
    @Deprecated("Replaced with interaction contexts")
    val scope: CommandScope
        get() = when (EnumSet.copyOf(contexts)) {
            enumSetOf(InteractionContextType.GUILD) -> CommandScope.GUILD
            enumSetOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM) -> CommandScope.GLOBAL
            else -> throw IllegalArgumentException("Cannot map $contexts to a CommandScope")
        }

    /**
     * The interaction contexts in which this command is executable in,
     * think of it as 'Where can I use this command in the Discord client'.
     *
     * **Default:** [GlobalApplicationCommandManager.Defaults.contexts] or [GuildApplicationCommandManager.Defaults.contexts]
     *
     * @see InteractionContextType
     * @see TopLevelSlashCommandData.contexts
     * @see JDAUserCommand.contexts
     * @see JDAMessageCommand.contexts
     */
    var contexts: Set<InteractionContextType>

    /**
     * The integration types in which this command can be installed in.
     *
     * **Default:** [GlobalApplicationCommandManager.Defaults.integrationTypes] or [GuildApplicationCommandManager.Defaults.integrationTypes]
     *
     * @see IntegrationType
     * @see TopLevelSlashCommandData.integrationTypes
     * @see JDAUserCommand.integrationTypes
     * @see JDAMessageCommand.integrationTypes
     */
    var integrationTypes: Set<IntegrationType>

    /**
     * Specifies whether the application command is disabled for everyone but administrators by default,
     * so that administrators can further configure the command.
     *
     * **Note:** You cannot use this with [userPermissions][ApplicationCommandBuilder.userPermissions].
     *
     * For example, if you want a ban command to be usable by someone who has a certain role,
     * but which doesn't have the [BAN_MEMBERS][Permission.BAN_MEMBERS] permission,
     * you would then default lock the command and let the admins of the guild configure it
     *
     * **Default:** false
     *
     * @return `true` if the command should be disabled by default
     *
     * @see TopLevelSlashCommandData.defaultLocked
     * @see JDAUserCommand.defaultLocked
     * @see JDAMessageCommand.defaultLocked
     */
    var isDefaultLocked: Boolean

    /**
     * Specifies whether the application command is usable in NSFW channels.
     *
     * Note: NSFW commands need to be enabled by the user to appear in DMs.
     *
     * See the [Age-Restricted Commands FAQ](https://support.discord.com/hc/en-us/articles/10123937946007) for more details.
     *
     * **Default:** false
     *
     * @return `true` if the command is restricted to NSFW channels
     *
     * @see TopLevelSlashCommandData.nsfw
     * @see JDAUserCommand.nsfw
     * @see JDAMessageCommand.nsfw
     */
    var nsfw: Boolean
}

val TopLevelApplicationCommandBuilder<*>.isGuildOnly: Boolean
    get() = contexts.singleOrNull() == InteractionContextType.GUILD
