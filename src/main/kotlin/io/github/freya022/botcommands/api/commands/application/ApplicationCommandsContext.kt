package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandList
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.context.message.MessageCommandInfo
import io.github.freya022.botcommands.api.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.CompletableFuture

/**
 * Helps to get application commands of a specific scope, find application commands with their name and update commands.
 */
@InterfacedService(acceptMultiple = false)
interface ApplicationCommandsContext {
    /**
     * Returns the [SlashCommandInfo] with the specified path,
     * if it is published in that scope, either in the guild, or globally.
     *
     * @param guild The guild from which to get the commands, can be `null` for global commands
     * @param path  Full path of the slash command (Examples: `ban` ; `info user` ; `ban user perm`)
     */
    fun findSlashCommand(guild: Guild?, path: CommandPath): SlashCommandInfo?

    /**
     * Returns the [UserCommandInfo] with the specified name,
     * if it is published in that scope, either in the guild, or globally.
     *
     * @param guild The guild from which to get the commands, can be `null` for global commands
     * @param name  Name of the user context command
     */
    fun findUserCommand(guild: Guild?, name: String): UserCommandInfo?

    /**
     * Returns the [MessageCommandInfo] with the specified name,
     * if it is published in that scope, either in the guild, or globally.
     *
     * @param guild The guild from which to get the commands, can be `null` for global commands
     * @param name  Name of the message context command
     */
    fun findMessageCommand(guild: Guild?, name: String): MessageCommandInfo?

    /**
     * Returns the application commands currently pushed in the specified guild's scope.
     *
     * If a guild is specified, the global commands will not be included,
     * use [getEffectiveApplicationCommands] instead.
     *
     * @param guild The guild from which to get the commands, can be `null` for global commands
     *
     * @see getEffectiveApplicationCommands
     */
    fun getApplicationCommands(guild: Guild?): List<TopLevelApplicationCommandInfo>

    /**
     * Returns the effective list of application commands this guild has access to.
     *
     * This always includes global commands.
     *
     * @param guild The guild in which to query the commands, can be `null` for global commands.
     *
     * @return The effective list of top-level commands this guild has access to
     */
    fun getEffectiveApplicationCommands(guild: Guild?): List<TopLevelApplicationCommandInfo>

    /**
     * Returns the application command with the specific type, id, group and subcommand,
     * or `null` if one of the expected arguments does not match.
     *
     * Works for both global and guild commands.
     *
     * @param type       Expected type of the application command
     * @param commandId  Expected ID of the top-level command
     * @param group      Optional group name
     * @param subcommand Optional subcommand name
     */
    fun <T : ApplicationCommandInfo> getApplicationCommandById(type: Class<T>, commandId: Long, group: String?, subcommand: String?): T?

    /**
     * Updates the application commands for the global scope.
     *
     * @param force Whether the commands should be updated no matter what
     *
     * @return A [CompletableFuture]&lt;[CommandUpdateResult]&gt;
     *
     * @see CompletableFuture.asDeferred
     * @see Deferred.await
     */
    fun updateGlobalApplicationCommands(force: Boolean): CompletableFuture<CommandUpdateResult>

    /**
     * Updates the application commands in the specified guild.
     *
     * A use case may be to remove a command from a guild while the bot is running
     * (either by filtering with [CommandList] or not running a DSL declaration).
     *
     * @param guild The guild which needs to be updated
     * @param force Whether the commands should be updated no matter what
     *
     * @return A [CompletableFuture]&lt;[CommandUpdateResult]&gt;
     *
     * @see CompletableFuture.asDeferred
     * @see Deferred.await
     */
    fun updateGuildApplicationCommands(guild: Guild, force: Boolean): CompletableFuture<CommandUpdateResult>
}

/**
 * Returns the application command with the specific type, id, group and subcommand,
 * or `null` if one of the expected arguments does not match.
 *
 * Works for both global and guild commands.
 *
 * @param T          Expected type of the application command
 * @param commandId  Expected ID of the top-level command
 * @param group      Optional group name
 * @param subcommand Optional subcommand name
 */
inline fun <reified T : ApplicationCommandInfo> ApplicationCommandsContext.getApplicationCommandById(
    commandId: Long,
    group: String?,
    subcommand: String?,
): T? = getApplicationCommandById(T::class.java, commandId, group, subcommand)

