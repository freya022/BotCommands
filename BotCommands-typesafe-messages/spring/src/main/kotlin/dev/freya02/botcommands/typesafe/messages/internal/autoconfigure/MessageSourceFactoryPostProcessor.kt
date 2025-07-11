package dev.freya02.botcommands.typesafe.messages.internal.autoconfigure

import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceFactoryGenerator
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import kotlin.reflect.KClass

internal class MessageSourceFactoryPostProcessor internal constructor(
    private val context: ApplicationContext,
) : BeanDefinitionRegistryPostProcessor {

    @Suppress("UNCHECKED_CAST")
    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val provider = object : ClassPathScanningCandidateComponentProvider(false) {
            override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
                return beanDefinition.metadata.isIndependent
            }
        }
        provider.addIncludeFilter(AnnotationTypeFilter(MessageSourceFactory::class.java, true, true))

        for (pkg in AutoConfigurationPackages.get(context)) {
            for (beanDefinition in provider.findCandidateComponents(pkg)) {
                val messageSourceFactoryType = Class.forName(beanDefinition.beanClassName).kotlin as KClass<IMessageSourceFactory<*>>

                val annotation = messageSourceFactoryType.findAnnotationRecursive<MessageSourceFactory>()
                    ?: error("Filter for MessageSourceFactory found a class without it")

                require(messageSourceFactoryType.isSubclassOf<IMessageSourceFactory<*>>()) {
                    "${messageSourceFactoryType.shortQualifiedName} must implement ${IMessageSourceFactory::class.simpleName}"
                }

                val sourceFactoryProvider = MessageSourceFactoryGenerator.createProvider(
                    annotation.bundleName,
                    messageSourceFactoryType,
                )
                registry.registerBeanDefinition(
                    messageSourceFactoryType.java.simpleName.replaceFirstChar { it.lowercaseChar() },
                    BeanDefinitionBuilder.genericBeanDefinition(messageSourceFactoryType.java) {
                        sourceFactoryProvider.get(context.getBean<BContext>())
                    }.beanDefinition
                )
            }
        }
    }
}
