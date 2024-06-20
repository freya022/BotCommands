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
import io.github.freya022.botcommands.internal.core.BContextImpl
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
    private val liveApplicationCommandInfoMap = TLongObjectHashMap<ApplicationCommandMap>()
    private val liveTopLevelApplicationCommands = TLongObjectHashMap<TopLevelApplicationCommandInfo>()

    //TODO remove need for liveApplicationCommandInfoMap (and probably ApplicationCommandMap altogether?)
    // however, to find with a guild, the top level data will require the (nullable) guild id
    override fun findLiveSlashCommand(guild: Guild?, path: CommandPath): SlashCommandInfo? =
        getLiveApplicationCommandsMap(guild)?.findSlashCommand(path)
            ?: getLiveApplicationCommandsMap(null)?.findSlashCommand(path)

    override fun findLiveUserCommand(guild: Guild?, name: String): UserCommandInfo? =
        getLiveApplicationCommandsMap(guild)?.findUserCommand(name)
            ?: getLiveApplicationCommandsMap(null)?.findUserCommand(name)

    override fun findLiveMessageCommand(guild: Guild?, name: String): MessageCommandInfo? =
        getLiveApplicationCommandsMap(guild)?.findMessageCommand(name)
            ?: getLiveApplicationCommandsMap(null)?.findMessageCommand(name)

    override fun getLiveApplicationCommandsMap(guild: Guild?): ApplicationCommandMap? {
        return liveApplicationCommandInfoMap[getGuildKey(guild)]
    }

    override fun getEffectiveApplicationCommandsMap(guild: Guild?): ApplicationCommandMap = when (guild) {
        null -> getLiveApplicationCommandsMap(guild = null) ?: MutableApplicationCommandMap.EMPTY_MAP

        else -> (getLiveApplicationCommandsMap(guild = null) ?: MutableApplicationCommandMap.EMPTY_MAP) +
                (getLiveApplicationCommandsMap(guild = guild) ?: MutableApplicationCommandMap.EMPTY_MAP)
    }

    internal fun putLiveApplicationCommandsMap(guild: Guild?, map: ApplicationCommandMap): Unit = writeLock.withLock {
        liveApplicationCommandInfoMap.put(getGuildKey(guild), map.toUnmodifiableMap())

        map.allApplicationCommands.forEach {
            val topLevelInstance = it.topLevelInstance
            liveTopLevelApplicationCommands.put(topLevelInstance.idLong, topLevelInstance)
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

    private fun getGuildKey(guild: Guild?): Long {
        return guild?.idLong ?: 0
    }
}