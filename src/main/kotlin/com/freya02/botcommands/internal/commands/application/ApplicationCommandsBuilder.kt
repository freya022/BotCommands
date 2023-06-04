package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.*
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.autobuilder.ContextCommandAutoBuilder
import com.freya02.botcommands.internal.commands.application.autobuilder.SlashCommandAutoBuilder
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.core.requiredFilter
import com.freya02.botcommands.internal.core.service.ServiceContainerImpl
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
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

    private val guildReadyMutex = Mutex()
    private val globalUpdateMutex = Mutex()
    private val guildUpdateMutexMap: MutableMap<Long, Mutex> = hashMapOf()
    private var init = false

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

        logger.debug("Loaded ${globalDeclarationFunctions.size} global declaration functions and ${guildDeclarationFunctions.size} guild declaration functions")
        if (globalDeclarationFunctions.isNotEmpty()) {
            logger.trace { "Global declaration functions:\n" + globalDeclarationFunctions.joinToString("\n") { it.function.shortSignature } }
        }

        if (guildDeclarationFunctions.isNotEmpty()) {
            logger.trace { "Guild declaration functions:\n" + guildDeclarationFunctions.joinToString("\n") { it.function.shortSignature } }
        }
    }

    @BEventListener
    internal suspend fun onGuildReady(event: GuildReadyEvent) {
        guildReadyMutex.withLock {
            val isFirstRun = synchronized(this) {
                if (init) return@synchronized false
                init = true

                true
            }

            if (isFirstRun) {
                onFirstRun()
            }
        }

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
        logger.error(
            "Encountered an exception while updating commands for guild '{}' ({})",
            guild.name,
            guild.id,
            t
        )
    }

    internal suspend fun updateGlobalCommands(force: Boolean = false): CommandUpdateResult = globalUpdateMutex.withLock {
        val failedDeclarations: MutableList<CommandUpdateException> = arrayListOf()

        val manager = GlobalApplicationCommandManager(context)
        globalDeclarationFunctions.forEach { classPathFunction ->
            runCatching {
                runDeclarationFunction(classPathFunction, manager)
            }.onFailure { failedDeclarations.add(CommandUpdateException(classPathFunction.function, it)) }
        }

        val globalUpdater = ApplicationCommandsUpdater.ofGlobal(context, manager)
        val needsUpdate = force || globalUpdater.shouldUpdateCommands()
        if (needsUpdate) {
            globalUpdater.updateCommands()
            logger.debug("Global commands were{} updated ({})", getForceString(force), getCheckTypeString())
        } else {
            logger.debug("Global commands does not have to be updated ({})", getCheckTypeString())
        }

        applicationCommandsContext.putLiveApplicationCommandsMap(null, globalUpdater.applicationCommands.toApplicationCommandMap())

        return CommandUpdateResult(null, needsUpdate, failedDeclarations)
    }

    internal suspend fun updateGuildCommands(guild: Guild, force: Boolean = false): CommandUpdateResult {
        val slashGuildIds = context.applicationConfig.slashGuildIds
        if (slashGuildIds.isNotEmpty()) {
            if (guild.idLong in slashGuildIds) {
                return CommandUpdateResult(guild, false, listOf())
            }
        }

        synchronized(guildUpdateMutexMap) {
            guildUpdateMutexMap.computeIfAbsent(guild.idLong) { Mutex() }
        }.withLock {
            val failedDeclarations: MutableList<CommandUpdateException> = arrayListOf()

            val manager = GuildApplicationCommandManager(context, guild)
            guildDeclarationFunctions.forEach { classPathFunction ->
                runCatching {
                    runDeclarationFunction(classPathFunction, manager)
                }.onFailure { failedDeclarations.add(CommandUpdateException(classPathFunction.function, it)) }
            }

            val guildUpdater = ApplicationCommandsUpdater.ofGuild(context, guild, manager)
            val needsUpdate = force || guildUpdater.shouldUpdateCommands()
            if (needsUpdate) {
                guildUpdater.updateCommands()
                logger.debug(
                    "Guild '${guild.name}' (${guild.id}) commands were{} updated ({})",
                    getForceString(force),
                    getCheckTypeString()
                )
            } else {
                logger.debug("Guild '${guild.name}' (${guild.id}) commands does not have to be updated ({})", getCheckTypeString())
            }

            applicationCommandsContext.putLiveApplicationCommandsMap(guild, guildUpdater.applicationCommands.toApplicationCommandMap())

            return CommandUpdateResult(guild, needsUpdate, failedDeclarations)
        }
    }

    private fun getForceString(force: Boolean): String = if (force) " force" else ""

    private fun getCheckTypeString(): String =
        if (context.applicationConfig.onlineAppCommandCheckEnabled) "Online check" else "Local disk check"

    private fun Collection<ApplicationCommandInfo>.toApplicationCommandMap() = MutableApplicationCommandMap.fromCommandList(this)

    private suspend fun onFirstRun() {
        logger.debug("First ready")

        try {
            updateCatching(null) { updateGlobalCommands() }
        } catch (e: Throwable) {
            logger.error("An error occurred while updating global commands", e)
        }
    }

    private inline fun updateCatching(guild: Guild?, block: () -> CommandUpdateResult) {
        block().also { result ->
            if (result.updateExceptions.isNotEmpty()) {
                when {
                    guild != null -> logger.error("Errors occurred while registering commands for guild '{}' ({})", guild.name, guild.id)
                    else -> logger.error("Errors occurred while registering commands:")
                }

                result.updateExceptions.forEach { updateException ->
                    logger.error("Function: {}", updateException.function.shortSignature, updateException.throwable)
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