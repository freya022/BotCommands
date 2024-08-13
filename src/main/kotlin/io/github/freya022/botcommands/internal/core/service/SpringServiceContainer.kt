package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType
import io.github.freya022.botcommands.api.core.service.ServiceResult
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import org.springframework.beans.factory.BeanCurrentlyInCreationException
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.AnnotationBeanNameGenerator
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
internal class SpringServiceContainer internal constructor(private val applicationContext: ConfigurableApplicationContext) : ServiceContainer {
    override fun <T : Any> tryGetService(name: String, requiredType: KClass<T>): ServiceResult<T> {
        if (applicationContext.containsBean(name)) {
            return ServiceResult.pass(applicationContext.getBean(name, requiredType.java))
        }

        return ErrorType.UNKNOWN.toResult("Spring said no for ${requiredType.simpleNestedName} and $name")
    }

    override fun <T : Any> tryGetService(clazz: KClass<T>): ServiceResult<T> {
        val provider = applicationContext.getBeanProvider(clazz.java, false)
        return provider.ifAvailable?.let(ServiceResult.Companion::pass) ?:
            ErrorType.UNKNOWN.toResult("Spring said no for ${clazz.simpleNestedName}")
    }

    override fun canCreateService(name: String, requiredType: KClass<*>): ServiceError? {
        if (applicationContext.containsBean(name)) {
            if (applicationContext.isTypeMatch(name, requiredType.java)) {
                return null
            }
        }

        return ErrorType.UNKNOWN.toError("Spring said no for ${requiredType.simpleNestedName} and $name")
    }

    override fun canCreateService(clazz: KClass<*>): ServiceError? {
        try {
            if (applicationContext.getBeanProvider(clazz.java, false).ifAvailable != null) {
                return null
            }
        } catch (e: BeanCurrentlyInCreationException) {
            return null // All good
        }

        return ErrorType.UNKNOWN.toError("Spring said no for ${clazz.simpleNestedName}")
    }

    override fun <T : Any> peekServiceOrNull(name: String, requiredType: KClass<T>): T? {
        if (applicationContext.containsBeanDefinition(name)) {
            return try {
                applicationContext.getBeanProvider(requiredType.java, false).ifAvailable
            } catch (e: BeanCurrentlyInCreationException) {
                null // All good
            }
        }

        return null
    }

    override fun <T : Any> peekServiceOrNull(clazz: KClass<T>): T? {
        applicationContext.getBeanNamesForType(clazz.java).forEach { name ->
            peekServiceOrNull(name, clazz)?.let { return it }
        }

        return null
    }

    override fun getServiceNamesForAnnotation(annotationType: KClass<out Annotation>): List<String> {
        return applicationContext.getBeanNamesForAnnotation(annotationType.java).asList().unmodifiableView()
    }

    override fun <A : Annotation> findAnnotationOnService(name: String, annotationType: KClass<A>): A? {
        return applicationContext.findAnnotationOnBean(name, annotationType.java)
    }

    override fun <T : Any> getInterfacedServiceTypes(clazz: KClass<T>): List<KClass<T>> {
        return getInterfacedServices(clazz).map { it::class as KClass<T> }
    }

    override fun <T : Any> getInterfacedServices(clazz: KClass<T>): List<T> {
        return applicationContext.getBeanProvider(clazz.java).toList()
    }

    override fun <T : Any> putServiceAs(t: T, clazz: Class<out T>) = putService(t)

    override fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>) = putService(t)

    override fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>, name: String) = putService(t, name)

    override fun putService(t: Any, name: String) {
        applicationContext.beanFactory.registerSingleton(name, t)
    }

    override fun putService(t: Any) {
        val generatedBeanName = AnnotationBeanNameGenerator.INSTANCE
            .generateBeanName(
                AnnotatedGenericBeanDefinition(t.javaClass),
                applicationContext.beanFactory as BeanDefinitionRegistry
            )
        applicationContext.beanFactory.registerSingleton(generatedBeanName, t)
    }
}