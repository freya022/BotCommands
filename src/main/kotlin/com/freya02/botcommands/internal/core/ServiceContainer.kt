package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.api.core.annotations.ServiceType
import com.freya02.botcommands.api.core.config.BServiceConfig
import com.freya02.botcommands.api.core.events.PreloadServiceEvent
import com.freya02.botcommands.api.core.suppliers.annotations.Supplier
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.isLoadableService
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.lang.reflect.Modifier
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class ServiceContainer internal constructor(private val context: BContextImpl) {
    private val logger = KotlinLogging.logger { }

    private val serviceConfig: BServiceConfig = context.config.serviceConfig
    private val serviceMap: MutableMap<KClass<*>, Any> = hashMapOf()
    private val unavailableServices: MutableMap<KClass<*>, String> = hashMapOf() //Must not contain InjectedService(s) !
    private val lock = ReentrantLock()

    private val localBeingCheckedSet: ThreadLocal<MutableSet<KClass<*>>> = ThreadLocal.withInitial { linkedSetOf() }

    init {
        putService(this)
        putService(context)
        putService(context.eventManager)
        putService(context.classPathContainer)
        putServiceAs<BContext>(context)
        putServiceAs(context.config)
    }

    @JvmSynthetic
    internal fun preloadServices() {
        runBlocking {
            getService(EventDispatcher::class).dispatchEvent(PreloadServiceEvent())
        }

        context.classPathContainer.classes.forEach { clazz ->
            if (clazz.isLoadableService()) {
                if (clazz.findAnnotation<BService>()?.lazy == true) return@forEach
                if (clazz.findAnnotation<ConditionalService>()?.lazy == true) return@forEach

                if (clazz in serviceMap || clazz in unavailableServices) return@forEach //Skip classes that have been already loaded/checked
                tryGetService(clazz).errorMessage?.let { errorMessage ->
                    logger.trace { "Service ${clazz.simpleName} not loaded: $errorMessage" }
                }
            }
        }
    }

    fun <T : Any> getService(clazz: Class<T>): T {
        return getService(clazz.kotlin)
    }

    inline fun <reified T : Any> getService(): T {
        return getService(T::class)
    }

    fun <T : Any> getService(clazz: KClass<T>): T {
        return tryGetService(clazz).getOrThrow()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> tryGetService(clazz: KClass<T>): ServiceResult<T> = lock.withLock {
        canCreateService(clazz)?.let { errorMessage -> return ServiceResult(null, errorMessage) }

        val service = serviceMap[clazz] as T?
        if (service != null) return ServiceResult(service, null)

        try {
            return localBeingCheckedSet.get().withServiceCreateKey(clazz) {
                //Don't measure time globally, we need to not take into account the time to make dependencies
                val (anyResult, nanos) = constructInstance(clazz)
                val result: ServiceResult<T> = anyResult as ServiceResult<T> //Doesn't really matter, the object is not used anyway
                if (result.errorMessage != null)
                    return result

                val instance = result.getOrThrow()
                when (val serviceType = clazz.findAnnotation<ServiceType>()) {
                    null -> serviceMap[clazz] = instance
                    else -> {
                        serviceMap[serviceType.type] = instance
                        if (serviceType.keepOriginalType) serviceMap[clazz] = instance
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

            val checker = findConditionalServiceChecker(clazz)
            requireUser(checker != null || conditionalService.dependencies.isNotEmpty()) { //Either a checker or validated dependencies
                "Conditional service ${clazz.simpleName} needs to implement ${ConditionalServiceChecker::class.simpleName}, check the docs for more details"
            }
            checker?.checkServiceAvailability(context)?.let { errorMessage -> return@cachedCallback errorMessage } //Final optional check
        }

        //Check parameters of dynamic resolvers
        for (supplier in serviceConfig.dynamicInstanceSuppliers) {
            findSupplierFunction(supplier).nonInstanceParameters.forEach {
                canCreateService(it.type.jvmErasure)?.let { errorMessage -> return@cachedCallback errorMessage }
            }
        }

        //Check constructor parameters
        findConstructor(clazz).nonInstanceParameters.forEach {
            canCreateService(it.type.jvmErasure)?.let { errorMessage -> return@cachedCallback errorMessage }
        }

        return null
    }?.also { errorMessage -> unavailableServices[clazz] = errorMessage }

    fun getFunctionService(function: KFunction<*>): Any {
        return when {
            function.isStatic -> throwInternal("$function: Tried to get a function's instance but was static, this should have been checked beforehand")
            else -> getService(function.javaMethod!!.declaringClass)
        }
    }

    fun <T : Any> putService(t: T) {
        serviceMap[t::class] = t
    }

    internal inline fun <reified T : Any> putServiceAs(t: T) {
        serviceMap[T::class] = t
    }

    fun getParameters(types: List<KClass<*>>, map: Map<KClass<*>, Any> = mapOf()): List<Any> {
        return types.map {
            map[it] ?: getService(it)
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
        for (dynamicInstanceSupplier in serviceConfig.dynamicInstanceSuppliers) {
            measureTimedValue {
                runSupplierFunction(dynamicInstanceSupplier)
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
            else -> {
                val constructor = findConstructor(clazz)

                val params = constructor.nonInstanceParameters.map {
                    val dependencyResult = tryGetService(it.type.jvmErasure) //Try to get a dependency, if it doesn't work then return the message
                    dependencyResult.service ?: return TimedInstantiation(ServiceResult(null, dependencyResult.errorMessage!!), Duration.INFINITE)
                }
                measureTimedValue { constructor.call(*params.toTypedArray()) } //Avoid measuring time it takes to load other services
            }
        }.let {
            TimedInstantiation(ServiceResult(it.value, null), it.duration)
        }
    }

    private fun findConstructor(clazz: KClass<*>): KFunction<Any> {
        val constructors = clazz.constructors
        require(constructors.isNotEmpty()) { "Class " + clazz.simpleNestedName + " must have an accessible constructor" }
        require(constructors.size == 1) { "Class " + clazz.simpleNestedName + " must have exactly one constructor" }

        return constructors.single()
    }

    private fun runSupplierFunction(supplier: Any): Any? { //TODO test supplier func
        val supplierFunction = findSupplierFunction(supplier)

        val params = supplierFunction.nonInstanceParameters.map {
            val dependencyResult = tryGetService(it.type.jvmErasure) //Try to get a dependency, if it doesn't work then skip this supplier
            dependencyResult.service ?: return null
        }

        return supplierFunction.call(*params.toTypedArray())
    }

    private fun findSupplierFunction(supplier: Any): KFunction<*> {
        val suppliers = supplier::class
            .declaredMemberFunctions
            .filter { it.hasAnnotation<Supplier>() }

        if (suppliers.size != 1) {
            throwService("Class ${supplier::class.jvmName} should have only one supplier function")
        }

        val supplierFunction = suppliers.single()
        val annotation = supplierFunction.findAnnotation<Supplier>()
            ?: throwInternal("Supplier annotation should have been checked but was not found")

        if (supplierFunction.returnType.jvmErasure != annotation.type) {
            throwService("Function should return the type declared in @Supplier", supplierFunction)
        }
        return supplierFunction
    }

    data class ServiceResult<T>(val service: T?, val errorMessage: String?) {
        init {
            if (service == null && errorMessage == null) {
                throwInternal("ServiceResult should contain either the service or the error message")
            }
        }

        fun getOrThrow(): T = when {
            service != null -> service
            errorMessage != null -> throwService(errorMessage)
            else -> throwInternal("ServiceResult should contain either the service or the error message")
        }
    }

    private data class TimedInstantiation(val result: ServiceResult<*>, val duration: Duration)
}