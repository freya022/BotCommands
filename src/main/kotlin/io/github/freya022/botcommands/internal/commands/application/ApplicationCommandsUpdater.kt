package io.github.freya022.botcommands.internal.commands.application

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.exceptions.ApplicationCommandUpdateException
import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandGroupInfo
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig.LogDataIf
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.DefaultObjectMapper
import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import io.github.freya022.botcommands.internal.commands.application.cache.factory.ApplicationCommandsCacheFactory
import io.github.freya022.botcommands.internal.commands.application.diff.DiffLogger
import io.github.freya022.botcommands.internal.commands.application.localization.BCLocalizationFunction
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.getDiscordOptions
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.exceptions.internalErrorMessage
import io.github.freya022.botcommands.internal.utils.asScopeString
import io.github.freya022.botcommands.internal.utils.rethrowAt
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.exceptions.ParsingException
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.Command.Type.*
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.*
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import net.dv8tion.jda.api.utils.data.DataArray

private val logger = KotlinLogging.logger { }

internal class ApplicationCommandsUpdater private constructor(
    private val context: BContextImpl,
    private val guild: Guild?,
    manager: AbstractApplicationCommandManager
) {
    private val updateRateLimiter: ApplicationCommandsUpdateRateLimiter = context.getService()

    private val commandsCache: ApplicationCommandsCache
    private val cacheConfig: ApplicationCommandsCacheConfig

    init {
        val cacheFactory = context.getService<ApplicationCommandsCacheFactory>()
        commandsCache = cacheFactory.create(guild)
        cacheConfig = cacheFactory.cacheConfig
    }

    internal val applicationCommands: Collection<TopLevelApplicationCommandInfo> = manager.allApplicationCommands.filterCommands()
    private val commandData: Collection<CommandData>
    internal inline val commandsCount: Int get() = commandData.size

    internal lateinit var metadata: List<TopLevelApplicationCommandMetadataImpl>
        private set

    init {
        commandData = mapSlashCommands(manager.slashCommands) +
                mapContextCommands(manager.userContextCommands, USER) +
                mapContextCommands(manager.messageContextCommands, MESSAGE)

        //Apply localization
        val localizationFunction: LocalizationFunction = BCLocalizationFunction(context)
        for (commandData in commandData) {
            commandData.setLocalizationFunction(localizationFunction)
        }
    }

    suspend fun tryUpdateCommands(force: Boolean): Boolean {
        if (force) {
            updateCommands()
            return true
        }

        val needsUpdate = if (cacheConfig.checkOnline) {
            checkOnlineCommands()
        } else {
            checkOfflineCommands()
        }

        if (!needsUpdate) return false

        updateCommands()
        return true
    }

    private suspend fun checkOnlineCommands(): Boolean {
        val oldCommands = retrieveCommands().filterActualCommands()
        val oldCommandBytes = oldCommands
            .map(CommandData::fromCommand)
            .toJsonBytes()

        metadata = oldCommands.map { TopLevelApplicationCommandMetadataImpl.fromCommand(guild, it) }
        return checkCommandJson(oldCommandBytes.decodeToString())
    }

    private suspend fun retrieveCommands(): List<Command> = updateRateLimiter.withToken {
        val action = when {
            guild != null -> guild.retrieveCommands(true)
            else -> context.jda.retrieveCommands(true)
        }
        action.await()
    }

    private suspend fun checkOfflineCommands(): Boolean {
        val data = commandsCache.tryRead()
        if (data.commands == null) {
            logger.trace { "Updating commands because no commands are cached" }
            return true
        }

        if (data.metadata == null) {
            logger.trace { "Updating commands because no metadata is cached" }
            return true
        }

        val hasMissingKey = updateOnMissingKey {
            val array = data.metadata.let(DataArray::fromJson)
            metadata = readMetadata(array)
        }

        return hasMissingKey || checkCommandJson(data.commands)
    }

    private suspend inline fun updateOnMissingKey(crossinline block: suspend () -> Unit): Boolean = try {
        block()
        false
    } catch (e: ParsingException) {
        logger.debug(e) { "Considering commands to be outdated because of an error during deserialization" }
        true
    }

    private fun readMetadata(array: DataArray): List<TopLevelApplicationCommandMetadataImpl> {
        val metadata = buildList(array.length()) {
            for (i in 0..<array.length()) {
                add(TopLevelApplicationCommandMetadataImpl.fromData(array.getObject(i)))
            }
        }
        val missingCommands = commandData.mapTo(hashSetOf()) { it.name } - metadata.mapTo(hashSetOf()) { it.name }
        if (missingCommands.isNotEmpty()) {
            throw ParsingException("Missing metadata for $missingCommands")
        }

        return metadata
    }

    @Suppress("UNCHECKED_CAST")
    private fun checkCommandJson(oldData: String): Boolean {
        val newBytes = commandData.toJsonBytes()

        val oldCommands = DefaultObjectMapper.readList(oldData) as List<Map<String, *>>
        val newCommands = DefaultObjectMapper.readList(newBytes) as List<Map<String, *>>

        val isSame = DiffLogger.withLogger(context, cacheConfig, guild.asScopeString()) {
            cacheConfig.diffEngine.instance.checkCommands(oldCommands, newCommands)
        }

        val logData = when (cacheConfig.logDataIf) {
            LogDataIf.CHANGED -> !isSame
            LogDataIf.ALWAYS -> true
            LogDataIf.NEVER -> false
        }
        if (logData) {
            logger.trace { "Old commands data: $oldData" }
            logger.trace { "New commands data: ${newBytes.decodeToString()}" }
        }

        return !isSame
    }

    private suspend fun updateCommands() {
        val commands = pushCommands()

        metadata = commands.map { TopLevelApplicationCommandMetadataImpl.fromCommand(guild, it) }

        saveCommandData(guild)
        printPushedCommandData(commands, guild)
    }

    private suspend fun pushCommands(): List<Command> = updateRateLimiter.withToken {
        val action = when {
            guild != null -> guild.updateCommands()
            else -> context.jda.updateCommands()
        }
        action.addCommands(commandData).await().filterActualCommands()
    }

    private fun List<Command>.filterActualCommands() = filter {
        when (it.type) {
            SLASH, USER, MESSAGE -> true
            UNKNOWN -> {
                logger.warn { "Ignoring unknown command type, id: ${it.id}, name: ${it.name}" }
                false // Discard, users can't create unknown commands
            }
            else -> {
                logger.warn { internalErrorMessage("JDA seems to have added a new command type, id: ${it.id}, name: ${it.name}") }
                true // Keep it, the user seems to actively use it
            }
        }
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
                @Suppress("DEPRECATION")
                return@filter settings.getGuildCommands(guild).filter.test(info.path)
            }
        }

        return@filter true
    }

    private fun <D : CommandData> D.configureTopLevel(info: TopLevelApplicationCommandInfo): D = apply {
        if (info.nsfw) isNSFW = true
        setContexts(info.contexts)
        setIntegrationTypes(info.integrationTypes)
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
            "Updated $commandNumber commands for $scope"
        }
    }

    private suspend fun saveCommandData(guild: Guild?) {
        try {
            commandsCache.write(
                commandData.toJsonBytes(),
                metadata.map { it.toData() }.let(DataArray::fromCollection).toJson()
            )
        } catch (e: Exception) {
            logger.error(e) {
                "An exception occurred while saving ${guild.asScopeString().substringBefore(" scope")} commands with $commandsCache"
            }
        }
    }

    private fun Collection<CommandData>.toJsonBytes(): ByteArray = DataArray.fromCollection(this).toJson()

    companion object {
        fun ofGlobal(context: BContextImpl, manager: GlobalApplicationCommandManager): ApplicationCommandsUpdater {
            return ApplicationCommandsUpdater(context, null, manager)
        }

        fun ofGuild(context: BContextImpl, guild: Guild, manager: GuildApplicationCommandManager): ApplicationCommandsUpdater {
            return ApplicationCommandsUpdater(context, guild, manager)
        }
    }
}