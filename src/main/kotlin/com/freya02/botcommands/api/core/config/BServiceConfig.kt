package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.InstanceSupplier
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.core.service.ServiceStart
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.core.service.annotations.ResolverFactory
import com.freya02.botcommands.internal.core.service.ServiceAnnotationsMap
import com.freya02.botcommands.internal.toImmutableMap
import com.freya02.botcommands.internal.toImmutableSet
import kotlin.reflect.KClass

@InjectedService
interface BServiceConfig {
    val serviceAnnotations: Set<KClass<out Annotation>>
    val serviceAnnotationsMap: Map<KClass<out Annotation>, Map<KClass<*>, Annotation>>
    val instanceSupplierMap: Map<KClass<*>, InstanceSupplier<*>>
}

class BServiceConfigBuilder internal constructor() : BServiceConfig {
    override val serviceAnnotations: MutableSet<KClass<out Annotation>> = hashSetOf(BService::class, Command::class, Resolver::class, ResolverFactory::class)

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
    fun registerService(annotationReceiver: KClass<*>, start: ServiceStart = ServiceStart.DEFAULT, name: String = "") {
        _serviceAnnotationsMap.put(annotationReceiver, BService::class, BService(start, name))
    }

    fun registerCommand(annotationReceiver: KClass<*>) {
        _serviceAnnotationsMap.put(annotationReceiver, Command::class, Command())
    }

    fun registerResolver(annotationReceiver: KClass<*>) {
        _serviceAnnotationsMap.put(annotationReceiver, Resolver::class,
            Resolver()
        )
    }

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
