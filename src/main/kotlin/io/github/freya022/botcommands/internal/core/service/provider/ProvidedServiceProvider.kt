package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.service.DefaultServiceContainerImpl
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmName

internal class ProvidedServiceProvider internal constructor(
    override val instance: Any,
    override val primaryType: KClass<*>,
    override val name: String,
    override val isPrimary: Boolean,
    override val priority: Int,
    override val annotations: Collection<Annotation>,
    typeAliases: Set<KClass<*>>
) : ServiceProvider {
    init {
        if (!primaryType.isInstance(instance))
            throwArgument("${instance.javaClass.name} is not an instance of ${primaryType.jvmName}")

        require(primaryType !in typeAliases) {
            "Primary type ${primaryType.simpleNestedName} cannot be a type alias at the same time"
        }

        typeAliases.forEach {
            if (!it.java.isInstance(instance)) {
                throwArgument("Type alias ${it.simpleNestedName} must be a subclass of ${instance.javaClass.name}")
            }
        }
    }

    private val clazz = instance::class

    override val providerKey = clazz.jvmName
    override val types: Set<KClass<*>> = typeAliases + primaryType
    override val isLazy get() = false

    override fun getProviderFunction(): KFunction<*>? =
        clazz.constructors.firstOrNull()

    override fun getProviderSignature(): String = "<provided ${clazz.shortQualifiedName}>"

    override fun canInstantiate(serviceContainer: DefaultServiceContainerImpl): ServiceError? {
        return null
    }

    override fun createInstance(serviceContainer: DefaultServiceContainerImpl): TimedInstantiation {
        throwInternal("Tried to create an instance of ${clazz.jvmName} when one already exists, instance should be retrieved manually beforehand")
    }
}