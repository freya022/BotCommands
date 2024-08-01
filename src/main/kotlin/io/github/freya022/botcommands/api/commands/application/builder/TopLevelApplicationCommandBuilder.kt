package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import net.dv8tion.jda.api.Permission

interface TopLevelApplicationCommandBuilder<T : ApplicationCommandOptionAggregateBuilder<T>> : ApplicationCommandBuilder<T> {
    /**
     * @see TopLevelSlashCommandData.scope
     * @see JDAUserCommand.scope
     * @see JDAMessageCommand.scope
     */
    val scope: CommandScope

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
