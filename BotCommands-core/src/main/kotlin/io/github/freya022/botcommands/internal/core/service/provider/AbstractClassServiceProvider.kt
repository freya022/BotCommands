package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.service.ServiceSupplier
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.service.annotations.Primary
import io.github.freya022.botcommands.api.core.utils.getAllAnnotations
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

internal abstract class AbstractClassServiceProvider internal constructor(
    protected val clazz: KClass<*>
) : ServiceProvider {
    override val annotations = clazz.getAllAnnotations()
    override val name = getServiceName(clazz)
    override val providerKey = clazz.jvmName
    override val primaryType get() = clazz
    override val types = getServiceTypes(primaryType)
    override val isPrimary = hasAnnotation<Primary>()
    override val isLazy = hasAnnotation<Lazy>()
    override val priority = getAnnotatedServicePriority()

    override fun toString() = providerKey
}

@PublishedApi
internal fun ServiceProvider.getServiceName(clazz: KClass<*>) =
    getAnnotatedServiceName() ?: ServiceSupplier.defaultName(clazz)
