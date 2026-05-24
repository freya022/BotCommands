package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.internal.core.ClassPathProcessor
import io.github.freya022.botcommands.internal.utils.isObject
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.throwInternal
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.kotlinFunction

internal class ServiceProviders : ClassPathProcessor {
    private val nameMap: MutableMap<String, MutableSet<ServiceProvider>> = ConcurrentHashMap()
    private val typeMap: MutableMap<KClass<*>, MutableSet<ServiceProvider>> = ConcurrentHashMap()

    internal val allProviders: Collection<ServiceProvider>
        get() = nameMap.values.flatten()

    internal fun putServiceProvider(serviceProvider: ServiceProvider) {
        nameMap.computeIfAbsent(serviceProvider.name) { ConcurrentSkipListSet() }.add(serviceProvider)
        serviceProvider.types.forEach { type ->
            typeMap.computeIfAbsent(type) { ConcurrentSkipListSet() }.add(serviceProvider)
        }
    }

    internal fun findAllForType(type: KClass<*>): Set<ServiceProvider> = typeMap[type] ?: emptySet()
    internal fun findAllForName(name: String): Set<ServiceProvider> = nameMap[name] ?: emptySet()

    override fun processClass(data: ClassPathProcessor.ClassData) {
        if (!data.isService) return
        if (data.classInfo.isAnnotation) return

        if (data.kClass.isObject) {
            putServiceProvider(ObjectServiceProvider(data.kClass))
        } else {
            putServiceProvider(ClassServiceProvider(data.kClass))
        }
    }

    override fun processMethod(data: ClassPathProcessor.MethodData) {
        if (!data.isServiceFactory) return

        if (data.methodInfo.isConstructor)
            throwArgument("Constructor of ${data.classData.classInfo.simpleName} cannot be annotated with a service annotation")
        val method = data.method as Method

        val function =
            data.method.kotlinFunction
                ?: data.classData.kClass.memberProperties.find { it.javaGetter == method }?.getter
                ?: throwInternal("Cannot get KFunction/KProperty.Getter from $method")
        putServiceProvider(FunctionServiceProvider(function))
    }
}
