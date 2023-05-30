package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.InstanceSupplier
import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.internal.core.ServiceAnnotationsMap
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
    override val serviceAnnotations: MutableSet<KClass<out Annotation>> = hashSetOf(BService::class)

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

    fun registerCommand(annotationReceiver: KClass<*>) {
        //TODO rename to Command, limit to Class
        _serviceAnnotationsMap.put(annotationReceiver, CommandMarker::class, CommandMarker())
    }

    //TODO registerResolver
    //TODO registerResolverFactory

    @JvmSynthetic
    internal fun build() = object : BServiceConfig {
        override val serviceAnnotations = this@BServiceConfigBuilder.serviceAnnotations.toImmutableSet()
        override val serviceAnnotationsMap = this@BServiceConfigBuilder.serviceAnnotationsMap //Already immutable
        override val instanceSupplierMap = this@BServiceConfigBuilder.instanceSupplierMap //Already immutable
    }
}
