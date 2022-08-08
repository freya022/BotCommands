package com.freya02.botcommands.commands.internal.application

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.CommandUpdateResult
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.application.IApplicationCommandManager
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.*
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.application.MutableApplicationCommandMap
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import com.freya02.botcommands.internal.utils.Utils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.requests.ErrorResponse
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

private val LOGGER = Logging.getLogger()

@BService
internal class ApplicationCommandsBuilder(
    private val context: BContextImpl,
    private val serviceContainer: ServiceContainer,
    classPathContainer: ClassPathContainer
) {
    private val applicationCommandsContext = context.applicationCommandsContext

    private val globalDeclarationFunctions: MutableList<ClassPathFunction> = arrayListOf()
    private val guildDeclarationFunctions: MutableList<ClassPathFunction> = arrayListOf()

    private val guildReadyMutex = Mutex()
    private val guildUpdateMutexMap: MutableMap<Long, Mutex> = hashMapOf()
    private var init = false

    init {
        for (classPathFunction in classPathContainer
            .functionsWithAnnotation<Declaration>()
            .requireNonStatic()
            .requireFirstArg(GlobalApplicationCommandManager::class, GuildApplicationCommandManager::class)
        ) {
            when (classPathFunction.function.valueParameters.first().type.jvmErasure) {
                GlobalApplicationCommandManager::class -> globalDeclarationFunctions.add(classPathFunction)
                GuildApplicationCommandManager::class -> guildDeclarationFunctions.add(classPathFunction)
                else -> throwInternal("Function first param should have been checked")
            }
        }

        LOGGER.debug("Loaded ${globalDeclarationFunctions.size} global declaration functions and ${guildDeclarationFunctions.size} guild declaration functions")
        if (globalDeclarationFunctions.isNotEmpty()) {
            LOGGER.trace("Global declaration functions:\n" + globalDeclarationFunctions.joinToString("\n") { it.function.shortSignature })
        }

        if (guildDeclarationFunctions.isNotEmpty()) {
            LOGGER.trace("Guild declaration functions:\n" + guildDeclarationFunctions.joinToString("\n") { it.function.shortSignature })
        }
    }

    @BEventListener
    internal suspend fun onGuildReady(event: GuildReadyEvent, context: BContextImpl) {
        guildReadyMutex.withLock {
            val isFirstRun = synchronized(this) {
                if (init) return@synchronized false
                init = true

                true
            }

            if (isFirstRun) {
                onFirstRun(context, event.jda)
            }
        }

        val guild = event.guild
        LOGGER.debug("Guild ready: $guild")

        try {
            updateGuildCommands(guild)
        } catch (t: Throwable) {
            handleGuildCommandUpdateException(guild, t)
        }
    }

    internal fun handleGuildCommandUpdateException(guild: Guild, t: Throwable) {
        val e = Utils.getErrorResponseException(t)

        if (e != null && e.errorResponse == ErrorResponse.MISSING_ACCESS) {
            val inviteUrl = "${guild.jda.getInviteUrl()}&guild_id=${guild.id}"
            LOGGER.warn(
                "Could not register guild commands for guild '{}' ({}) as it appears the OAuth2 grants misses applications.commands, you can re-invite the bot in this guild with its already existing permission with this link: {}",
                guild.name,
                guild.id,
                inviteUrl
            )

            //TODO missing access event
        } else {
            LOGGER.error(
                "Encountered an exception while updating commands for guild '{}' ({})",
                guild.name,
                guild.id,
                t
            )
        }
    }

    internal suspend fun updateGlobalCommands(force: Boolean = false): CommandUpdateResult {
        val manager = GlobalApplicationCommandManager(context)
        globalDeclarationFunctions.forEach { classPathFunction ->
            runDeclarationFunction(classPathFunction, serviceContainer, manager)
        }

        val globalUpdater = ApplicationCommandsUpdaterKt.ofGlobal(context, manager)
        val needsUpdate = force || globalUpdater.shouldUpdateCommands()
        if (needsUpdate) {
            globalUpdater.updateCommands()
            LOGGER.debug("Global commands were{} updated ({})", getForceString(force), getCheckTypeString())
        } else {
            LOGGER.debug("Global commands does not have to be updated ({})", getCheckTypeString())
        }

        applicationCommandsContext.putLiveApplicationCommandsMap(null, globalUpdater.guildApplicationCommands.toApplicationCommandMap())

        return CommandUpdateResult(null, needsUpdate)
    }

    internal suspend fun updateGuildCommands(guild: Guild, force: Boolean = false): CommandUpdateResult {
        guildUpdateMutexMap.computeIfAbsent(guild.idLong) { Mutex() }.withLock {
            //TODO check if guild is in slash guild IDs, if not and not empty then skip
            val manager = GuildApplicationCommandManager(context, guild)
            guildDeclarationFunctions.forEach { classPathFunction ->
                runDeclarationFunction(classPathFunction, serviceContainer, manager)
            }

            val guildUpdater = ApplicationCommandsUpdaterKt.ofGuild(context, guild, manager)
            val needsUpdate = force || guildUpdater.shouldUpdateCommands()
            if (needsUpdate) {
                guildUpdater.updateCommands()
                LOGGER.debug("Guild '${guild.name}' (${guild.id}) commands were{} updated ({})", getForceString(force), getCheckTypeString())
            } else {
                LOGGER.debug("Guild '${guild.name}' (${guild.id}) commands does not have to be updated ({})", getCheckTypeString())
            }

            applicationCommandsContext.putLiveApplicationCommandsMap(guild, guildUpdater.guildApplicationCommands.toApplicationCommandMap())

            return CommandUpdateResult(guild, needsUpdate)
        }
    }

    private fun getForceString(force: Boolean): String = if (force) " force" else ""

    private fun getCheckTypeString(): String =
        if (context.isOnlineAppCommandCheckEnabled) "Online check" else "Local disk check"

    private fun List<ApplicationCommandInfo>.toApplicationCommandMap() = MutableApplicationCommandMap.fromCommandList(this)

    private suspend fun onFirstRun(context: BContextImpl, jda: JDA) {
        LOGGER.debug("First ready") //TODO runInitialization ? (+ exit if error ?)

        jda.setRequiredScopes("applications.commands")

        context.serviceContainer.putService(ApplicationCommandsCacheKt(jda))

        try {
            updateGlobalCommands()
        } catch (e: Throwable) {
            LOGGER.error("An error occurred while updating global commands", e)
        }
    }

    private suspend fun runDeclarationFunction(
        classPathFunction: ClassPathFunction,
        serviceContainer: ServiceContainer,
        manager: IApplicationCommandManager
    ) {
        val function = classPathFunction.function
        val args = serviceContainer.getParameters(
            function.nonInstanceParameters.map { it.type.jvmErasure },
            mapOf(manager::class to manager)
        ).toTypedArray()

        function.callSuspend(classPathFunction.instance, *args)
    }
}