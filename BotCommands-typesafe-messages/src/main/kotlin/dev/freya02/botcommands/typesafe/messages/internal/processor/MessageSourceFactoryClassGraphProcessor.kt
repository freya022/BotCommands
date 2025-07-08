package dev.freya02.botcommands.typesafe.messages.internal.processor

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceFactoryGenerator
import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.service.BCServiceContainer
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceSupplier
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.utils.superErasureAt
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

internal object MessageSourceFactoryClassGraphProcessor : ClassGraphProcessor {

    @Suppress("UNCHECKED_CAST")
    override fun processClass(serviceContainer: ServiceContainer, classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
        if (serviceContainer !is BCServiceContainer) return

        val annotation = classInfo.getAnnotationInfo(MessageSourceFactory::class.java)?.loadClassAndInstantiate() as MessageSourceFactory? ?: return

        require(classInfo.isInterface) {
            "${classInfo.shortQualifiedName} must be an interface"
        }
        require(classInfo.implementsInterface(IMessageSourceFactory::class.java)) {
            "${classInfo.shortQualifiedName} must implement ${IMessageSourceFactory::class.simpleName}"
        }

        val messageSourceFactoryType = kClass as KClass<IMessageSourceFactory<*>>
        val messageSourceType = kClass.superErasureAt<IMessageSourceFactory<*>>(0).jvmErasure as KClass<IMessageSource>

        serviceContainer.putSuppliedService(ServiceSupplier(messageSourceFactoryType) { context ->
            MessageSourceFactoryGenerator.createFactory(
                context,
                annotation,
                messageSourceFactoryType,
                messageSourceType
            )
        })
    }
}
