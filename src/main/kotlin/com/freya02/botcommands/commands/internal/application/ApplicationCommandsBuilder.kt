package com.freya02.botcommands.commands.internal.application

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.application.IApplicationCommandManager
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.*
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.ApplicationCommandInfoMap
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
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
    private val globalDeclarationFunctions: MutableList<ClassPathFunction> = arrayListOf()
    private val guildDeclarationFunctions: MutableList<ClassPathFunction> = arrayListOf()

    private val mutex = Mutex()
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
        mutex.withLock {
            val isFirstRun = synchronized(this) {
                if (init) return@synchronized false
                init = true

                true
            }

            if (isFirstRun) {
                onFirstRun(context, event.jda)
            }

            LOGGER.debug("Guild ready: ${event.guild}")

            //TODO check if guild is in slash guild IDs, if not and not empty then skip
            val manager = GuildApplicationCommandManager(context, event.guild)
            guildDeclarationFunctions.forEach { classPathFunction ->
                runDeclarationFunction(classPathFunction, serviceContainer, manager)
            }

            try {
                val guildUpdater = ApplicationCommandsUpdaterKt.ofGuild(context, event.guild, manager)
                if (guildUpdater.shouldUpdateCommands()) {
                    guildUpdater.updateCommands()
                    LOGGER.debug("Guild '${event.guild.name}' (${event.guild.id}) commands were updated ({})", getCheckTypeString())
                } else {
                    LOGGER.debug(
                        "Guild '${event.guild.name}' (${event.guild.id}) commands does not have to be updated ({})",
                        getCheckTypeString()
                    )
                }

                context.applicationCommandsContext.putLiveApplicationCommandsMap(
                    event.guild,
                    ApplicationCommandInfoMap.fromCommandList(guildUpdater.guildApplicationCommands)
                )
            } catch (e: Exception) {
                LOGGER.error("An error occurred while updating global commands", e)
            }
        }
    }

    private fun getCheckTypeString(): String =
        if (context.isOnlineAppCommandCheckEnabled) "Online check" else "Local disk check"

    private suspend fun onFirstRun(context: BContextImpl, jda: JDA) {
        LOGGER.debug("First ready")

        jda.setRequiredScopes("applications.commands")

        context.serviceContainer.putService(ApplicationCommandsCacheKt(jda))

        val manager = GlobalApplicationCommandManager(context)
        globalDeclarationFunctions.forEach { classPathFunction ->
            runDeclarationFunction(classPathFunction, serviceContainer, manager)
        }

        try {
            val globalUpdater = ApplicationCommandsUpdaterKt.ofGlobal(context, manager)
            if (globalUpdater.shouldUpdateCommands()) {
                globalUpdater.updateCommands()
                LOGGER.debug("Global commands were updated ({})", getCheckTypeString())
            } else {
                LOGGER.debug(
                    "Global commands does not have to be updated ({})",
                    getCheckTypeString()
                )
            }

            context.applicationCommandsContext.putLiveApplicationCommandsMap(
                null,
                ApplicationCommandInfoMap.fromCommandList(globalUpdater.guildApplicationCommands)
            )
        } catch (e: Exception) {
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