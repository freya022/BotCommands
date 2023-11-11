package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandList
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfo
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.CompletableFuture

/**
 * Helps to get application commands of a specific scope, find application commands with their name and update commands.
 */
@InjectedService
interface ApplicationCommandsContext {
    /**
     * Returns the [SlashCommandInfo] with the specified path,
     * if it is published in that scope, either in the guild, or globally.
     *
     * @param guild The guild from which to get the commands, can be `null` for global commands
     * @param path  Full path of the slash command (Examples: `ban` ; `info user` ; `ban user perm`)
     */
    fun findLiveSlashCommand(guild: Guild?, path: CommandPath): SlashCommandInfo?

    /**
     * Returns the [UserCommandInfo] with the specified name,
     * if it is published in that scope, either in the guild, or globally.
     *
     * @param guild The guild from which to get the commands, can be `null` for global commands
     * @param name  Name of the user context command
     */
    fun findLiveUserCommand(guild: Guild?, name: String): UserCommandInfo?

    /**
     * Returns the [MessageCommandInfo] with the specified name,
     * if it is published in that scope, either in the guild, or globally.
     *
     * @param guild The guild from which to get the commands, can be `null` for global commands
     * @param name  Name of the message context command
     */
    fun findLiveMessageCommand(guild: Guild?, name: String): MessageCommandInfo?

    /**
     * Returns the application commands currently pushed in the specified guild's scope.
     *
     * If a guild is specified, the global commands will not be included,
     * use [getEffectiveApplicationCommandsMap] instead.
     *
     * @param guild The guild from which to get the commands, can be `null` for global commands
     *
     * @see getEffectiveApplicationCommandsMap
     */
    fun getLiveApplicationCommandsMap(guild: Guild?): ApplicationCommandMap?

    /**
     * Returns the effective application commands available for the specific guild.
     *
     * @param guild The guild in which to query the commands, can be `null` for global commands
     *
     * @return The [ApplicationCommandMap] of the specific guild
     */
    fun getEffectiveApplicationCommandsMap(guild: Guild?): ApplicationCommandMap

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
