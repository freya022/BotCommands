package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.*
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.declaration.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.declaration.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.declaration.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.internal.commands.application.autobuilder.ContextCommandAutoBuilder
import io.github.freya022.botcommands.internal.commands.application.autobuilder.SlashCommandAutoBuilder
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.ServiceContainerImpl
import io.github.freya022.botcommands.internal.core.service.getParameters
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.reference
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

@BService
internal class ApplicationCommandsBuilder(
    private val context: BContextImpl,
    private val serviceContainer: ServiceContainerImpl
) {
    private val logger = KotlinLogging.logger {  }

    private val applicationCommandsContext = context.applicationCommandsContext

    private val globalDeclarationFunctions: MutableList<ClassPathFunction> = arrayListOf()
    private val guildDeclarationFunctions: MutableList<ClassPathFunction> = arrayListOf()

    private val globalUpdateMutex = Mutex()
    private val guildUpdateGlobalMutex: Mutex = Mutex()
    private val guildUpdateMutexMap: MutableMap<Long, Mutex> = hashMapOf()

    private var firstGlobalUpdate = true
    private val firstGuildUpdates = hashSetOf<Long>()

    init {
        val slashCommandAutoBuilder = serviceContainer.getService<SlashCommandAutoBuilder>()
        globalDeclarationFunctions += ClassPathFunction(slashCommandAutoBuilder, SlashCommandAutoBuilder::declareGlobal)
        guildDeclarationFunctions += ClassPathFunction(slashCommandAutoBuilder, SlashCommandAutoBuilder::declareGuild)

        val contextCommandAutoBuilder = serviceContainer.getService<ContextCommandAutoBuilder>()
        globalDeclarationFunctions += ClassPathFunction(contextCommandAutoBuilder, ContextCommandAutoBuilder::declareGlobalMessage)
        globalDeclarationFunctions += ClassPathFunction(contextCommandAutoBuilder, ContextCommandAutoBuilder::declareGlobalUser)
        guildDeclarationFunctions += ClassPathFunction(contextCommandAutoBuilder, ContextCommandAutoBuilder::declareGuildMessage)
        guildDeclarationFunctions += ClassPathFunction(contextCommandAutoBuilder, ContextCommandAutoBuilder::declareGuildUser)

        context.instantiableServiceAnnotationsMap
            .getInstantiableFunctionsWithAnnotation<Command, AppDeclaration>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(GlobalApplicationCommandManager::class, GuildApplicationCommandManager::class))
            .forEach { classPathFunction ->
                when (classPathFunction.function.valueParameters.first().type.jvmErasure) {
                    GlobalApplicationCommandManager::class -> globalDeclarationFunctions.add(classPathFunction)
                    GuildApplicationCommandManager::class -> guildDeclarationFunctions.add(classPathFunction)
                    else -> throwInternal("Function first param should have been checked")
                }
            }

        logger.debug { "Loaded ${globalDeclarationFunctions.size} global declaration functions and ${guildDeclarationFunctions.size} guild declaration functions" }
        if (globalDeclarationFunctions.isNotEmpty()) {
            logger.trace { "Global declaration functions:\n" + globalDeclarationFunctions.joinAsList { it.function.shortSignature } }
        }

        if (guildDeclarationFunctions.isNotEmpty()) {
            logger.trace { "Guild declaration functions:\n" + guildDeclarationFunctions.joinAsList { it.function.shortSignature } }
        }
    }

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
        globalDeclarationFunctions.forEach { classPathFunction ->
            runCatching {
                runDeclarationFunction(classPathFunction, manager)
            }.onFailure { failedDeclarations.add(CommandUpdateException(classPathFunction.function, it)) }
        }

        if (failedDeclarations.isNotEmpty() && firstGlobalUpdate) {
            logger.error { "An exception occurred while updating global commands on startup, aborting any update" }
            return CommandUpdateResult(null, false, failedDeclarations)
        }

        val globalUpdater = ApplicationCommandsUpdater.ofGlobal(context, manager)
        val needsUpdate = force || globalUpdater.shouldUpdateCommands()
        if (needsUpdate) {
            globalUpdater.updateCommands()
            logger.debug { "Global commands were${getForceString(force)} updated (${getCheckTypeString()})" }
        } else {
            logger.debug { "Global commands does not have to be updated, ${globalUpdater.filteredCommandsCount} were kept (${getCheckTypeString()})" }
        }

        applicationCommandsContext.putLiveApplicationCommandsMap(null, globalUpdater.applicationCommands.toApplicationCommandMap())

        firstGlobalUpdate = false
        return CommandUpdateResult(null, needsUpdate, failedDeclarations)
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
            guildDeclarationFunctions.forEach { classPathFunction ->
                runCatching {
                    runDeclarationFunction(classPathFunction, manager)
                }.onFailure { failedDeclarations.add(CommandUpdateException(classPathFunction.function, it)) }
            }

            if (failedDeclarations.isNotEmpty() && guild.idLong !in firstGuildUpdates) {
                context.dispatchException("An exception occurred while updating commands for '${guild.name}' (${guild.idLong}) on startup", null)
                logger.error { "An exception occurred while updating commands for '${guild.name}' (${guild.idLong}) on startup, aborting any update" }
                return CommandUpdateResult(guild, false, failedDeclarations)
            }

            val guildUpdater = ApplicationCommandsUpdater.ofGuild(context, guild, manager)
            val needsUpdate = force || guildUpdater.shouldUpdateCommands()
            if (needsUpdate) {
                guildUpdater.updateCommands()
                logger.debug { "Guild '${guild.name}' (${guild.id}) commands were${getForceString(force)} updated (${getCheckTypeString()})" }
            } else {
                logger.debug { "Guild '${guild.name}' (${guild.id}) commands does not have to be updated, ${guildUpdater.filteredCommandsCount} were kept (${getCheckTypeString()})" }
            }

            applicationCommandsContext.putLiveApplicationCommandsMap(guild, guildUpdater.applicationCommands.toApplicationCommandMap())

            firstGuildUpdates.add(guild.idLong)
            return CommandUpdateResult(guild, needsUpdate, failedDeclarations)
        }
    }

    private fun getForceString(force: Boolean): String = if (force) " force" else ""

    private fun getCheckTypeString(): String =
        if (context.applicationConfig.onlineAppCommandCheckEnabled) "Online check" else "Local disk check"

    private fun Collection<ApplicationCommandInfo>.toApplicationCommandMap() = MutableApplicationCommandMap.fromCommandList(this)

    private inline fun updateCatching(guild: Guild?, block: () -> CommandUpdateResult) {
        block().also { result ->
            if (result.updateExceptions.isNotEmpty()) {
                when {
                    guild != null -> logger.error { "Errors occurred while registering commands for guild '${guild.name}' (${guild.id})" }
                    else -> logger.error { "Errors occurred while registering commands:" }
                }

                result.updateExceptions.forEach { updateException ->
                    logger.error(updateException.throwable) { "Function: ${updateException.function.shortSignature}" }
                }
            }
        }
    }

    private suspend fun runDeclarationFunction(classPathFunction: ClassPathFunction, manager: AbstractApplicationCommandManager) {
        val (instance, function) = classPathFunction
        val args = serviceContainer.getParameters(function.nonInstanceParameters.drop(1).map { it.type.jvmErasure }).toTypedArray()
        function.callSuspend(instance, manager, *args)
    }
}