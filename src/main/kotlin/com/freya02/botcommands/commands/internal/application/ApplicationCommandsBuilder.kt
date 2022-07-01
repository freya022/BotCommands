package com.freya02.botcommands.commands.internal.application

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.GuildApplicationCommandManager
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.PreboundFunction
import com.freya02.botcommands.core.internal.ServiceContainer
import com.freya02.botcommands.core.internal.annotations.BInternalClass
import com.freya02.botcommands.internal.BContextImpl
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

private val LOGGER = Logging.getLogger()

@BService
@BInternalClass
internal class ApplicationCommandsBuilder(classPathContainer: ClassPathContainer, serviceContainer: ServiceContainer) {
    private val declarationFunctions: MutableList<PreboundFunction> = arrayListOf()

    init {
        for (classPathFunction in classPathContainer.functionsWithAnnotation<Declaration>()) {
            val function = classPathFunction.function
            val args = serviceContainer.getParameters(function.valueParameters.map { it.type.jvmErasure }).toTypedArray()

            declarationFunctions.add(PreboundFunction(classPathFunction, args))
        }
    }

    @BEventListener
    internal fun onGuildReady(event: GuildReadyEvent, context: BContextImpl) {
        LOGGER.debug("Guild ready: ${event.guild}")

        val manager = GuildApplicationCommandManager(context, event.guild)

    }
}