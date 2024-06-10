package io.github.freya022.botcommands.internal.commands.application

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandGroupInfo
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.overwriteBytes
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandsCache.Companion.toJsonBytes
import io.github.freya022.botcommands.internal.commands.application.localization.BCLocalizationFunction
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.getDiscordOptions
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.asScopeString
import io.github.freya022.botcommands.internal.utils.rethrowUser
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.*
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.nio.file.Files

private val logger = KotlinLogging.logger { }

internal class ApplicationCommandsUpdater private constructor(
    private val context: BContextImpl,
    private val guild: Guild?,
    manager: AbstractApplicationCommandManager
) {
    private val commandsCache = context.getService<ApplicationCommandsCache>()
    private val onlineCheck = context.applicationConfig.onlineAppCommandCheckEnabled

    private val commandsCachePath = when (guild) {
        null -> commandsCache.globalCommandsPath
        else -> commandsCache.getGuildCommandsPath(guild)
    }

    internal val allApplicationCommands: Collection<ApplicationCommandInfo> = manager.allApplicationCommands
    private val allCommandData: Collection<CommandData>
    internal val filteredCommandsCount: Int get() = allCommandData.size

    init {
        Files.createDirectories(commandsCachePath.parent)

        allCommandData = mapSlashCommands(manager.slashCommands) +
                mapContextCommands(manager.userContextCommands, Command.Type.USER) +
                mapContextCommands(manager.messageContextCommands, Command.Type.MESSAGE)

        //Apply localization
        val localizationFunction: LocalizationFunction = BCLocalizationFunction(context)
        for (commandData in allCommandData) {
            commandData.setLocalizationFunction(localizationFunction)
        }
    }

    suspend fun shouldUpdateCommands(): Boolean {
        val oldBytes = when {
            onlineCheck -> {
                (guild?.retrieveCommands(true) ?: context.jda.retrieveCommands(true))
                    .await()
                    .map { CommandData.fromCommand(it) }.toJsonBytes()
            }

            else -> {
                if (Files.notExists(commandsCachePath)) {
                    logger.trace { "Updating commands because cache file does not exists" }
                    return true
                }

                withContext(Dispatchers.IO) {
                    Files.readAllBytes(commandsCachePath)
                }
            }
        }

        val newBytes = allCommandData.toJsonBytes()
        return (!ApplicationCommandsCache.isJsonContentSame(context, oldBytes, newBytes)).also { needUpdate ->
            if (needUpdate) {
                logger.trace { "Updating commands because content is not equal" }

                if (context.debugConfig.enableApplicationDiffsLogs) {
                    logger.trace { "Old commands bytes: ${oldBytes.decodeToString()}" }
                    logger.trace { "New commands bytes: ${newBytes.decodeToString()}" }
                }
            }
        }
    }

    suspend fun updateCommands() {
        val updateAction = guild?.updateCommands() ?: context.jda.updateCommands()
        val commands = updateAction
            .addCommands(allCommandData)
            .await()

        saveCommandData(guild)
        printPushedCommandData(commands, guild)
    }

    private fun mapSlashCommands(commands: Collection<TopLevelSlashCommandInfo>): List<SlashCommandData> =
        commands
            .filterCommands()
            .mapCommands { info: TopLevelSlashCommandInfo ->
                val topLevelData = Commands.slash(info.name, info.description).configureTopLevel(info)
                if (info.isTopLevelCommandOnly) {
                    topLevelData.addOptions(info.getDiscordOptions(guild))
                } else {
                    topLevelData.addSubcommandGroups(
                        info.subcommandGroups.values.filterCommands().mapToSubcommandGroupData()
                    )
                    topLevelData.addSubcommands(info.subcommands.values.filterCommands().mapToSubcommandData())
                }

                topLevelData
            }

    private fun Collection<SlashSubcommandGroupInfo>.mapToSubcommandGroupData() =
        this.map { subcommandGroupInfo ->
            SubcommandGroupData(subcommandGroupInfo.name, subcommandGroupInfo.description).also {
                it.addSubcommands(subcommandGroupInfo.subcommands.values.mapToSubcommandData())
            }
        }

    private fun Collection<SlashSubcommandInfo>.mapToSubcommandData() =
        this.map { subcommandInfo ->
            SubcommandData(subcommandInfo.name, subcommandInfo.description)
                .addOptions(subcommandInfo.getDiscordOptions(guild))
        }

    private fun <T> mapContextCommands(
        commands: Collection<T>,
        type: Command.Type
    ): List<CommandData> where T : TopLevelApplicationCommandInfo,
                               T : ApplicationCommandInfo {
        return commands
            .filterCommands()
            .mapCommands { info: T ->
                Commands.context(type, info.name).configureTopLevel(info)
            }
    }

    private inline fun <T : ApplicationCommandInfo, R : CommandData> List<T>.mapCommands(transform: (T) -> R): List<R> =
        map {
            try {
                transform(it)
            } catch (e: Exception) { //TODO use some sort of exception context for command paths
                rethrowUser(it.function, "An exception occurred while processing command '${it.path.fullPath}'", e)
            }
        }

    private fun <T : INamedCommand> Collection<T>.filterCommands() = filter { info ->
        context.settingsProvider?.let { settings ->
            guild?.let { guild ->
                return@filter settings.getGuildCommands(guild).filter.test(info.path)
            }
        }

        return@filter true
    }

    private fun <D : CommandData, T> D.configureTopLevel(info: T): D
            where T : TopLevelApplicationCommandInfo,
                  T : ApplicationCommandInfo = apply {
        if (info.nsfw) isNSFW = true
        if (info.scope == CommandScope.GLOBAL_NO_DM) isGuildOnly = true
        if (info.isDefaultLocked) {
            defaultPermissions = DefaultMemberPermissions.DISABLED
        } else if (info.userPermissions.isNotEmpty()) {
            defaultPermissions = DefaultMemberPermissions.enabledFor(info.userPermissions)
        }
    }

    private fun printPushedCommandData(commands: List<Command>, guild: Guild?) {
        if (!logger.isTraceEnabled()) return

        logger.trace {
            val commandNumber = commands.size
            val scope = guild.asScopeString()
            "Updated $commandNumber / $filteredCommandsCount commands for $scope"
        }
    }

    private fun saveCommandData(guild: Guild?) {
        try {
            commandsCachePath.overwriteBytes(allCommandData.toJsonBytes())
        } catch (e: Exception) {
            logger.error(e) {
                "An exception occurred while temporarily saving ${guild.asScopeString()} commands in '${commandsCachePath.toAbsolutePath()}'"
            }
        }
    }

    companion object {
        fun ofGlobal(context: BContextImpl, manager: GlobalApplicationCommandManager): ApplicationCommandsUpdater {
            return ApplicationCommandsUpdater(context, null, manager)
        }

        fun ofGuild(context: BContextImpl, guild: Guild, manager: GuildApplicationCommandManager): ApplicationCommandsUpdater {
            return ApplicationCommandsUpdater(context, guild, manager)
        }
    }
}