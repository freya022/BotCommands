package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.annotations.api.annotations.Dependency
import com.freya02.botcommands.internal.BContextImpl
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

object ClassInstancer {
    @JvmStatic
    @Throws(InvocationTargetException::class, InstantiationException::class, IllegalAccessException::class)
    fun getMethodTarget(context: BContextImpl, method: Method): Any? {
        return when {
            Modifier.isStatic(method.modifiers) -> null
            else -> instantiate(context, method.declaringClass.kotlin)
        }
    }

    @Throws(InvocationTargetException::class, InstantiationException::class, IllegalAccessException::class)
    fun instantiate(context: BContextImpl, clazz: KClass<*>): Any {
        val oldInstance = context.getClassInstance(clazz)
        if (oldInstance != null) return oldInstance

        val instance = constructInstance(context, clazz)
        injectDependencies(context, instance)
        context.putClassInstance(clazz, instance)

        return instance
    }

    @Throws(InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class)
    private fun constructInstance(context: BContextImpl, clazz: KClass<*>): Any {
        for (dynamicInstanceSupplier in context.dynamicInstanceSuppliers) {
            val instance = dynamicInstanceSupplier.supply(context, clazz.java)
            if (instance != null) {
                return instance
            }
        }

        val instance: Any

        //The command object has to be created either by the instance supplier
        // or by the **only** constructor a class has
        // It must resolve all parameters types with the registered parameter suppliers
        val instanceSupplier = context.getInstanceSupplier(clazz.java)
        if (instanceSupplier != null) {
            instance = instanceSupplier.supply(context)
        } else {
            val constructors = clazz.constructors
            require(constructors.isNotEmpty()) { "Class " + clazz.simpleName + " must have an accessible constructor" }
            require(constructors.size <= 1) { "Class " + clazz.simpleName + " must have exactly one constructor" }

            val constructor = constructors.first()

            val parameters = constructor.valueParameters.map(KParameter::type).mapIndexed { i, parameterType ->
                val supplier = requireNotNull(context.getParameterSupplier(parameterType.jvmErasure.java)) {
                    "Found no constructor parameter supplier for parameter #$i of type $parameterType in class ${clazz.simpleName}"
                }

                return@mapIndexed supplier.supply(clazz.java)
            }

            instance = constructor.call(*parameters.toTypedArray())
        }
        return instance
    }

    @Throws(IllegalAccessException::class)
    private fun injectDependencies(context: BContextImpl, someCommand: Any) {
        for (field in someCommand.javaClass.declaredFields) {
            if (!field.isAnnotationPresent(Dependency::class.java)) continue
            if (!field.canAccess(someCommand)) {
                require(field.trySetAccessible()) { "Dependency field $field is not accessible (make it public ?)" }
            }

            val dependencySupplier = context.getCommandDependency(field.type)
                ?: throw IllegalArgumentException("Dependency supplier for field $field was not set")

            field[someCommand] = dependencySupplier.get()
        }
    }
}