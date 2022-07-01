package com.freya02.botcommands.commands.internal.application

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.application.IApplicationCommandManager
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.*
import com.freya02.botcommands.core.internal.events.FirstReadyEvent
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

private val LOGGER = Logging.getLogger()

@BService
internal class ApplicationCommandsBuilder(
    private val serviceContainer: ServiceContainer,
    classPathContainer: ClassPathContainer
) {
    private val globalDeclarationFunctions: MutableList<ClassPathFunction> = arrayListOf()
    private val guildDeclarationFunctions: MutableList<ClassPathFunction> = arrayListOf()

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
        LOGGER.debug("Guild ready: ${event.guild}")

        val manager = GuildApplicationCommandManager(context, event.guild)
        guildDeclarationFunctions.forEach { classPathFunction ->
            runDeclarationFunction(classPathFunction, serviceContainer, manager)
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

    @BEventListener
    internal suspend fun onReady(event: FirstReadyEvent, context: BContextImpl) {
        LOGGER.debug("First ready")

        val manager = GlobalApplicationCommandManager(context)
        globalDeclarationFunctions.forEach { classPathFunction ->
            runDeclarationFunction(classPathFunction, serviceContainer, manager)
        }
    }
}