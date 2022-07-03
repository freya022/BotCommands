package com.freya02.botcommands.core.internal

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.api.config.BServiceConfig
import com.freya02.botcommands.core.api.suppliers.annotations.Supplier
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.isStatic
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
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

        context.classPathContainer.classes.forEach {
            if (it.hasAnnotation<BService>()) {
                getService(it)

                LOGGER.trace("Loaded service: ${it.simpleName}")
            }
        }
    }

    fun <T : Any> getService(clazz: Class<T>): T {
        return getService(clazz.kotlin)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getService(clazz: KClass<T>): T {
        synchronized(serviceMap) {
            return (serviceMap[clazz] as T?) ?: run {
                //Don't autoload here as it could chain into loading stuff like BContextImpl or ApplicationCommandManager, which you shouldn't create automatically
                if (!context.classPathContainer.classes.contains(clazz)) {
                    throwUser("Cannot auto-load ${clazz.jvmName} as it is not in the classpath")
                }

                val beingCreatedSet = localBeingCreatedSet.get()
                try {
                    if (!beingCreatedSet.add(clazz)) {
                        throw IllegalStateException("Circular dependency detected, list of the services being created : [${beingCreatedSet.joinToString { it.java.simpleName }}] ; attempted to create a new ${clazz.java.simpleName}")
                    }

                    val instance = constructInstance(clazz)

                    beingCreatedSet.remove(clazz)

                    serviceMap[clazz] = instance

                    return instance as T
                } catch (e: Exception) {
                    throw RuntimeException("Unable to create service ${clazz.simpleName}", e)
                } finally {
                    beingCreatedSet.clear()
                }
            }
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

    fun getParameters(types: List<KClass<*>>, map: Map<KClass<*>, Any> = mapOf()): List<Any> {
        return types.map {
            map[it] ?: getService(it)
        }
    }

    private fun constructInstance(clazz: KClass<*>): Any {
        for (dynamicInstanceSupplier in serviceConfig.dynamicInstanceSuppliers) {
            val instance = runSupplierFunction(dynamicInstanceSupplier)
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
                runSupplierFunction(instanceSupplier) ?: throwUser("Supplier function in class '${instanceSupplier::class.jvmName}' returned null")
            }
            else -> {
                val constructors = clazz.constructors
                require(constructors.isNotEmpty()) { "Class " + clazz.simpleName + " must have an accessible constructor" }
                require(constructors.size <= 1) { "Class " + clazz.simpleName + " must have exactly one constructor" }

                val constructor = constructors.first()

                val params = getParameters(constructor.nonInstanceParameters.map { it.type.jvmErasure }, mapOf())
                constructor.call(*params.toTypedArray())
            }
        }
    }

    private fun runSupplierFunction(supplier: Any): Any? {
        val suppliers = supplier::class
            .declaredMemberFunctions
            .filter { it.hasAnnotation<Supplier>() }

        if (suppliers.size > 1) {
            throwUser("Class ${supplier::class.jvmName} should have only one supplier function")
        } else if (suppliers.isEmpty()) {
            throwUser("Class ${supplier::class.jvmName} should have one supplier function")
        }

        val supplierFunction = suppliers.first()
        val annotation = supplierFunction.findAnnotation<Supplier>()
            ?: throwInternal("Supplier annotation should have been checked but was not found")

        if (supplierFunction.returnType.jvmErasure != annotation.type) {
            throwUser(supplierFunction, "Function should return the type declared in @Supplier")
        }

        val params = getParameters(supplierFunction.nonInstanceParameters.map { it.type.jvmErasure }, mapOf())

        return supplierFunction.call(*params.toTypedArray())
    }
}