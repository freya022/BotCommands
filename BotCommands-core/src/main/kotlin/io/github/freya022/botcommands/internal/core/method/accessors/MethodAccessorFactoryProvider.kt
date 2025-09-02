package io.github.freya022.botcommands.internal.core.method.accessors

import dev.freya02.botcommands.method.accessors.api.annotations.ExperimentalMethodAccessorsApi
import dev.freya02.botcommands.method.accessors.internal.ClassFileMethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.KotlinReflectMethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.MethodAccessorFactory
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KFunction

internal object MethodAccessorFactoryProvider {

    private var logged = false

    private val kotlinReflectAccessorFactory: MethodAccessorFactory = KotlinReflectMethodAccessorFactory()
    private val classFileAccessorFactory: MethodAccessorFactory? by lazy {
        if (Runtime.version().feature() >= 24) {
            ClassFileMethodAccessorFactory()
        } else {
            null
        }
    }

    private val staticAccessors: MutableMap<KFunction<*>, MethodAccessor<*>> = hashMapOf()

    @OptIn(ExperimentalMethodAccessorsApi::class)
    internal fun getAccessorFactory(): MethodAccessorFactory {
        fun logUsage(msg: String) {
            if (logged) return
            synchronized(this) {
                if (logged) return
                logged = true
            }
            KotlinLogging.logger { }.info { msg }
        }

        return if (BotCommands.preferClassFileAccessors && classFileAccessorFactory != null) {
            logUsage("Using ClassFile-based method accessor factory")
            classFileAccessorFactory!!
        } else {
            logUsage("Using kotlin-reflect method accessor factory")
            kotlinReflectAccessorFactory
        }
    }

    internal fun <R> getStaticAccessor(function: KFunction<R>): MethodAccessor<R> = synchronized(this) {
        @Suppress("UNCHECKED_CAST")
        staticAccessors.getOrPut(function) { getAccessorFactory().create(null, function) } as MethodAccessor<R>
    }
}
