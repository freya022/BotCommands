package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.AbstractApplicationCommandManager
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.api.core.utils.overwriteBytes
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.ApplicationCommandsCache.Companion.toJsonBytes
import com.freya02.botcommands.internal.commands.application.context.message.MessageCommandInfo
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.commands.application.localization.BCLocalizationFunction
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashSubcommandGroupInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashSubcommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.getDiscordOptions
import com.freya02.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo
import com.freya02.botcommands.internal.commands.mixins.INamedCommand
import com.freya02.botcommands.internal.utils.asScopeString
import com.freya02.botcommands.internal.utils.rethrowUser
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.nio.file.Files

internal class ApplicationCommandsUpdater private constructor(
    private val context: BContextImpl,
    private val guild: Guild?,
    manager: AbstractApplicationCommandManager
) {
    private val logger = KotlinLogging.logger {  }

    private val commandsCache = context.getService<ApplicationCommandsCache>()
    private val onlineCheck = context.applicationConfig.onlineAppCommandCheckEnabled

    private val commandsCachePath = when (guild) {
        null -> commandsCache.globalCommandsPath
        else -> commandsCache.getGuildCommandsPath(guild)
    }

    val applicationCommands: Collection<ApplicationCommandInfo>
    private val allCommandData: Collection<CommandData>

    init {
        Files.createDirectories(commandsCachePath.parent)

        applicationCommands = manager.applicationCommands.values
        allCommandData = computeCommands().allCommandData

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
                    logger.trace("Updating commands because cache file does not exists")
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
                logger.trace("Updating commands because content is not equal")

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

    private fun computeCommands() = ApplicationCommandDataMap().also { map ->
        computeSlashCommands(applicationCommands, map)
        computeContextCommands(applicationCommands, map, UserCommandInfo::class.java, Command.Type.USER)
        computeContextCommands(applicationCommands, map, MessageCommandInfo::class.java, Command.Type.MESSAGE)
    }

    private fun computeSlashCommands(guildApplicationCommands: Collection<ApplicationCommandInfo>, map: ApplicationCommandDataMap) {
        guildApplicationCommands
            .filterIsInstance<TopLevelSlashCommandInfo>()
            .filterCommands()
            .forEach { info: TopLevelSlashCommandInfo ->
                try {
                    val isTopLevel = info.isTopLevelCommandOnly()
                    val topLevelData = Commands.slash(info.name, info.description).also { commandData ->
                        if (isTopLevel) {
                            commandData.addOptions(info.getDiscordOptions(guild))
                        }

                        commandData.configureTopLevel(info)
                    }

                    topLevelData.addSubcommandGroups(info.subcommandGroups.values.filterCommands().mapToSubcommandGroupData())
                    topLevelData.addSubcommands(info.subcommands.values.filterCommands().mapToSubcommandData())

                    map[Command.Type.SLASH, info.name] = topLevelData
                } catch (e: Exception) { //TODO use some sort of exception context for command paths
                    rethrowUser(info.function, "An exception occurred while processing command '${info.name}'", e)
                }
            }
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

    private fun <T> computeContextCommands(
        guildApplicationCommands: Collection<ApplicationCommandInfo>,
        map: ApplicationCommandDataMap,
        targetClazz: Class<T>,
        type: Command.Type
    ) where T : ITopLevelApplicationCommandInfo,
            T : ApplicationCommandInfo {
        guildApplicationCommands
            .filterIsInstance(targetClazz)
            .filterCommands()
            .forEach { info: T ->
                try {
                    //Standard command
                    map[type, info.name] = Commands.context(type, info.name).configureTopLevel(info)
                } catch (e: Exception) {
                    rethrowUser(info.function, "An exception occurred while processing a ${type.name} command ${info.name}", e)
                }
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

    private fun <T> CommandData.configureTopLevel(info: T): CommandData
            where T : ITopLevelApplicationCommandInfo,
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
        if (!logger.isTraceEnabled) return

        logger.trace {
            val commandNumber = commands.size
            val sentCommandNumber = allCommandData.size
            val scope = guild.asScopeString()
            "Updated $commandNumber / $sentCommandNumber commands for $scope"
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