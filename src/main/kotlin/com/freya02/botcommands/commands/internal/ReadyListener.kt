package com.freya02.botcommands.commands.internal

import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.ServiceContainer
import com.freya02.botcommands.core.internal.annotations.BInternalClass
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

@BInternalClass
internal class ReadyListener(private val classPathContainer: ClassPathContainer, private val serviceContainer: ServiceContainer) {
    private var ready = false

    @BEventListener
    internal fun onGuildReadyEvent(event: GuildReadyEvent) {
        synchronized(this) {
            if (ready) return
            ready = true

            for (classPathFunction in classPathContainer.functionsWithAnnotation<Declaration>()) {
                val function = classPathFunction.function
                val args = serviceContainer.getParameters(function.valueParameters.map { it.type.jvmErasure }).toTypedArray()
                function.call(classPathFunction.instance, *args)
            }
        }
    }
}