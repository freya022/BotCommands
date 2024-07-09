package io.github.freya022.botcommands.internal.commands.application

import gnu.trove.map.hash.TLongObjectHashMap
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.*
import io.github.freya022.botcommands.api.commands.application.context.message.MessageCommandInfo
import io.github.freya022.botcommands.api.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.api.core.debugNull
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.safeCast
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val logger = KotlinLogging.loggerOf<ApplicationCommandsContext>()

@BService
internal class ApplicationCommandsContextImpl internal constructor(private val context: BContextImpl) : ApplicationCommandsContext {
    private val writeLock = ReentrantLock()
    private val liveTopLevelApplicationCommands = TLongObjectHashMap<TopLevelApplicationCommandInfo>()

    override fun findSlashCommand(guild: Guild?, path: CommandPath): SlashCommandInfo? {
        val topLevelCommand = liveTopLevelApplicationCommands.valueCollection()
            .find { it.guildId == guild?.idLong && it.name == path.name }
            ?: return logger.debugNull { "Could not find slash command with top-level name '${path.name}'" }

        return getApplicationCommandById<SlashCommandInfo>(topLevelCommand.idLong, path.group, path.subname)
    }

    override fun findUserCommand(guild: Guild?, name: String): UserCommandInfo? {
        val topLevelCommand = liveTopLevelApplicationCommands.valueCollection()
            .find { it.guildId == guild?.idLong && it.name == name }
            ?: return logger.debugNull { "Could not find user command '$name'" }

        return topLevelCommand as? UserCommandInfo
            ?: return logger.debugNull { "Top level command '$name' is not a ${classRef<UserCommandInfo>()}" }
    }

    override fun findMessageCommand(guild: Guild?, name: String): MessageCommandInfo? {
        val topLevelCommand = liveTopLevelApplicationCommands.valueCollection()
            .find { it.guildId == guild?.idLong && it.name == name }
            ?: return logger.debugNull { "Could not find message command '$name'" }

        return topLevelCommand as? MessageCommandInfo
            ?: return logger.debugNull { "Top level command '$name' is not a ${classRef<MessageCommandInfo>()}" }
    }

    override fun getApplicationCommands(guild: Guild?): List<TopLevelApplicationCommandInfo> =
        liveTopLevelApplicationCommands.valueCollection().filter { it.guildId == guild?.idLong }

    override fun getEffectiveApplicationCommands(guild: Guild?): List<TopLevelApplicationCommandInfo> {
        val topLevelCommands = liveTopLevelApplicationCommands.valueCollection()
        return when (guild) {
            // Keep global
            null -> topLevelCommands.filter { it.guildId == null }.unmodifiableView()
            // Keep global and guild with id
            else -> topLevelCommands.filter { it.guildId == null || it.guildId == guild.idLong }.unmodifiableView()
        }
    }

    internal fun putLiveApplicationCommandsMap(topLevelCommands: Collection<TopLevelApplicationCommandInfo>): Unit = writeLock.withLock {
        topLevelCommands.forEach { topLevelCommand ->
            liveTopLevelApplicationCommands.put(topLevelCommand.idLong, topLevelCommand)
        }
    }

    // Logged at debug as this could be used for introspection
    override fun <T : ApplicationCommandInfo> getApplicationCommandById(type: Class<T>, commandId: Long, group: String?, subcommand: String?): T? {
        val topLevelCommand = liveTopLevelApplicationCommands[commandId]
            ?: return logger.debugNull { "Could not find command with id $commandId" }

        var command: ApplicationCommandInfo = topLevelCommand
        if (topLevelCommand is TopLevelSlashCommandInfo) {
            if (group != null) {
                requireNotNull(subcommand) { "Subcommand cannot be null if a group is specified" }
                val subgroup = topLevelCommand.subcommandGroups[group]
                    ?: return logger.debugNull { "Could not find group '$group' in '${topLevelCommand.name}' ($commandId)" }
                command = subgroup.subcommands[subcommand]
                    ?: return logger.debugNull { "Could not find subcommand '$subcommand' in '${topLevelCommand.name} $group' ($commandId)" }
            } else if (subcommand != null) {
                command = topLevelCommand.subcommands[subcommand]
                    ?: return logger.debugNull { "Could not find subcommand '$subcommand' in '${topLevelCommand.name}' ($commandId)" }
            }
        }

        return type.safeCast(command)
            ?: return logger.debugNull { "Command '${command.fullCommandName}' is not a ${type.simpleNestedName}" }
    }

    override fun updateGlobalApplicationCommands(force: Boolean): CompletableFuture<CommandUpdateResult> {
        return context.coroutineScopesConfig.commandUpdateScope.async {
            context.getService<ApplicationCommandsBuilder>().updateGlobalCommands(force)
        }.asCompletableFuture()
    }

    override fun updateGuildApplicationCommands(guild: Guild, force: Boolean): CompletableFuture<CommandUpdateResult> {
        return context.coroutineScopesConfig.commandUpdateScope.async {
            context.getService<ApplicationCommandsBuilder>().updateGuildCommands(guild, force)
        }.asCompletableFuture()
    }
}