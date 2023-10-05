package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.service.InstanceSupplier
import io.github.freya022.botcommands.api.core.service.ServiceStart
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.toImmutableMap
import io.github.freya022.botcommands.api.core.utils.toImmutableSet
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.service.ServiceAnnotationsMap
import kotlin.reflect.KClass

@InjectedService
interface BServiceConfig {
    val serviceAnnotations: Set<KClass<out Annotation>>
    val serviceAnnotationsMap: Map<KClass<out Annotation>, Map<KClass<*>, Annotation>>
    val instanceSupplierMap: Map<KClass<*>, InstanceSupplier<*>>
}

@ConfigDSL
class BServiceConfigBuilder internal constructor() : BServiceConfig {
    override val serviceAnnotations: MutableSet<KClass<out Annotation>> = hashSetOf(BService::class, Command::class, Resolver::class, ResolverFactory::class, Handler::class)

    private val _serviceAnnotationsMap = ServiceAnnotationsMap()
    override val serviceAnnotationsMap: Map<KClass<out Annotation>, Map<KClass<*>, Annotation>>
        get() = _serviceAnnotationsMap.toImmutableMap()

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

    @JvmOverloads
    fun registerService(annotationReceiver: Class<*>, start: ServiceStart = ServiceStart.DEFAULT, name: String = "", priority: Int = 0) =
        registerService(annotationReceiver.kotlin, start, name, priority)

    @JvmSynthetic
    fun registerService(annotationReceiver: KClass<*>, start: ServiceStart = ServiceStart.DEFAULT, name: String = "", priority: Int = 0) {
        _serviceAnnotationsMap.put(annotationReceiver, BService::class, BService(start, name, priority))
    }

    fun registerCommand(annotationReceiver: Class<*>) =
        registerCommand(annotationReceiver.kotlin)

    @JvmSynthetic
    fun registerCommand(annotationReceiver: KClass<*>) {
        _serviceAnnotationsMap.put(annotationReceiver, Command::class, Command())
    }

    fun registerResolver(annotationReceiver: Class<*>) =
        registerResolver(annotationReceiver.kotlin)

    @JvmSynthetic
    fun registerResolver(annotationReceiver: KClass<*>) {
        _serviceAnnotationsMap.put(annotationReceiver, Resolver::class,
            Resolver()
        )
    }

    fun registerResolverFactory(annotationReceiver: Class<*>) =
        registerResolverFactory(annotationReceiver.kotlin)

    @JvmSynthetic
    fun registerResolverFactory(annotationReceiver: KClass<*>) {
        _serviceAnnotationsMap.put(annotationReceiver, ResolverFactory::class,
            ResolverFactory()
        )
    }

    @JvmSynthetic
    internal fun build() = object : BServiceConfig {
        override val serviceAnnotations = this@BServiceConfigBuilder.serviceAnnotations.toImmutableSet()
        override val serviceAnnotationsMap = this@BServiceConfigBuilder.serviceAnnotationsMap //Already immutable
        override val instanceSupplierMap = this@BServiceConfigBuilder.instanceSupplierMap //Already immutable
    }
}
