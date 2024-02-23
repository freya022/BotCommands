package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.service.InstanceSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.toImmutableMap
import io.github.freya022.botcommands.api.core.utils.toImmutableSet
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import kotlin.reflect.KClass

@InjectedService
interface BServiceConfig {
    //TODO document - this seems to be mostly used to retain classpath elements
    val serviceAnnotations: Set<KClass<out Annotation>>
    val instanceSupplierMap: Map<KClass<*>, InstanceSupplier<*>>
}

@ConfigDSL
class BServiceConfigBuilder internal constructor() : BServiceConfig {
    override val serviceAnnotations: MutableSet<KClass<out Annotation>> = hashSetOf(BService::class, Command::class, Resolver::class, ResolverFactory::class, Handler::class)

    private val _instanceSupplierMap: MutableMap<KClass<*>, InstanceSupplier<*>> = hashMapOf()
    override val instanceSupplierMap: Map<KClass<*>, InstanceSupplier<*>>
        get() = _instanceSupplierMap.toImmutableMap()

    /**
     * Adds a supplier which returns instances of the specified classes
     *
     * This is used when creating services
     */
    fun <T : Any> registerInstanceSupplier(clazz: Class<T>, instanceSupplier: InstanceSupplier<T>) {
        _instanceSupplierMap[clazz.kotlin] = instanceSupplier
    }

    @JvmSynthetic
    internal fun build() = object : BServiceConfig {
        override val serviceAnnotations = this@BServiceConfigBuilder.serviceAnnotations.toImmutableSet()
        override val instanceSupplierMap = this@BServiceConfigBuilder.instanceSupplierMap //Already immutable
    }
}
