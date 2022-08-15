package com.freya02.botcommands.core.internal

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.api.annotations.ConditionalService
import com.freya02.botcommands.core.api.annotations.ConditionalServiceCheck
import com.freya02.botcommands.core.api.annotations.LateService
import com.freya02.botcommands.core.api.config.BServiceConfig
import com.freya02.botcommands.core.api.events.PreloadServiceEvent
import com.freya02.botcommands.core.api.exceptions.ServiceException
import com.freya02.botcommands.core.api.suppliers.annotations.Supplier
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

private val LOGGER = Logging.getLogger()

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

                    LOGGER.trace("Loaded service: ${clazz.simpleName}")

                    return instance as T
                } catch (e: Exception) {
                    throw RuntimeException("Unable to create service ${clazz.simpleName}", e)
                } finally {
                    beingCreatedSet.clear()
                }
            }
        }
    }

    private fun <T : Any> checkConditions(clazz: KClass<T>) {
        val companionObj = clazz.companionObjectInstance ?: return

        val checks = companionObj::class.declaredMemberFunctions.filter { it.hasAnnotation<ConditionalServiceCheck>() }
        if (checks.isEmpty()) return
        if (checks.size > 1) throwUser("Class ${clazz.jvmName} has more than one ConditionalServiceCheck function")

        val func = checks.single()
        if (!func.isPublic) throwUser(func, "Function should be public")
        if (func.returnType.jvmErasure != String::class) throwUser(func, "Function should return a 'String?'")
        if (!func.returnType.isMarkedNullable) throwUser(func, "Function should return a 'String?'")

        val params = getParameters(func.nonInstanceParameters.map { it.type.jvmErasure }, mapOf(), false)
        val message: String? = func.call(companionObj, *params.toTypedArray()) as String?

        if (message != null) {
            throwService("Cannot auto-load ${clazz.simpleName}: $message")
        }
    }

    fun getFunctionService(function: KFunction<Any>): Any {
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
                runSupplierFunction(instanceSupplier, useNonClasspath) ?: throwService("Supplier function in class '${instanceSupplier::class.jvmName}' returned null")
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
}