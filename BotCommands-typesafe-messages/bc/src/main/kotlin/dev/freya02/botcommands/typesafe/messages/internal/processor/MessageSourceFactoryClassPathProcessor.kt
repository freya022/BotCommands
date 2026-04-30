package dev.freya02.botcommands.typesafe.messages.internal.processor

import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceFactoryGenerator
import io.github.freya022.botcommands.api.core.service.BCServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceSupplier
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.core.ClassPathProcessor
import kotlin.reflect.KClass

internal object MessageSourceFactoryClassPathProcessor : ClassPathProcessor {

    @Suppress("UNCHECKED_CAST")
    override fun processClass(data: ClassPathProcessor.ClassData) {
        val serviceContainer = data.serviceContainer
        if (serviceContainer !is BCServiceContainer) return

        val classInfo = data.classInfo
        val kClass = data.kClass
        val annotation = classInfo.getAnnotationInfo(MessageSourceFactory::class.java)?.loadClassAndInstantiate() as MessageSourceFactory? ?: return

        require(classInfo.implementsInterface(IMessageSourceFactory::class.java)) {
            "${classInfo.shortQualifiedName} must implement ${IMessageSourceFactory::class.simpleName}"
        }

        val messageSourceFactoryType = kClass as KClass<IMessageSourceFactory<*>>

        val sourceFactoryProvider = MessageSourceFactoryGenerator.createProvider(annotation, messageSourceFactoryType)
        serviceContainer.putSuppliedService(ServiceSupplier(
            primaryType = messageSourceFactoryType,
            additionalTypes = setOf(IMessageSourceFactory::class),
            annotations = messageSourceFactoryType.annotations,
        ) { context ->
            sourceFactoryProvider.get(context)
        })
    }
}
