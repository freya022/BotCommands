package io.github.freya022.botcommands.internal.commands.application

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.exceptions.ApplicationCommandUpdateException
import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandGroupInfo
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.overwriteBytes
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandsCache.Companion.toJsonBytes
import io.github.freya022.botcommands.internal.commands.application.localization.BCLocalizationFunction
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.getDiscordOptions
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.asScopeString
import io.github.freya022.botcommands.internal.utils.rethrowAt
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.exceptions.ParsingException
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.*
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import net.dv8tion.jda.api.utils.data.DataArray
import java.nio.file.Files
import kotlin.io.path.bufferedReader

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

    private val commandsMetadataCachePath = when (guild) {
        null -> commandsCache.globalCommandsMetadataPath
        else -> commandsCache.getGuildCommandsMetadataPath(guild)
    }

    internal val allApplicationCommands: Collection<ApplicationCommandInfo> = manager.allApplicationCommands
    private val allCommandData: Collection<CommandData>
    internal val filteredCommandsCount: Int get() = allCommandData.size

    internal lateinit var metadata: List<TopLevelApplicationCommandMetadataImpl>
        private set

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

    suspend fun tryUpdateCommands(force: Boolean): Boolean {
        if (force) {
            updateCommands()
            return true
        }

        val needsUpdate = if (onlineCheck) {
            checkOnlineCommands()
        } else {
            checkOfflineCommands()
        }

        if (!needsUpdate) return false

        updateCommands()
        return true
    }

    private suspend fun checkOnlineCommands(): Boolean {
        val oldCommands = (guild?.retrieveCommands(true) ?: context.jda.retrieveCommands(true)).await()
        val oldCommandBytes = oldCommands
            .map(CommandData::fromCommand)
            .toJsonBytes()

        metadata = oldCommands.map { TopLevelApplicationCommandMetadataImpl.fromCommand(guild, it) }
        return checkCommandJson(oldCommandBytes)
    }

    private suspend fun checkOfflineCommands(): Boolean = withContext(Dispatchers.IO) {
        if (Files.notExists(commandsCachePath)) {
            logger.trace { "Updating commands because cache file does not exists" }
            return@withContext true
        }

        if (Files.notExists(commandsMetadataCachePath)) {
            logger.trace { "Updating commands metadata because cache file does not exists" }
            return@withContext true
        }

        val hasMissingKey = updateOnMissingKey {
            val array = commandsMetadataCachePath.bufferedReader().use(DataArray::fromJson)
            metadata = readMetadata(array)
        }

        hasMissingKey || checkCommandJson(Files.readAllBytes(commandsCachePath))
    }

    private inline fun updateOnMissingKey(crossinline block: () -> Unit): Boolean = try {
        block()
        false
    } catch (e: ParsingException) {
        true
    }

    private fun readMetadata(array: DataArray): List<TopLevelApplicationCommandMetadataImpl> {
        val metadata = TopLevelApplicationCommandMetadataImpl.fromData(array)
        val missingCommands = allCommandData.mapTo(hashSetOf()) { it.name } - metadata.mapTo(hashSetOf()) { it.name }
        if (missingCommands.isNotEmpty()) {
            throw ParsingException("Missing metadata for $missingCommands")
        }

        return metadata
    }

    private fun checkCommandJson(oldBytes: ByteArray): Boolean {
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

    private suspend fun updateCommands() {
        val updateAction = guild?.updateCommands() ?: context.jda.updateCommands()
        val commands = updateAction
            .addCommands(allCommandData)
            .await()

        metadata = commands.map { TopLevelApplicationCommandMetadataImpl.fromCommand(guild, it) }

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
        this.mapCommands { subcommandGroupInfo ->
            SubcommandGroupData(subcommandGroupInfo.name, subcommandGroupInfo.description).also {
                it.addSubcommands(subcommandGroupInfo.subcommands.values.mapToSubcommandData())
            }
        }

    private fun Collection<SlashSubcommandInfo>.mapToSubcommandData() =
        this.mapCommands { subcommandInfo ->
            SubcommandData(subcommandInfo.name, subcommandInfo.description)
                .addOptions(subcommandInfo.getDiscordOptions(guild))
        }

    private fun mapContextCommands(
        commands: Collection<TopLevelApplicationCommandInfo>,
        type: Command.Type
    ): List<CommandData> {
        return commands
            .filterCommands()
            .mapCommands { info: TopLevelApplicationCommandInfo ->
                Commands.context(type, info.name).configureTopLevel(info)
            }
    }

    private inline fun <T, R> Collection<T>.mapCommands(
        transform: (T) -> R,
    ): List<R> where T : INamedCommand,
                     T : IDeclarationSiteHolder {
        return map {
            try {
                transform(it)
            } catch (e: ApplicationCommandUpdateException) {
                throw e
            } catch (e: Exception) {
                e.rethrowAt(::ApplicationCommandUpdateException, "An exception occurred while pushing '${it.path}'", it.declarationSite)
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

    private fun <D : CommandData> D.configureTopLevel(info: TopLevelApplicationCommandInfo): D = apply {
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
            commandsMetadataCachePath.overwriteBytes(metadata.map { it.toData() }.let(DataArray::fromCollection).toJson())
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