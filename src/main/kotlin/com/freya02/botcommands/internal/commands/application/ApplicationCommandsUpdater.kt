package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.builder.DebugBuilder
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.IApplicationCommandManager
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asScopeString
import com.freya02.botcommands.internal.commands.application.ApplicationCommandsCache.Companion.toJsonBytes
import com.freya02.botcommands.internal.commands.application.context.message.MessageCommandInfo
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.commands.application.localization.BCLocalizationFunction
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashSubcommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.getMethodOptions
import com.freya02.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo
import com.freya02.botcommands.internal.overwriteBytes
import com.freya02.botcommands.internal.rethrowUser
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.nio.file.Files

private val LOGGER = Logging.getLogger()
internal class ApplicationCommandsUpdater private constructor(
    private val context: BContextImpl,
    private val guild: Guild?,
    manager: IApplicationCommandManager
) {
    private val commandsCache = context.getService(ApplicationCommandsCache::class)
    private val onlineCheck = context.config.applicationConfig.onlineAppCommandCheckEnabled

    private val commandsCachePath = when (guild) {
        null -> commandsCache.globalCommandsPath
        else -> commandsCache.getGuildCommandsPath(guild)
    }

    val applicationCommands: List<ApplicationCommandInfo>
    private val allCommandData: Collection<CommandData>

    init {
        Files.createDirectories(commandsCachePath.parent)

        applicationCommands = manager.applicationCommands.let {
            it.filter {
                context.settingsProvider?.let { settings ->
                    guild?.let { guild ->
                        return@filter settings.getGuildCommands(guild).filter.test(it._path)
                    }
                }

                return@filter true
            }
        }

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
                    LOGGER.trace("Updating commands because cache file does not exists")
                    return true
                }

                withContext(Dispatchers.IO) {
                    Files.readAllBytes(commandsCachePath)
                }
            }
        }

        val newBytes = allCommandData.toJsonBytes()
        return (!ApplicationCommandsCache.isJsonContentSame(oldBytes, newBytes)).also { needUpdate ->
            if (needUpdate) {
                LOGGER.trace("Updating commands because content is not equal")

                if (DebugBuilder.isLogApplicationDiffsEnabled()) {
                    LOGGER.trace("Old commands bytes: {}", oldBytes.decodeToString())
                    LOGGER.trace("New commands bytes: {}", newBytes.decodeToString())
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

    private fun computeSlashCommands(
        guildApplicationCommands: List<ApplicationCommandInfo>,
        map: ApplicationCommandDataMap
    ) {
        guildApplicationCommands
            .filterIsInstance<TopLevelSlashCommandInfo>()
            .forEach { info: TopLevelSlashCommandInfo ->
                try {
                    val isTopLevel = info.subcommands.isEmpty() && info.subcommandGroups.isEmpty()
                    val topLevelData = Commands.slash(info.name, info.description).also { commandData ->
                        if (isTopLevel) {
                            val methodOptions = info.getMethodOptions(guild)
                            commandData.addOptions(methodOptions)
                        }

                        commandData.configureTopLevel(info)
                    }

                    topLevelData.addSubcommandGroups(info.subcommandGroups.values.map { subcommandGroupInfo ->
                        SubcommandGroupData(subcommandGroupInfo.name, subcommandGroupInfo.description).also {
                            it.addSubcommands(subcommandGroupInfo.subcommands.values.mapToSubcommandData())
                        }
                    })

                    topLevelData.addSubcommands(info.subcommands.values.mapToSubcommandData())

                    map[Command.Type.SLASH, info.name] = topLevelData
                } catch (e: Exception) { //TODO use some sort of exception context for command paths
                    rethrowUser(info.method, "An exception occurred while processing command '${info.name}'", e)
                }
            }
    }

    private fun Collection<SlashSubcommandInfo>.mapToSubcommandData() =
        this.map { subcommandInfo ->
            SubcommandData(subcommandInfo.name, subcommandInfo.description).also {
                val methodOptions = subcommandInfo.getMethodOptions(guild)
                it.addOptions(methodOptions)
            }
        }

    private fun <T> computeContextCommands(
        guildApplicationCommands: List<ApplicationCommandInfo>,
        map: ApplicationCommandDataMap,
        targetClazz: Class<T>,
        type: Command.Type
    ) where T: ITopLevelApplicationCommandInfo, T: ApplicationCommandInfo {
        guildApplicationCommands
            .filterIsInstance(targetClazz)
            .forEach { info: T ->
                try {
                    //Standard command
                    map[type, info.name] = Commands.context(type, info.name).configureTopLevel(info)
                } catch (e: Exception) {
                    rethrowUser(
                        info.method,
                        "An exception occurred while processing a ${type.name} command ${info.name}",
                        e
                    )
                }
            }
    }

    private fun <T> CommandData.configureTopLevel(info: T): CommandData
            where T : ITopLevelApplicationCommandInfo,
                  T : ApplicationCommandInfo = apply {
        if (info.scope == CommandScope.GLOBAL_NO_DM) isGuildOnly = true
        if (info.isDefaultLocked) {
            defaultPermissions = DefaultMemberPermissions.DISABLED
        } else if (info.userPermissions.isNotEmpty()) {
            defaultPermissions = DefaultMemberPermissions.enabledFor(info.userPermissions)
        }
    }

    private fun printPushedCommandData(commands: List<Command>, guild: Guild?) {
        if (!LOGGER.isTraceEnabled) return

        val commandNumber = commands.size
        val sentCommandNumber = allCommandData.size
        val cacheViewNumber = context.applicationCommandsView.size
        val scope = guild.asScopeString()

        LOGGER.trace("Updated $commandNumber / $sentCommandNumber ($cacheViewNumber) commands for $scope")
    }

    private fun saveCommandData(guild: Guild?) {
        try {
            commandsCachePath.overwriteBytes(allCommandData.toJsonBytes())
        } catch (e: Exception) {
            LOGGER.error(
                "An exception occurred while temporarily saving {} commands in '{}'",
                guild.asScopeString(),
                commandsCachePath.toAbsolutePath(),
                e
            )
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