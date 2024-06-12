package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.CommandUpdateException
import io.github.freya022.botcommands.api.commands.application.CommandUpdateResult
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveBestReference
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.reference
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildReadyEvent

@BService
internal class ApplicationCommandsBuilder(
    private val context: BContextImpl,
    private val globalApplicationCommandProviders: List<GlobalApplicationCommandProvider>,
    private val guildApplicationCommandProviders: List<GuildApplicationCommandProvider>
) {
    private val logger = KotlinLogging.logger {  }

    private val applicationCommandsContext = context.applicationCommandsContext

    private val globalUpdateMutex = Mutex()
    private val guildUpdateGlobalMutex: Mutex = Mutex()
    private val guildUpdateMutexMap: MutableMap<Long, Mutex> = hashMapOf()

    private var firstGlobalUpdate = true
    private val firstGuildUpdates = hashSetOf<Long>()

    @BEventListener
    internal suspend fun onInjectedJDA(event: InjectedJDAEvent) {
        try {
            updateCatching(null) { updateGlobalCommands() }
        } catch (e: Throwable) {
            logger.error(e) { "An error occurred while updating global commands" }
        }
    }

    @BEventListener
    internal suspend fun onGuildReady(event: GuildReadyEvent) {
        val guild = event.guild

        try {
            updateCatching(guild) {
                updateGuildCommands(guild)
            }
        } catch (t: Throwable) {
            handleGuildCommandUpdateException(guild, t)
        }
    }

    internal fun handleGuildCommandUpdateException(guild: Guild, t: Throwable) {
        logger.error(t) { "Encountered an exception while updating commands for guild '${guild.name}' (${guild.id})" }
    }

    internal suspend fun updateGlobalCommands(force: Boolean = false): CommandUpdateResult = globalUpdateMutex.withLock {
        val failedDeclarations: MutableList<CommandUpdateException> = arrayListOf()

        val manager = GlobalApplicationCommandManager(context)
        globalApplicationCommandProviders.forEach { globalApplicationCommandProvider ->
            runCatching {
                globalApplicationCommandProvider.declareGlobalApplicationCommands(manager)
            }.onFailure { failedDeclarations.add(CommandUpdateException(globalApplicationCommandProvider::declareGlobalApplicationCommands.resolveBestReference(), it)) }
        }

        if (failedDeclarations.isNotEmpty() && firstGlobalUpdate) {
            logger.error { "An exception occurred while updating global commands on startup, aborting any update" }
            return CommandUpdateResult(null, false, failedDeclarations)
        }

        val globalUpdater = ApplicationCommandsUpdater.ofGlobal(context, manager)
        val hasUpdated = globalUpdater.tryUpdateCommands(force)
        if (hasUpdated) {
            logger.debug { "Global commands were${getForceString(force)} updated (${getCheckTypeString()})" }
        } else {
            logger.debug { "Global commands does not have to be updated, ${globalUpdater.filteredCommandsCount} were kept (${getCheckTypeString()})" }
        }

        setMetadata(globalUpdater)
        applicationCommandsContext.putLiveApplicationCommandsMap(null, globalUpdater.allApplicationCommands.toApplicationCommandMap())

        firstGlobalUpdate = false
        return CommandUpdateResult(null, hasUpdated, failedDeclarations)
    }

    internal suspend fun updateGuildCommands(guild: Guild, force: Boolean = false): CommandUpdateResult {
        val slashGuildIds = context.applicationConfig.slashGuildIds
        if (slashGuildIds.isNotEmpty()) {
            if (guild.idLong !in slashGuildIds) {
                logger.trace { "Skipping application command updates in ${guild.name} (${guild.id}) as it is not in ${BApplicationConfig::slashGuildIds.reference}" }
                applicationCommandsContext.putLiveApplicationCommandsMap(guild, MutableApplicationCommandMap.EMPTY_MAP)
                return CommandUpdateResult(guild, false, listOf())
            }
        }

        guildUpdateGlobalMutex.withLock {
            guildUpdateMutexMap.computeIfAbsent(guild.idLong) { Mutex() }
        }.withLock {
            val failedDeclarations: MutableList<CommandUpdateException> = arrayListOf()

            val manager = GuildApplicationCommandManager(context, guild)
            guildApplicationCommandProviders.forEach { guildApplicationCommandProvider ->
                runCatching {
                    guildApplicationCommandProvider.declareGuildApplicationCommands(manager)
                }.onFailure { failedDeclarations.add(CommandUpdateException(guildApplicationCommandProvider::declareGuildApplicationCommands.resolveBestReference(), it)) }
            }

            if (failedDeclarations.isNotEmpty() && guild.idLong !in firstGuildUpdates) {
                context.dispatchException("An exception occurred while updating commands for '${guild.name}' (${guild.idLong}) on startup", null)
                logger.error { "An exception occurred while updating commands for '${guild.name}' (${guild.idLong}) on startup, aborting any update" }
                return CommandUpdateResult(guild, false, failedDeclarations)
            }

            val guildUpdater = ApplicationCommandsUpdater.ofGuild(context, guild, manager)
            val hasUpdated = guildUpdater.tryUpdateCommands(force)
            if (hasUpdated) {
                logger.debug { "Guild '${guild.name}' (${guild.id}) commands were${getForceString(force)} updated (${getCheckTypeString()})" }
            } else {
                logger.debug { "Guild '${guild.name}' (${guild.id}) commands does not have to be updated, ${guildUpdater.filteredCommandsCount} were kept (${getCheckTypeString()})" }
            }

            setMetadata(guildUpdater)
            applicationCommandsContext.putLiveApplicationCommandsMap(guild, guildUpdater.allApplicationCommands.toApplicationCommandMap())

            firstGuildUpdates.add(guild.idLong)
            return CommandUpdateResult(guild, hasUpdated, failedDeclarations)
        }
    }

    private fun setMetadata(updater: ApplicationCommandsUpdater) {
        updater.metadata.forEach { metadata ->
            val command = updater.allApplicationCommands.find { it.name == metadata.name }
                ?: throwInternal("Could not match JDA command '${metadata.name}'")

            val accessor = command as? TopLevelApplicationCommandMetadataAccessor
                ?: throwInternal("${command.javaClass.simpleNestedName} must implement ${classRef<TopLevelApplicationCommandMetadataAccessor>()}")

            accessor.metadata = metadata
        }
    }

    private fun getForceString(force: Boolean): String = if (force) " force" else ""

    private fun getCheckTypeString(): String =
        if (context.applicationConfig.onlineAppCommandCheckEnabled) "Online check" else "Local disk check"

    private fun Collection<ApplicationCommandInfo>.toApplicationCommandMap() = MutableApplicationCommandMap.fromCommandList(this)

    private inline fun updateCatching(guild: Guild?, block: () -> CommandUpdateResult) {
        runCatching(block)
            .onSuccess { result ->
                if (result.updateExceptions.isNotEmpty()) {
                    if (guild != null)
                        logger.error { "Errors occurred while registering commands for guild '${guild.name}' (${guild.id})" }
                    else
                        logger.error { "Errors occurred while registering global commands" }

                    result.updateExceptions.forEach { updateException ->
                        logger.error(updateException.throwable) { "Function: ${updateException.function.shortSignature}" }
                    }
                }
            }
            .onFailure {
                if (guild != null)
                    logger.error(it) { "Errors occurred while registering commands for guild '${guild.name}' (${guild.id})" }
                else
                    logger.error(it) { "Errors occurred while registering global commands" }
            }
    }
}