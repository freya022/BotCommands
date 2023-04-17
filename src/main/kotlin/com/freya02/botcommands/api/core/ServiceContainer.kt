package com.freya02.botcommands.api.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.api.core.annotations.ServiceType
import com.freya02.botcommands.api.core.config.BServiceConfig
import com.freya02.botcommands.api.core.events.PreloadServiceEvent
import com.freya02.botcommands.api.core.suppliers.annotations.DynamicSupplier
import com.freya02.botcommands.api.core.suppliers.annotations.InstanceSupplier
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.core.*
import com.freya02.botcommands.internal.utils.FunctionFilter
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
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@InjectedService
class ServiceContainer internal constructor(private val context: BContextImpl) {
    private val logger = KotlinLogging.logger { }

    private val serviceConfig: BServiceConfig = context.config.serviceConfig

    @PublishedApi
    @JvmSynthetic
    internal val serviceMap = ServiceMap()

    private val unavailableServices: MutableMap<KClass<*>, String> = hashMapOf() //Must not contain InjectedService(s) !
    private val lock = ReentrantLock()

    private val localBeingCheckedSet: ThreadLocal<MutableSet<KClass<*>>> = ThreadLocal.withInitial { linkedSetOf() }

    private val dynamicSuppliers: List<KFunction<*>> by lazy {
        context.classPathContainer.classes.flatMap { clazz ->
            clazz.nonExtensionFunctions //Companion objects are included in those classes, no need to get them
                .withFilter(FunctionFilter.annotation<DynamicSupplier>())
                .requiredFilter(FunctionFilter.staticOrCompanion())
                .requiredFilter(FunctionFilter.firstArg(Class::class))
                .requiredFilter(FunctionFilter.returnType(Any::class))
        }
    }

    internal val loadableServices: Map<ServiceStart, List<KClass<*>>>
        @JvmSynthetic get() =
            enumMapOf<ServiceStart, MutableList<KClass<*>>>().also { loadableServices ->
                context.classPathContainer.classes.forEach { clazz ->
                    clazz.findAnnotation<BService>()?.let {
                        loadableServices.getOrPut(it.start) { mutableListOf() }.add(clazz)
                        return@forEach
                    }
                    clazz.findAnnotation<ConditionalService>()?.let {
                        loadableServices.getOrPut(it.start) { mutableListOf() }.add(clazz)
                        return@forEach
                    }
                }
            }

    init {
        putService(this)
        putService(context)
        putService(context.eventManager)
        putServiceAs<IEventManager>(context.eventManager) //Should be used if JDA is constructed as a service
        putService(context.classPathContainer)
        putServiceAs<BContext>(context)
        putServiceAs(context.config)
    }

    @JvmSynthetic
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

    @JvmSynthetic
    internal fun loadServices(loadableServices: Map<ServiceStart, List<KClass<*>>>, requestedStart: ServiceStart) {
        loadableServices[requestedStart]?.forEach { clazz ->
            //Skip classes that have been already loaded/checked
            if (clazz in serviceMap || clazz in unavailableServices) return@forEach

            tryGetService(clazz).errorMessage?.let { errorMessage ->
                logger.trace { "Service ${clazz.simpleName} not loaded: $errorMessage" }
            }
        }
    }

    fun <T : Any> getService(clazz: Class<T>): T {
        return getService(clazz.kotlin)
    }

    @JvmSynthetic
    inline fun <reified T : Any> getService(): T {
        return getService(T::class)
    }

    fun <T : Any> getService(clazz: KClass<T>): T {
        return tryGetService(clazz).getOrThrow()
    }

    @JvmSynthetic
    inline fun <reified T : Any> getServiceOrNull(): T? {
        return getServiceOrNull(T::class)
    }

    fun <T : Any> getServiceOrNull(clazz: Class<T>): T? {
        return getServiceOrNull(clazz.kotlin)
    }

    fun <T : Any> getServiceOrNull(clazz: KClass<T>): T? {
        return tryGetService(clazz).getOrNull()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> tryGetService(clazz: KClass<T>): ServiceResult<T> = lock.withLock {
        val service = serviceMap[clazz] as T?
        if (service != null) return ServiceResult(service, null)

        canCreateService(clazz)?.let { errorMessage -> return ServiceResult(null, errorMessage) }

        try {
            return localBeingCheckedSet.get().withServiceCreateKey(clazz) {
                //Don't measure time globally, we need to not take into account the time to make dependencies
                val (anyResult, nanos) = constructInstance(clazz)
                val result: ServiceResult<T> = anyResult as ServiceResult<T> //Doesn't really matter, the object is not used anyway
                if (result.errorMessage != null)
                    return result

                val instance = result.getOrThrow()
                when (val serviceType = clazz.findAnnotation<ServiceType>()) {
                    null -> serviceMap.put(instance, clazz)
                    else -> {
                        serviceMap.put(instance, serviceType.type)
                        if (serviceType.keepOriginalType) serviceMap.put(instance, clazz)
                    }
                }

                logger.trace { "Loaded service ${clazz.simpleName} in %.3f ms".format((nanos.inWholeNanoseconds) / 1000000.0) }
                ServiceResult(instance, null)
            }
        } catch (e: Exception) {
            throw RuntimeException("Unable to create service ${clazz.simpleName}", e)
        }
    }

    /**
     * Returns a non-null string if the service is not instantiable
     */
    internal fun canCreateService(clazz: KClass<*>): String? = localBeingCheckedSet.get().withServiceCheckKey(clazz) cachedCallback@{
        //If the object doesn't exist then check if it's an injected service, if it is then it cannot be created automatically
        if (clazz !in serviceMap) {
            unavailableServices[clazz]?.let { return it }

            clazz.findAnnotation<InjectedService>()?.let {
                //Skips cache
                return "Tried to load an unavailable InjectedService '${clazz.simpleName}', reason might include: ${it.message}"
            }
        } else {
            return null
        }

        // Services can be conditional
        // They can implement an interface to do checks
        // They can also depend on other services, in which case the interface becomes optional
        clazz.findAnnotation<ConditionalService>()?.let { conditionalService ->
            conditionalService.dependencies.forEach { dependency ->
                canCreateService(dependency)?.let { errorMessage ->
                    return@cachedCallback "Conditional service depends on ${dependency.simpleName} but it is not available: $errorMessage"
                }
            }

            //Skip checker if dependencies have been validated
            if (conditionalService.dependencies.isNotEmpty()) return@let

            val checker = findConditionalServiceChecker(clazz)
            requireUser(checker != null) {
                "Conditional service ${clazz.simpleName} needs to implement ${ConditionalServiceChecker::class.simpleName}, check the docs for more details"
            }
            checker.checkServiceAvailability(context)?.let { errorMessage -> return@cachedCallback errorMessage } //Final optional check
        }

        //Check parameters of dynamic resolvers
        dynamicSuppliers.forEach { dynamicSupplierFunction ->
            dynamicSupplierFunction.nonInstanceParameters.drop(1).forEach {
                canCreateService(it.type.jvmErasure)?.let { errorMessage -> return@cachedCallback errorMessage }
            }
        }

        //Is a singleton
        if (clazz.objectInstance != null) return null

        //Check constructor parameters
        //It's fine if there's no constructor, it just means it's not instantiable
        val constructingFunction = findConstructingFunction(clazz).let { it.getOrNull() ?: return it.errorMessage }
        constructingFunction.nonInstanceParameters.forEach {
            canCreateService(it.type.jvmErasure)?.let { errorMessage -> return@cachedCallback errorMessage }
        }

        return null
    }
//        ?.also { errorMessage -> unavailableServices[clazz] = errorMessage }

    fun getFunctionService(function: KFunction<*>): Any {
        return when {
            function.isStatic -> throwInternal("$function: Tried to get a function's instance but was static, this should have been checked beforehand")
            else -> getService(function.javaMethod!!.declaringClass)
        }
    }

    fun <T : Any> putService(t: T) {
        serviceMap.put(t, t::class)
    }

    inline fun <reified T : Any> putServiceAs(t: T) {
        serviceMap.put(t, T::class)
    }

    fun getParameters(types: List<KClass<*>>, map: Map<KClass<*>, Any> = mapOf()): List<Any> {
        return types.map {
            map[it] ?: getService(it)
        }
    }

    inline fun <T, reified R : Any> lazy(): ReadOnlyProperty<T, R> {
        val kClass = R::class

        return object : ReadOnlyProperty<T, R> {
            val value: R by lazy { getService(kClass) }

            override fun getValue(thisRef: T, property: KProperty<*>) = value
        }
    }

    //If services have circular dependencies during checking, consider it to not be an issue
    private inline fun <T : Any, R> MutableSet<KClass<*>>.withServiceCheckKey(clazz: KClass<T>, block: () -> R): R? {
        if (!this.add(clazz)) return null
        try {
            return block()
        } finally {
            this.remove(clazz)
        }
    }

    private inline fun <T : Any, R> MutableSet<KClass<*>>.withServiceCreateKey(clazz: KClass<T>, block: () -> R): R {
        if (!this.add(clazz))
            throw IllegalStateException("Circular dependency detected, list of the services being created : [${this.joinToString { it.java.simpleName }}] ; attempted to create a new ${clazz.java.simpleName}")
        try {
            return block()
        } finally {
            this.remove(clazz)
        }
    }

    private fun findConditionalServiceChecker(clazz: KClass<*>): ConditionalServiceChecker? {
        //Kotlin implementations uses companion object
        clazz.companionObjectInstance?.let { companion ->
            requireUser(companion is ConditionalServiceChecker) {
                "Companion object of ${clazz.simpleName} needs to implement ${ConditionalServiceChecker::class.simpleName}"
            }

            return companion
        }

        //Find any nested class which extend the interface, is static and
        clazz.nestedClasses.forEach { nestedClass ->
            if (nestedClass == ConditionalServiceChecker::class) {
                requireUser(Modifier.isStatic(nestedClass.java.modifiers)) { "Nested class ${nestedClass.java.simpleName} must be static" }

                val instance = nestedClass.constructors
                    .find { it.parameters.isEmpty() && it.isPublic }
                    ?.call() ?: throwUser("A ${ConditionalServiceChecker::class.simpleName} constructor needs to be no-arg and accessible")

                return instance as ConditionalServiceChecker
            }
        }

        return null
    }

    @OptIn(ExperimentalTime::class)
    private fun constructInstance(clazz: KClass<*>): TimedInstantiation {
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

    private fun findConstructingFunction(clazz: KClass<*>): ServiceResult<KFunction<*>> {
        //Find in companion object or in static methods
        val instanceSupplier = findInstanceSupplier(clazz, clazz.staticAndCompanionDeclaredMemberFunctions)

        if (instanceSupplier != null) {
            return ServiceResult(instanceSupplier, null)
        }

        val constructors = clazz.constructors
        if (constructors.isEmpty()) { return ServiceResult(null, "Class " + clazz.simpleNestedName + " must have an accessible constructor") }
        if (constructors.size != 1) { return ServiceResult(null, "Class " + clazz.simpleNestedName + " must have exactly one constructor") }

        return ServiceResult(constructors.single(), null)
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

    private fun <R> KFunction<R>.callStatic(vararg args: Any?): R {
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

    private data class TimedInstantiation(val result: ServiceResult<*>, val duration: Duration)
}
