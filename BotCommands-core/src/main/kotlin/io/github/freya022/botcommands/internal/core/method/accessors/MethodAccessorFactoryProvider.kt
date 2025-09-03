package io.github.freya022.botcommands.internal.core.method.accessors

import dev.freya02.botcommands.method.accessors.api.annotations.ExperimentalMethodAccessorsApi
import dev.freya02.botcommands.method.accessors.internal.KotlinReflectMethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.MethodAccessorFactory
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KFunction

internal object MethodAccessorFactoryProvider {

    private lateinit var accessorFactory: MethodAccessorFactory
    private val staticAccessors: MutableMap<KFunction<*>, MethodAccessor<*>> = hashMapOf()

    internal fun getAccessorFactory(): MethodAccessorFactory {
        if (::accessorFactory.isInitialized) return accessorFactory

        synchronized(this) {
            if (::accessorFactory.isInitialized) return accessorFactory

            accessorFactory = loadAccessorFactory()
        }

        return accessorFactory
    }

    @OptIn(ExperimentalMethodAccessorsApi::class)
    private fun loadAccessorFactory(): MethodAccessorFactory {
        if (BotCommands.preferClassFileAccessors) {
            val accessorFactory = tryLoadClassFileAccessor()
            if (accessorFactory != null) return accessorFactory
        }

        val logger = KotlinLogging.logger { }
        logger.debug { "Using kotlin-reflect method accessors" }
        return KotlinReflectMethodAccessorFactory()
    }

    private fun tryLoadClassFileAccessor(): MethodAccessorFactory? {
        val logger = KotlinLogging.logger { }

        val runtimeFeature = Runtime.version().feature()
        if (runtimeFeature < 24) {
            logger.debug { "Cannot use ClassFile-based method accessors as this feature requires Java 24+, currently running on Java $runtimeFeature" }
            return null
        }

        val accessorFactory = try {
            Class.forName("dev.freya02.botcommands.method.accessors.internal.ClassFileMethodAccessorFactory")
                .getDeclaredConstructor()
                .newInstance() as MethodAccessorFactory
        } catch (e: ClassNotFoundException) {
            logger.debug(e) { "Cannot use ClassFile-based method accessors as the 'BotCommands-method-accessors-classfile' dependency is missing" }
            return null
        }

        logger.debug { "Using ClassFile-based method accessors" }
        return accessorFactory
    }

    internal fun <R> getStaticAccessor(function: KFunction<R>): MethodAccessor<R> = synchronized(this) {
        @Suppress("UNCHECKED_CAST")
        staticAccessors.getOrPut(function) { getAccessorFactory().create(null, function) } as MethodAccessor<R>
    }
}
