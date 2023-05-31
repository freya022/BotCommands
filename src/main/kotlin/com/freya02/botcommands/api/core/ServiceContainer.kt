package com.freya02.botcommands.api.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.api.core.config.BServiceConfig
import com.freya02.botcommands.api.core.events.PreloadServiceEvent
import com.freya02.botcommands.api.core.suppliers.annotations.DynamicSupplier
import com.freya02.botcommands.api.core.suppliers.annotations.InstanceSupplier
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.core.*
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionUtils.declaringClass
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonExtensionFunctions
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import com.freya02.botcommands.internal.utils.ReflectionUtils.staticAndCompanionDeclaredMemberFunctions
import com.freya02.botcommands.internal.utils.requiredFilter
import com.freya02.botcommands.internal.utils.withFilter
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.hooks.IEventManager
import java.lang.reflect.Modifier
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.*
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

internal class ServiceCreationStack {
    private val localSet: ThreadLocal<MutableSet<ProviderName>> = ThreadLocal.withInitial { linkedSetOf() }
    private val set get() = localSet.get()

    //If services have circular dependencies during checking, consider it to not be an issue
    internal inline fun <R> withServiceCheckKey(provider: ServiceProvider, block: () -> R): R? {
        if (!set.add(provider.providerKey)) return null
        try {
            return block()
        } finally {
            set.remove(provider.providerKey)
        }
    }

    internal inline fun <R> withServiceCreateKey(provider: ServiceProvider, block: () -> R): R {
        if (!set.add(provider.providerKey))
            throw IllegalStateException("Circular dependency detected, list of the services being created : [${set.joinToString(" -> ")}] ; attempted to create ${provider.providerKey}")
        try {
            return block()
        } finally {
            set.remove(provider.providerKey)
        }
    }
}

data class ServiceResult<T : Any>(val service: T?, val errorMessage: String?) {
    init {
        if (service == null && errorMessage == null) {
            throwInternal("ServiceResult should contain either the service or the error message")
        }
    }

    fun getOrNull(): T? = when {
        service != null -> service
        errorMessage != null -> null
        else -> throwInternal("ServiceResult should contain either the service or the error message")
    }

    fun getOrThrow(): T = when {
        service != null -> service
        errorMessage != null -> throwService(errorMessage)
        else -> throwInternal("ServiceResult should contain either the service or the error message")
    }
}

@InjectedService
interface ServiceContainer {
    fun <T : Any> tryGetService(name: String, requiredType: KClass<T>): ServiceResult<T>
    fun <T : Any> tryGetService(name: String, requiredType: Class<T>): ServiceResult<T> = tryGetService(name, requiredType.kotlin)
    fun <T : Any> tryGetService(clazz: KClass<T>): ServiceResult<T>
    fun <T : Any> tryGetService(clazz: Class<T>): ServiceResult<T> = tryGetService(clazz.kotlin)

    fun <T : Any> getService(name: String, requiredType: KClass<T>): T = tryGetService(name, requiredType).getOrThrow()
    fun <T : Any> getService(name: String, requiredType: Class<T>): T = getService(name, requiredType.kotlin)
    fun <T : Any> getService(clazz: KClass<T>): T = tryGetService(clazz).getOrThrow()
    fun <T : Any> getService(clazz: Class<T>): T = getService(clazz.kotlin)

    fun <T : Any> getServiceOrNull(name: String, requiredType: KClass<T>): T? = tryGetService(name, requiredType).getOrNull()
    fun <T : Any> getServiceOrNull(name: String, requiredType: Class<T>): T? = getServiceOrNull(name, requiredType.kotlin)
    fun <T : Any> getServiceOrNull(clazz: KClass<T>): T? = tryGetService(clazz).getOrNull()
    fun <T : Any> getServiceOrNull(clazz: Class<T>): T? = getServiceOrNull(clazz.kotlin)

    fun <T : Any> peekServiceOrNull(name: String, requiredType: KClass<T>): T?
    fun <T : Any> peekServiceOrNull(name: String, requiredType: Class<T>): T? = peekServiceOrNull(name, requiredType.kotlin)
    fun <T : Any> peekServiceOrNull(clazz: KClass<T>): T?
    fun <T : Any> peekServiceOrNull(clazz: Class<T>): T? = peekServiceOrNull(clazz.kotlin)

    fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>, name: String? = null)
    fun <T : Any> putServiceAs(t: T, clazz: Class<out T>) = putServiceAs(t, clazz.kotlin)
    fun putService(t: Any, name: String?): Unit = putServiceAs(t, t::class, name)
    fun putService(t: Any): Unit = putServiceAs(t, t::class)
}

private val logger = KotlinLogging.logger { }

class ServiceContainerImpl internal constructor(internal val context: BContextImpl) : ServiceContainer {
    private val serviceConfig: BServiceConfig = context.serviceConfig

    private val serviceMap = ServiceMap()

    private val unavailableServices: MutableMap<KClass<*>, String> = hashMapOf() //Must not contain InjectedService(s) !
    private val lock = ReentrantLock()

    private val serviceCreationStack = ServiceCreationStack()

    internal val dynamicSuppliers: List<KFunction<*>> by lazy {
        context.classPathContainer.classes.flatMap { clazz ->
            clazz.nonExtensionFunctions //Companion objects are included in those classes, no need to get them
                .withFilter(FunctionFilter.annotation<DynamicSupplier>())
                .requiredFilter(FunctionFilter.staticOrCompanion())
                .requiredFilter(FunctionFilter.firstArg(Class::class))
                .requiredFilter(FunctionFilter.returnType(Any::class))
        }
    }

    init {
        putService(this)
        putServiceAs<ServiceContainer>(this)
        putService(context)
        putService(context.eventManager)
        putServiceAs<IEventManager>(context.eventManager) //Should be used if JDA is constructed as a service
        putService(context.classPathContainer)
        putServiceAs<BContext>(context)
        putServiceAs(context.config)
    }

    internal fun preloadServices() {
        runBlocking {
            dynamicSuppliers.let {
                if (it.isEmpty()) return@let

                logger.trace {
                    val functionsListStr = it.joinToString("\n\t - ", "\t - ", "") { dynamicSupplierFunction ->
                        dynamicSupplierFunction.shortSignatureNoSrc
                    }
                    "Loaded ${it.size} dynamic suppliers:\n$functionsListStr"
                }
            }

            getService(EventDispatcher::class).dispatchEvent(PreloadServiceEvent())
        }
    }

    internal fun loadServices(loadableServices: Map<ServiceStart, List<KClass<*>>>, requestedStart: ServiceStart) {
        loadableServices[requestedStart]?.forEach { clazz ->
            //Skip classes that have been already loaded/checked
            if (clazz in serviceMap || clazz in unavailableServices) return@forEach

            tryLoadService(clazz).errorMessage?.let { errorMessage ->
                logger.trace { "Service ${clazz.simpleNestedName} not loaded: $errorMessage" }
            }
        }
    }

    /**
     * This is different from [tryGetService] as this makes a provider from this class,
     * forcing this service to be created, no matter the types of services being pre-registered in [ServiceProviders]
     */
    private fun tryLoadService(clazz: KClass<*>): ServiceResult<Any> {
        val provider = context.serviceProviders.findForType(clazz)
            ?: return ServiceResult(null, "No service or factories found for type ${clazz.simpleNestedName}")
        provider.instance?.let { return ServiceResult(it, null) }

        val errorMessage = canCreateService(provider)
        if (errorMessage != null)
            return ServiceResult(null, errorMessage)
        return tryGetService(provider)
    }

    override fun <T : Any> peekServiceOrNull(clazz: KClass<T>): T? {
        val provider = context.serviceProviders.findForType(clazz) ?: return null
        return clazz.cast(provider.instance)
    }

    override fun <T : Any> peekServiceOrNull(name: String, requiredType: KClass<T>): T? {
        val provider = context.serviceProviders.findForName(name) ?: return null
        return provider.instance?.let { requiredType.safeCast(it) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> tryGetService(name: String, requiredType: KClass<T>): ServiceResult<T> {
        val provider = context.serviceProviders.findForName(name)
            ?: return ServiceResult(null, "No service or factories found for service name '$name'")

        val service = provider.instance
        if (service != null) {
            if (!requiredType.isInstance(service)) {
                return ServiceResult(null, "A service was found but type is incorrect, requested: ${requiredType.simpleNestedName}, actual: ${service::class.simpleNestedName}")
            }
            return ServiceResult(service as T, null)
        }

        val errorMessage = canCreateService(provider)
        if (errorMessage != null)
            return ServiceResult(null, errorMessage)
        return tryGetService(provider)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> tryGetService(clazz: KClass<T>): ServiceResult<T> = lock.withLock {
        val provider = context.serviceProviders.findForType(clazz)
            ?: return ServiceResult(null, "No service or factories found for type ${clazz.simpleNestedName}")
        val instance = provider.instance as T?
        if (instance != null) return ServiceResult(instance, null)

        val errorMessage = canCreateService(provider)
        if (errorMessage != null)
            return ServiceResult(null, errorMessage)

        return tryGetService(provider)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> tryGetService(provider: ServiceProvider): ServiceResult<T> {
        try {
            return serviceCreationStack.withServiceCreateKey(provider) {
                //Don't measure time globally, we need to not take into account the time to make dependencies
                val (anyResult, nanos) = provider.createInstance(this)
                //Doesn't really matter, the object is not used anyway
                val result: ServiceResult<T> = anyResult as ServiceResult<T>
                if (result.errorMessage != null)
                    return result

                val instance = result.getOrThrow()
                provider.types.forEach { serviceType ->
                    serviceMap.put(instance, serviceType, serviceType.getServiceName())
                }

                logger.trace { "Loaded service ${provider.types.joinToString(" and ") { it.simpleNestedName } } in %.3f ms".format((nanos.inWholeNanoseconds) / 1000000.0) }
                ServiceResult(instance, null)
            }
        } catch (e: Exception) {
            throw RuntimeException("Unable to create service ${provider.primaryType.simpleNestedName}", e)
        }
    }

    override fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>, name: String?) {
        context.serviceProviders.putServiceProvider(ClassServiceProvider(clazz, t))
        serviceMap.put(t, clazz, name ?: clazz.getServiceName())
    }

    internal fun getFunctionService(function: KFunction<*>): Any = when {
        function.isConstructor -> throwInternal(function, "Tried to get a function's instance but was a constructor, this should have been checked beforehand")
        function.isStatic -> throwInternal(function, "Tried to get a function's instance but was static, this should have been checked beforehand")
        else -> tryLoadService(function.declaringClass).getOrThrow()
    }

    internal fun getFunctionServiceOrNull(function: KFunction<*>): Any? = when {
        function.isConstructor || function.isStatic -> null
        else -> tryLoadService(function.declaringClass).getOrNull()
    }

    internal fun getParameters(types: List<KClass<*>>, map: Map<KClass<*>, Any> = mapOf()): List<Any> {
        return types.map {
            map[it] ?: getService(it)
        }
    }

    /**
     * Returns a non-null string if the service is not instantiable
     */
    internal fun canCreateService(clazz: KClass<*>): String? {
        val provider = context.serviceProviders.findForType(clazz)
            ?: return "No class or factories found for service ${clazz.simpleNestedName}"

        return canCreateService(provider)
    }

    /**
     * Returns a non-null string if the service is not instantiable
     */
    private fun canCreateService(provider: ServiceProvider): String? {
        if (provider.instance != null) return null

        return serviceCreationStack.withServiceCheckKey(provider) {
            provider.canInstantiate(this)
        }
    }

    internal fun findConditionalServiceChecker(clazz: KClass<*>): ConditionalServiceChecker? {
        //Kotlin implementations uses companion object
        clazz.companionObjectInstance?.let { companion ->
            requireUser(companion is ConditionalServiceChecker) {
                "Companion object of ${clazz.simpleNestedName} needs to implement ${ConditionalServiceChecker::class.simpleNestedName}"
            }

            return companion
        }

        //Find any nested class which extend the interface, is static and
        clazz.nestedClasses.forEach { nestedClass ->
            if (nestedClass == ConditionalServiceChecker::class) {
                requireUser(Modifier.isStatic(nestedClass.java.modifiers)) { "Nested class ${nestedClass.simpleNestedName} must be static" }

                val instance = nestedClass.constructors
                    .find { it.parameters.isEmpty() && it.isPublic }
                    ?.call() ?: throwUser("A ${ConditionalServiceChecker::class.simpleNestedName} constructor needs to be no-arg and accessible")

                return instance as ConditionalServiceChecker
            }
        }

        return null
    }

    @OptIn(ExperimentalTime::class)
    internal fun constructInstance(clazz: KClass<*>): TimedInstantiation {
        dynamicSuppliers.forEach { dynamicSupplierFunction ->
            measureTimedValue {
                runDynamicSupplier(clazz, dynamicSupplierFunction)
            }.let {
                it.value?.let { service ->
                    return TimedInstantiation(ServiceResult(service, null), it.duration)
                }
            }
        }

        //The command object has to be created either by the instance supplier
        // or by the **only** constructor a class has
        // It must resolve all parameters types with the registered parameter suppliers
        val instanceSupplier = serviceConfig.instanceSupplierMap[clazz]
        return when {
            instanceSupplier != null -> {
                measureTimedValue {
                    instanceSupplier.supply(context)
                        ?: throwService("Supplier function in class '${instanceSupplier::class.jvmName}' returned null")
                }
            }
            clazz.objectInstance != null -> measureTimedValue { clazz.objectInstance }
            else -> {
                val constructingFunction = findConstructingFunction(clazz).getOrThrow()

                val params = constructingFunction.nonInstanceParameters.map {
                    val dependencyResult = tryGetService(it.type.jvmErasure) //Try to get a dependency, if it doesn't work then return the message
                    dependencyResult.service ?: return TimedInstantiation(ServiceResult(null, dependencyResult.errorMessage!!), Duration.INFINITE)
                }
                measureTimedValue { constructingFunction.callStatic(*params.toTypedArray()) } //Avoid measuring time it takes to load other services
            }
        }.let {
            TimedInstantiation(ServiceResult(it.value, null), it.duration)
        }
    }

    internal fun findConstructingFunction(clazz: KClass<*>): ServiceResult<KFunction<*>> {
        //Find in companion object or in static methods
        val instanceSupplier = findInstanceSupplier(clazz, clazz.staticAndCompanionDeclaredMemberFunctions)

        if (instanceSupplier != null) {
            return ServiceResult(instanceSupplier, null)
        }

        val constructors = clazz.constructors
        if (constructors.isEmpty()) { return ServiceResult(null, "Class " + clazz.simpleNestedName + " must have an accessible constructor") }
        if (constructors.size != 1) { return ServiceResult(null, "Class " + clazz.simpleNestedName + " must have exactly one constructor") }

        val constructor = constructors.single()
        if (constructor.visibility != KVisibility.PUBLIC && constructor.visibility != KVisibility.INTERNAL) {
            return ServiceResult(null, "Constructor of ${clazz.simpleNestedName} must be public")
        }

        return ServiceResult(constructor, null)
    }

    private fun findInstanceSupplier(classType: KClass<*>, functions: Collection<KFunction<*>>): KFunction<*>? {
        return functions.withFilter(FunctionFilter.annotation<InstanceSupplier>()).let { supplierFunctions ->
            if (supplierFunctions.size > 1)
                throwUser("Class ${classType.simpleNestedName} needs to have only one method annotated with @${InstanceSupplier::class.simpleNestedName}")

            supplierFunctions.firstOrNull()?.also { function ->
                if (function.returnType.jvmErasure != classType)
                    throwUser(function, "Function needs to return a ${classType.simpleNestedName} as specified by @${InstanceSupplier::class.simpleNestedName}")
            }
        }
    }

    private fun runDynamicSupplier(requestedType: KClass<*>, dynamicSupplierFunction: KFunction<*>): Any? {
        val params: List<Any> = dynamicSupplierFunction.nonInstanceParameters.drop(1).map {
            //Try to get a dependency, if it doesn't work then skip this supplier
            tryGetService(it.type.jvmErasure).service ?: return null
        }

        return dynamicSupplierFunction.callStatic(requestedType.java, *params.toTypedArray())
    }

    data class TimedInstantiation(val result: ServiceResult<*>, val duration: Duration)

    companion object {
        internal fun <R> KFunction<R>.callStatic(vararg args: Any?): R {
            return when (val instanceParameter = this.instanceParameter) {
                null -> this.call(*args)
                else -> {
                    val companionObjectClazz = instanceParameter.type.jvmErasure
                    if (!companionObjectClazz.isCompanion)
                        throwInternal("Tried to call a non-static function but the ${companionObjectClazz.simpleNestedName} instance parameter is not a companion object")
                    val companionObjectInstance = companionObjectClazz.objectInstance
                        ?: throwInternal("Tried to call a non-static function but the ${companionObjectClazz.simpleNestedName} instance parameter is not a companion object")

                    this.call(companionObjectInstance, *args)
                }
            }
        }
    }
}

inline fun <reified T : Any> ServiceContainer.getService(): T = getService(T::class)

inline fun <reified T : Any> ServiceContainer.getServiceOrNull(): T? = getServiceOrNull(T::class)

inline fun <reified T : Any> ServiceContainer.putServiceAs(t: T) = putServiceAs(t, T::class)

inline fun <T, reified R : Any> ServiceContainer.lazy() = object : ReadOnlyProperty<T, R> {
    val value: R by lazy { getService(R::class) }

    override fun getValue(thisRef: T, property: KProperty<*>) = value
}

internal fun KClass<*>.getServiceName(annotation: BService? = this.findAnnotation()): String = when {
    annotation == null || annotation.name.isEmpty() -> this.simpleNestedName.replaceFirstChar { it.lowercase() }
    else -> annotation.name
}

internal fun KFunction<*>.getServiceName(annotation: BService? = this.findAnnotation()): String = when {
    annotation == null || annotation.name.isEmpty() -> this.name
    else -> annotation.name
}

internal val BContextImpl.loadableServices: Map<ServiceStart, List<KClass<*>>>
    @JvmSynthetic get() =
        enumMapOf<ServiceStart, MutableList<KClass<*>>>().also { loadableServices ->
            serviceAnnotationsMap
                .getAllClasses()
                .forEach { clazz ->
                    val start = clazz.findAnnotation<BService>()?.start ?: ServiceStart.DEFAULT
                    loadableServices.getOrPut(start) { mutableListOf() }.add(clazz)
                }
        }