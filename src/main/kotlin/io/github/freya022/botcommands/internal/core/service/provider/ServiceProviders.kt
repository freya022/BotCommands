package io.github.freya022.botcommands.internal.core.service.provider

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.core.config.BServiceConfig
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.throwInternal
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.kotlinFunction

internal class ServiceProviders(private val serviceConfig: BServiceConfig) : ClassGraphProcessor {
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

    override fun processClass(classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
        val instanceSupplier = serviceConfig.instanceSupplierMap[kClass]
        if (instanceSupplier != null) {
            putServiceProvider(SuppliedServiceProvider(kClass, instanceSupplier))
            return
        }

        if (!isService) return

        putServiceProvider(ClassServiceProvider(kClass))
    }

    override fun processMethod(
        methodInfo: MethodInfo,
        method: Executable,
        classInfo: ClassInfo,
        kClass: KClass<*>,
        isServiceFactory: Boolean
    ) {
        if (!isServiceFactory) return

        if (methodInfo.isConstructor)
            throwArgument("Constructor of ${classInfo.simpleName} cannot be annotated with a service annotation")
        method as Method

        val function =
            method.kotlinFunction
                ?: kClass.memberProperties.find { it.javaGetter == method }?.getter
                ?: throwInternal("Cannot get KFunction/KProperty.Getter from $method")
        putServiceProvider(FunctionServiceProvider(function))
    }

    override fun postProcess() {
        val missingSuppliedProviders = serviceConfig.instanceSupplierMap.keys - typeMap.keys
        check(missingSuppliedProviders.isEmpty()) {
            "Some instance suppliers were registered but their class were not in the search path:\n${missingSuppliedProviders.joinAsList { it.shortQualifiedName } }"
        }
    }
}