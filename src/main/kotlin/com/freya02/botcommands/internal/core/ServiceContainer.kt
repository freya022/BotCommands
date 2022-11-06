package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.annotations.LateService
import com.freya02.botcommands.api.core.config.BServiceConfig
import com.freya02.botcommands.api.core.events.PreloadServiceEvent
import com.freya02.botcommands.api.core.exceptions.ServiceException
import com.freya02.botcommands.api.core.suppliers.annotations.Supplier
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

private val LOGGER = KotlinLogging.logger { }

class ServiceContainer internal constructor(private val context: BContextImpl) {
    private val serviceConfig: BServiceConfig = context.config.serviceConfig
    private val serviceMap: MutableMap<KClass<*>, Any> = hashMapOf()

    private val localBeingCreatedSet: ThreadLocal<MutableSet<KClass<*>>> = ThreadLocal.withInitial { linkedSetOf() }

    init {
        putService(this)
        putService(context)
        putService(context.eventManager)
        putService(context.classPathContainer)
        putServiceAs<BContext>(context)
        putServiceAs(context.config)
    }

    internal fun preloadServices() {
        runBlocking {
            getService(EventDispatcher::class).dispatchEvent(PreloadServiceEvent())
        }

        context.classPathContainer.classes.forEach {
            if (it.hasAnnotation<BService>() || (it.hasAnnotation<ConditionalService>() && !it.hasAnnotation<LateService>())) {
                getService(it)
            }
        }
    }

    @JvmOverloads
    fun <T : Any> getService(clazz: Class<T>, useNonClasspath: Boolean = false): T {
        return getService(clazz.kotlin, useNonClasspath)
    }

    fun <T : Any> tryGetService(clazz: KClass<T>): Result<T> {
        return try {
            Result.success(getService(clazz))
        } catch (e: ServiceException) {
            Result.failure(e)
        }
    }

    inline fun <reified T : Any> getService(useNonClasspath: Boolean = false): T {
        return getService(T::class, useNonClasspath)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getService(clazz: KClass<T>, useNonClasspath: Boolean = false): T {
        synchronized(serviceMap) {
            return (serviceMap[clazz] as T?) ?: run {
                //Don't autoload here as it could chain into loading stuff like BContextImpl or ApplicationCommandManager, which you shouldn't create automatically
                //Cannot load if the class isn't in the class path or the loading isn't forced
                //Still load late services if they're requested
                if (!useNonClasspath && !context.classPathContainer.classes.contains(clazz) && !clazz.hasAnnotation<LateService>()) {
                    if (clazz.hasAnnotation<ConditionalService>()) {
                        throwService("Cannot auto-load ${clazz.jvmName}: ${clazz.findAnnotation<ConditionalService>()!!.message}")
                    } else {
                        throwService("Cannot auto-load ${clazz.jvmName} as it is not in the classpath")
                    }
                }

                val beingCreatedSet = localBeingCreatedSet.get()
                try {
                    if (!beingCreatedSet.add(clazz)) {
                        throw IllegalStateException("Circular dependency detected, list of the services being created : [${beingCreatedSet.joinToString { it.java.simpleName }}] ; attempted to create a new ${clazz.java.simpleName}")
                    }

                    checkConditions(clazz)

                    val instance = constructInstance(clazz, false)

                    beingCreatedSet.remove(clazz)

                    serviceMap[clazz] = instance

                    LOGGER.trace { "Loaded service: ${clazz.simpleName}" }

                    return instance as T
                } catch (e: Exception) {
                    throw RuntimeException("Unable to create service ${clazz.simpleName}", e)
                } finally {
                    beingCreatedSet.clear()
                }
            }
        }
    }

    /**
     * Returns a non-null string if the service is not instantiable
     */
    internal fun canCreateService(clazz: KClass<*>): String? {
        // Services can be conditional
        // They can implement an interface to do checks
        // They can also depend on other services, in which case the interface becomes optional
        return clazz.findAnnotation<ConditionalService>()?.let { conditionalService ->
            conditionalService.dependencies.forEach { dependency ->
                canCreateService(dependency)?.let { errorMessage ->
                    return "Conditional service depends on ${dependency.simpleName} but it is not available: $errorMessage"
                }
            }

            val checker = findConditionalServiceChecker(clazz)
            requireUser(checker != null || conditionalService.dependencies.isNotEmpty()) { //Either a checker or validated dependencies
                "Conditional service ${clazz.simpleName} needs to implement ${ConditionalServiceChecker::class.simpleName}, check the docs for more details"
            }
            return checker?.checkServiceAvailability(context) //Final optional check
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

    private fun checkConditions(clazz: KClass<*>) {
        canCreateService(clazz)?.let { errorMessage ->
            throwService("Cannot auto-load ${clazz.simpleName}: $errorMessage")
        }
    }

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

    fun getParameters(types: List<KClass<*>>, map: Map<KClass<*>, Any> = mapOf(), useNonClasspath: Boolean = false): List<Any> {
        return types.map {
            map[it] ?: getService(it, useNonClasspath)
        }
    }

    private fun constructInstance(clazz: KClass<*>, useNonClasspath: Boolean): Any {
        for (dynamicInstanceSupplier in serviceConfig.dynamicInstanceSuppliers) {
            val instance = runSupplierFunction(dynamicInstanceSupplier, useNonClasspath)
            if (instance != null) {
                return instance
            }
        }

        //The command object has to be created either by the instance supplier
        // or by the **only** constructor a class has
        // It must resolve all parameters types with the registered parameter suppliers
        val instanceSupplier = serviceConfig.instanceSupplierMap[clazz]
        return when {
            instanceSupplier != null -> {
                instanceSupplier.supply(context)
                    ?: throwService("Supplier function in class '${instanceSupplier::class.jvmName}' returned null")
            }
            else -> {
                val constructors = clazz.constructors
                require(constructors.isNotEmpty()) { "Class " + clazz.simpleName + " must have an accessible constructor" }
                require(constructors.size <= 1) { "Class " + clazz.simpleName + " must have exactly one constructor" }

                val constructor = constructors.first()

                val params = getParameters(constructor.nonInstanceParameters.map { it.type.jvmErasure }, mapOf(), useNonClasspath)
                constructor.call(*params.toTypedArray())
            }
        }
    }

    private fun runSupplierFunction(supplier: Any, useNonClasspath: Boolean): Any? { //TODO test supplier func
        val suppliers = supplier::class
            .declaredMemberFunctions
            .filter { it.hasAnnotation<Supplier>() }

        if (suppliers.size > 1) {
            throwService("Class ${supplier::class.jvmName} should have only one supplier function")
        } else if (suppliers.isEmpty()) {
            throwService("Class ${supplier::class.jvmName} should have one supplier function")
        }

        val supplierFunction = suppliers.first()
        val annotation = supplierFunction.findAnnotation<Supplier>()
            ?: throwInternal("Supplier annotation should have been checked but was not found")

        if (supplierFunction.returnType.jvmErasure != annotation.type) {
            throwService("Function should return the type declared in @Supplier", supplierFunction)
        }

        val params = getParameters(supplierFunction.nonInstanceParameters.map { it.type.jvmErasure }, mapOf(), useNonClasspath)

        return supplierFunction.call(*params.toTypedArray())
    }

    internal data class ServiceAvailability(val available: Boolean, val errorMessage: String?)
}